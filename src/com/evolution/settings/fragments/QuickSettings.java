/*
 * Copyright (C) 2023 Evolution X
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

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.evolution.EvolutionUtils;
import com.android.internal.util.evolution.ThemeUtils;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.preference.SystemSettingEditTextPreference;
import com.evolution.settings.preference.SystemSettingListPreference;

import java.util.List;

@SearchIndexable
public class QuickSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "QuickSettings";
    private static final String QS_FOOTER_TEXT_STRING = "qs_footer_text_string";
    private static final String KEY_BRIGHTNESS_SLIDER_POSITION = "qs_brightness_slider_position";
    private static final String KEY_QS_PANEL_STYLE  = "qs_panel_style";
    private static final String KEY_QS_VIBRATION_CATEGORY = "quick_settings_vibration";
    private static final String KEY_SHOW_AUTO_BRIGHTNESS = "qs_show_auto_brightness";
    private static final String KEY_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";
    private static final String QUICK_PULLDOWN = "status_bar_quick_qs_pulldown";
    private static final String overlayThemeTarget  = "com.android.systemui";

    private ListPreference mShowBrightnessSlider;
    private ListPreference mBrightnessSliderPosition;
    private ListPreference mQuickPulldown;
    private PreferenceCategory mQuickSettingsVibration;
    private SwitchPreference mShowAutoBrightness;
    private SystemSettingEditTextPreference mFooterString;
    private SystemSettingListPreference mQsStyle;
    private ThemeUtils mThemeUtils;
    private Handler mHandler;
    private Vibrator mVibrator;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.evolution_settings_quicksettings;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

	mThemeUtils = new ThemeUtils(getActivity());

        final Context mContext = getActivity().getApplicationContext();
        final ContentResolver resolver = mContext.getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mQsStyle = (SystemSettingListPreference) findPreference(KEY_QS_PANEL_STYLE);
        mCustomSettingsObserver.observe();

        mShowBrightnessSlider = findPreference(KEY_SHOW_BRIGHTNESS_SLIDER);
        mShowBrightnessSlider.setOnPreferenceChangeListener(this);
        boolean showSlider = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT) > 0;

        mBrightnessSliderPosition = findPreference(KEY_BRIGHTNESS_SLIDER_POSITION);
        mBrightnessSliderPosition.setEnabled(showSlider);

        mShowAutoBrightness = findPreference(KEY_SHOW_AUTO_BRIGHTNESS);
        boolean automaticAvailable = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);
        if (automaticAvailable) {
            mShowAutoBrightness.setEnabled(showSlider);
        } else {
            prefScreen.removePreference(mShowAutoBrightness);
        }

        int qpmode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 0, UserHandle.USER_CURRENT);
        mQuickPulldown = (ListPreference) findPreference(QUICK_PULLDOWN);
        mQuickPulldown.setValue(String.valueOf(qpmode));
        mQuickPulldown.setSummary(mQuickPulldown.getEntry());
        mQuickPulldown.setOnPreferenceChangeListener(this);

        mFooterString = (SystemSettingEditTextPreference) findPreference(QS_FOOTER_TEXT_STRING);
        mFooterString.setOnPreferenceChangeListener(this);
        String footerString = Settings.System.getString(resolver,
                QS_FOOTER_TEXT_STRING);
        if (footerString != null && !footerString.isEmpty())
            mFooterString.setText(footerString);
        else {
            mFooterString.setText("#KeepEvolving");
            Settings.System.putString(resolver,
                    Settings.System.QS_FOOTER_TEXT_STRING, "#KeepEvolving");
        }

        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mQuickSettingsVibration = (PreferenceCategory) findPreference(KEY_QS_VIBRATION_CATEGORY);
        if (!mVibrator.hasVibrator()) {
            prefScreen.removePreference(mQuickSettingsVibration);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mShowBrightnessSlider) {
            int value = Integer.parseInt((String) newValue);
            mBrightnessSliderPosition.setEnabled(value > 0);
            if (mShowAutoBrightness != null)
                mShowAutoBrightness.setEnabled(value > 0);
            return true;
        } else if (preference == mQuickPulldown) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, value,
                    UserHandle.USER_CURRENT);
            int index = mQuickPulldown.findIndexOfValue((String) newValue);
            mQuickPulldown.setSummary(
                    mQuickPulldown.getEntries()[index]);
            return true;
        } else if (preference == mFooterString) {
            String value = (String) newValue;
            if (value != null && !value.isEmpty())
                Settings.System.putString(resolver,
                        Settings.System.QS_FOOTER_TEXT_STRING, value);
            else {
                mFooterString.setText("#KeepEvolving");
                Settings.System.putString(resolver,
                        Settings.System.QS_FOOTER_TEXT_STRING, "#KeepEvolving");
            }
            return true;
        } else if (preference == mQsStyle) {
            mCustomSettingsObserver.observe();
            return true;
        }
        return false;
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
                    Settings.System.QS_PANEL_STYLE),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.QS_PANEL_STYLE))) {
                updateQsStyle();
            }
        }
    }

    private void updateQsStyle() {
        ContentResolver resolver = getActivity().getContentResolver();

        int qsPanelStyle = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.QS_PANEL_STYLE , 0, UserHandle.USER_CURRENT);

        String qsPanelStyleCategory = "android.theme.customization.qs_panel";

        /// reset all overlays before applying
        resetQsOverlays(qsPanelStyleCategory);

        if (qsPanelStyle == 0) return;

        switch (qsPanelStyle) {
            case 1:
              setQsStyle("com.android.system.qs.outline", qsPanelStyleCategory);
              break;
            case 2:
            case 3:
              setQsStyle("com.android.system.qs.twotoneaccent", qsPanelStyleCategory);
              break;
            case 4:
              setQsStyle("com.android.system.qs.shaded", qsPanelStyleCategory);
              break;
            case 5:
              setQsStyle("com.android.system.qs.cyberpunk", qsPanelStyleCategory);
              break;
            case 6:
              setQsStyle("com.android.system.qs.neumorph", qsPanelStyleCategory);
              break;
            case 7:
              setQsStyle("com.android.system.qs.reflected", qsPanelStyleCategory);
              break;
            case 8:
              setQsStyle("com.android.system.qs.surround", qsPanelStyleCategory);
              break;
            case 9:
              setQsStyle("com.android.system.qs.thin", qsPanelStyleCategory);
              break;
            case 10:
              setQsStyle("com.android.system.qs.twotoneaccenttrans", qsPanelStyleCategory);
              break;
            default:
              break;
        }
    }

    public void resetQsOverlays(String category) {
        mThemeUtils.setOverlayEnabled(category, overlayThemeTarget, overlayThemeTarget);
    }

    public void setQsStyle(String overlayName, String category) {
        mThemeUtils.setOverlayEnabled(category, overlayName, overlayThemeTarget);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.evolution_settings_quicksettings);
}
