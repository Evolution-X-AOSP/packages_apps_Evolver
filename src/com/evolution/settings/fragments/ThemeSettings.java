/*
 * Copyright (C) 2019-2021 The Evolution X Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolution.settings.fragments;

import static android.os.UserHandle.USER_CURRENT;
import static android.os.UserHandle.USER_SYSTEM;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.IOverlayManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.*;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.evolution.EvolutionUtils;
import com.android.internal.util.evolution.ThemesUtils;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.development.OverlayCategoryPreferenceController;
import com.android.settings.display.FontPickerPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.display.AccentColorPreferenceController;
import com.evolution.settings.display.QsTileStylePreferenceController;
import com.evolution.settings.display.SwitchStylePreferenceController;
import com.evolution.settings.preference.SystemSettingListPreference;
import com.evolution.settings.preference.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class ThemeSettings extends DashboardFragment implements OnPreferenceChangeListener {

    public static final String TAG = "ThemeSettings";

    private static final String CUSTOM_CLOCK_FACE = Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_FACE;
    private static final String DEFAULT_CLOCK = "com.android.keyguard.clock.DefaultClockController";
    private static final String HIDE_NOTCH = "display_hide_notch";
    private static final String PREF_KEY_CUTOUT = "cutout_category";
    private static final String PREF_NAVBAR_STYLE = "theme_navbar_style";
    private static final String SLIDER_STYLE  = "slider_style";

    private static FontPickerPreferenceController mFontPickerPreference;

    private Context mContext;
    private Handler mHandler;
    private IOverlayManager mOverlayManager;
    private IOverlayManager mOverlayService;
    private IntentFilter mIntentFilter;

    private ListPreference mLockClockStyles;
    private ListPreference mNavbarPicker;
    private SystemSettingListPreference mSlider;
    private SystemSettingSwitchPreference mHideNotch;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.android.server.ACTION_FONT_CHANGED")) {
                mFontPickerPreference.stopProgress();
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefScreen = getPreferenceScreen();

        final Resources res = getResources();
        mContext = getActivity();

        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.android.server.ACTION_FONT_CHANGED");

        mLockClockStyles = (ListPreference) findPreference(CUSTOM_CLOCK_FACE);
        String mLockClockStylesValue = getLockScreenCustomClockFace();
        mLockClockStyles.setValue(mLockClockStylesValue);
        mLockClockStyles.setSummary(mLockClockStyles.getEntry());
        mLockClockStyles.setOnPreferenceChangeListener(this);

        mNavbarPicker = (ListPreference) findPreference(PREF_NAVBAR_STYLE);
        int navbarStyleValues = getOverlayPosition(ThemesUtils.NAVBAR_STYLES);
        if (navbarStyleValues != -1) {
            mNavbarPicker.setValue(String.valueOf(navbarStyleValues + 2));
        } else {
            mNavbarPicker.setValue("1");
        }
        mNavbarPicker.setSummary(mNavbarPicker.getEntry());
        mNavbarPicker.setOnPreferenceChangeListener(this);

        // On three button nav
        if (!EvolutionUtils.isThemeEnabled("com.android.internal.systemui.navbar.threebutton")) {
            mNavbarPicker.setVisible(false);
        }

        PreferenceCategory mCutoutPref = (PreferenceCategory) prefScreen
                .findPreference(PREF_KEY_CUTOUT);
        String hasDisplayCutout = getResources().getString(com.android.internal.R.string.config_mainBuiltInDisplayCutout);
        if (TextUtils.isEmpty(hasDisplayCutout)) {
            prefScreen.removePreference(mCutoutPref);
        }

        mHideNotch = (SystemSettingSwitchPreference) prefScreen.findPreference(HIDE_NOTCH);
        boolean mHideNotchSupported = res.getBoolean(
                com.android.internal.R.bool.config_showHideNotchSettings);
        if (!mHideNotchSupported) {
            prefScreen.removePreference(mHideNotch);
        }

        mSlider = (SystemSettingListPreference) findPreference(SLIDER_STYLE);
        mCustomSettingsObserver.observe();
    }

    private int getOverlayPosition(String[] overlays) {
        int position = -1;
        for (int i = 0; i < overlays.length; i++) {
            String overlay = overlays[i];
            if (EvolutionUtils.isThemeEnabled(overlay)) {
                position = i;
            }
        }
        return position;
    }

    private String getOverlayName(String[] overlays) {
        String overlayName = null;
        for (int i = 0; i < overlays.length; i++) {
            String overlay = overlays[i];
            if (EvolutionUtils.isThemeEnabled(overlay)) {
                overlayName = overlay;
            }
        }
        return overlayName;
    }

    public void handleOverlays(String packagename, Boolean state, IOverlayManager mOverlayManager) {
        try {
            mOverlayService.setEnabled(packagename, state, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {

        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            Context mContext = getContext();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.SLIDER_STYLE),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.SLIDER_STYLE))) {
                updateSlider();
            }
        }
    }

    private void updateSlider() {
        ContentResolver resolver = getActivity().getContentResolver();

        boolean sliderDefault = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.SLIDER_STYLE , 0, UserHandle.USER_CURRENT) == 0;
        boolean sliderOOS = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.SLIDER_STYLE , 0, UserHandle.USER_CURRENT) == 1;
        boolean sliderAosp = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.SLIDER_STYLE , 0, UserHandle.USER_CURRENT) == 2;
        boolean sliderRUI = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.SLIDER_STYLE , 0, UserHandle.USER_CURRENT) == 3;

        if (sliderDefault) {
            setDefaultSlider(mOverlayService);
        } else if (sliderOOS) {
            enableSlider(mOverlayService, "com.android.theme.systemui_slider_oos");
        } else if (sliderAosp) {
            enableSlider(mOverlayService, "com.android.theme.systemui_slider.aosp");
        } else if (sliderRUI) {
            enableSlider(mOverlayService, "com.android.theme.systemui_slider.rui");
        }
    }

    public static void setDefaultSlider(IOverlayManager overlayManager) {
        for (int i = 0; i < SLIDERS.length; i++) {
            String sliders = SLIDERS[i];
            try {
                overlayManager.setEnabled(sliders, false, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void enableSlider(IOverlayManager overlayManager, String overlayName) {
        try {
            for (int i = 0; i < SLIDERS.length; i++) {
                String sliders = SLIDERS[i];
                try {
                    overlayManager.setEnabled(sliders, false, USER_SYSTEM);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            overlayManager.setEnabled(overlayName, true, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static final String[] SLIDERS = {
        "com.android.theme.systemui_slider_oos",
        "com.android.theme.systemui_slider.aosp",
        "com.android.theme.systemui_slider.rui"
    };

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockClockStyles) {
            setLockScreenCustomClockFace((String) newValue);
            int index = mLockClockStyles.findIndexOfValue((String) newValue);
            mLockClockStyles.setSummary(mLockClockStyles.getEntries()[index]);
            return true;
        } else if (preference == mNavbarPicker) {
            String navbarStyle = (String) newValue;
            int navbarStyleValue = Integer.parseInt(navbarStyle);
            mNavbarPicker.setValue(String.valueOf(navbarStyleValue));
            String overlayName = getOverlayName(ThemesUtils.NAVBAR_STYLES);
                if (overlayName != null) {
                    handleOverlays(overlayName, false, mOverlayManager);
                }
                if (navbarStyleValue > 1) {
                    handleOverlays(ThemesUtils.NAVBAR_STYLES[navbarStyleValue - 2],
                            true, mOverlayManager);
            }
            mNavbarPicker.setSummary(mNavbarPicker.getEntry());
            return true;
        } else if (preference == mSlider) {
            mCustomSettingsObserver.observe();
            return true;
        }
        return false;
    }

    private String getLockScreenCustomClockFace() {
        String value = Settings.Secure.getStringForUser(mContext.getContentResolver(),
                CUSTOM_CLOCK_FACE, USER_CURRENT);

        if (value == null || value.isEmpty()) value = DEFAULT_CLOCK;

        try {
            JSONObject json = new JSONObject(value);
            return json.getString("clock");
        } catch (JSONException ex) {
        }
        return value;
    }

    private void setLockScreenCustomClockFace(String value) {
        try {
            JSONObject json = new JSONObject();
            json.put("clock", value);
            Settings.Secure.putStringForUser(mContext.getContentResolver(), CUSTOM_CLOCK_FACE,
                    json.toString(), USER_CURRENT);
        } catch (JSONException ex) {
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.evolution_settings_themes;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, Fragment fragment) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new AccentColorPreferenceController(context));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.accent_color"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.adaptive_icon_shape"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.icon_pack"));
        controllers.add(new QsTileStylePreferenceController(context));
        controllers.add(new SwitchStylePreferenceController(context));
        controllers.add(mFontPickerPreference = new FontPickerPreferenceController(context, lifecycle));
        return controllers;
    }

    @Override
    public void onResume() {
        super.onResume();
        final Context context = getActivity();
        context.registerReceiver(mIntentReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        final Context context = getActivity();
        context.unregisterReceiver(mIntentReceiver);
        mFontPickerPreference.stopProgress();
    }

    /**
     * For Search.
     */

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.evolution_settings_themes);
}
