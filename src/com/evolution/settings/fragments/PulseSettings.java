/*
 * Copyright (C) 2020 The Dirty Unicorns Project
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

import java.util.ArrayList;
import java.util.List;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.evolution.EvolutionUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.preference.ColorSelectPreference;

import android.app.ActionBar;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.UserHandle;

import androidx.preference.PreferenceCategory;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import android.provider.SearchIndexableResource;
import android.provider.Settings;

@SearchIndexable
public class PulseSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = PulseSettings.class.getSimpleName();
    private static final String NAVBAR_PULSE_ENABLED_KEY = "navbar_pulse_enabled";
    private static final String LOCKSCREEN_PULSE_ENABLED_KEY = "lockscreen_pulse_enabled";
    private static final String PULSE_SMOOTHING_KEY = "pulse_smoothing_enabled";
    private static final String PULSE_COLOR_MODE_KEY = "pulse_color_mode";
    private static final String PULSE_COLOR_MODE_CHOOSER_KEY = "pulse_color_user";
    private static final String PULSE_COLOR_MODE_LAVA_SPEED_KEY = "pulse_lavalamp_speed";
    private static final String PULSE_RENDER_CATEGORY_SOLID = "pulse_2";
    private static final String PULSE_RENDER_CATEGORY_FADING = "pulse_fading_bars_category";
    private static final String PULSE_RENDER_MODE_KEY = "pulse_render_style";
    private static final int RENDER_STYLE_FADING_BARS = 0;
    private static final int RENDER_STYLE_SOLID_LINES = 1;
    private static final int COLOR_TYPE_ACCENT = 0;
    private static final int COLOR_TYPE_USER = 1;
    private static final int COLOR_TYPE_LAVALAMP = 2;
    private static final int COLOR_TYPE_AUTO = 3;

    private SwitchPreference mNavbarPulse;
    private SwitchPreference mLockscreenPulse;
    private SwitchPreference mPulseSmoothing;
    private Preference mRenderMode;
    private ListPreference mColorModePref;
    private ColorSelectPreference mColorPickerPref;
    private Preference mLavaSpeedPref;
    private int mColorPref;
    private int mColorAccent;

    private PreferenceCategory mFadingBarsCat;
    private PreferenceCategory mSolidBarsCat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.evolution_settings_pulse);

        mFooterPreferenceMixin.createFooterPreference()
                .setTitle(R.string.pulse_help_policy_notice_summary);

        ContentResolver resolver = getContentResolver();

        mNavbarPulse = findPreference(NAVBAR_PULSE_ENABLED_KEY);
        boolean navbarPulse = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.NAVBAR_PULSE_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        mNavbarPulse.setChecked(navbarPulse);
        mNavbarPulse.setOnPreferenceChangeListener(this);

        mLockscreenPulse = findPreference(LOCKSCREEN_PULSE_ENABLED_KEY);
        boolean lockscreenPulse = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_PULSE_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        mLockscreenPulse.setChecked(lockscreenPulse);
        mLockscreenPulse.setOnPreferenceChangeListener(this);

        mColorModePref = findPreference(PULSE_COLOR_MODE_KEY);
        mLavaSpeedPref = findPreference(PULSE_COLOR_MODE_LAVA_SPEED_KEY);
        mColorPickerPref = findPreference(PULSE_COLOR_MODE_CHOOSER_KEY);
        mColorModePref.setOnPreferenceChangeListener(this);
        int colorMode = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_COLOR_MODE, COLOR_TYPE_ACCENT, UserHandle.USER_CURRENT);
        updateColorPrefs(colorMode);

        mRenderMode = findPreference(PULSE_RENDER_MODE_KEY);
        mRenderMode.setOnPreferenceChangeListener(this);
        mFadingBarsCat = findPreference(PULSE_RENDER_CATEGORY_FADING);
        mSolidBarsCat = findPreference(PULSE_RENDER_CATEGORY_SOLID);

        mPulseSmoothing = findPreference(PULSE_SMOOTHING_KEY);

        mColorAccent = (EvolutionUtils.getThemeAccentColor(getContext()));
        mColorPref = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_COLOR_USER, mColorAccent,
                        UserHandle.USER_CURRENT);
        mColorPickerPref.setColor(mColorPref);
        mColorPickerPref.setOnPreferenceChangeListener(this);

        updateAllPrefs();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getContext().getContentResolver();
        if (preference == mNavbarPulse) {
            boolean val = (Boolean) newValue;
            Settings.Secure.putIntForUser(resolver,
                Settings.Secure.NAVBAR_PULSE_ENABLED, val ? 1 : 0, UserHandle.USER_CURRENT);
            updateAllPrefs();
            return true;
        } else if (preference == mLockscreenPulse) {
            boolean val = (Boolean) newValue;
            Settings.Secure.putIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_PULSE_ENABLED, val ? 1 : 0, UserHandle.USER_CURRENT);
            updateAllPrefs();
            return true;
        } else if (preference == mColorModePref) {
            updateColorPrefs(Integer.valueOf(String.valueOf(newValue)));
            return true;
        } else if (preference == mRenderMode) {
            updateRenderCategories(Integer.valueOf(String.valueOf(newValue)));
            return true;
        } else if (preference.equals(mColorPickerPref)) {
            ColorSelectPreference colorPref = (ColorSelectPreference) preference;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_COLOR_USER, colorPref.getColor(),
                    UserHandle.USER_CURRENT);
            mColorPref = colorPref.getColor();
            mColorPickerPref.setColor(mColorPref);
            return true;
        }
        return false;
    }

    private void updateAllPrefs() {
        ContentResolver resolver = getContentResolver();

        boolean navbarPulse = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.NAVBAR_PULSE_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        boolean lockscreenPulse = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_PULSE_ENABLED, 0, UserHandle.USER_CURRENT) != 0;

        mPulseSmoothing.setEnabled(navbarPulse || lockscreenPulse);

        mColorModePref.setEnabled(navbarPulse || lockscreenPulse);
        if (navbarPulse || lockscreenPulse) {
            int colorMode = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.PULSE_COLOR_MODE, COLOR_TYPE_ACCENT, UserHandle.USER_CURRENT);
            updateColorPrefs(colorMode);
        } else {
            mColorPickerPref.setEnabled(false);
            mLavaSpeedPref.setEnabled(false);
        }

        mRenderMode.setEnabled(navbarPulse || lockscreenPulse);
        if (navbarPulse || lockscreenPulse) {
            int renderMode = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.PULSE_RENDER_STYLE, RENDER_STYLE_SOLID_LINES, UserHandle.USER_CURRENT);
            updateRenderCategories(renderMode);
        } else {
            mFadingBarsCat.setEnabled(false);
            mSolidBarsCat.setEnabled(false);
        }
    }

    private void updateColorPrefs(int val) {
        switch (val) {
            case COLOR_TYPE_ACCENT:
                mColorPickerPref.setEnabled(false);
                mLavaSpeedPref.setEnabled(false);
                break;
            case COLOR_TYPE_USER:
                mColorPickerPref.setEnabled(true);
                mLavaSpeedPref.setEnabled(false);
                break;
            case COLOR_TYPE_LAVALAMP:
                mColorPickerPref.setEnabled(false);
                mLavaSpeedPref.setEnabled(true);
                break;
            case COLOR_TYPE_AUTO:
                mColorPickerPref.setEnabled(false);
                mLavaSpeedPref.setEnabled(false);
                break;
        }
    }

    private void updateRenderCategories(int mode) {
        mFadingBarsCat.setEnabled(mode == RENDER_STYLE_FADING_BARS);
        mSolidBarsCat.setEnabled(mode == RENDER_STYLE_SOLID_LINES);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EVO_SETTINGS;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                boolean enabled) {
            final ArrayList<SearchIndexableResource> result = new ArrayList<>();
            final SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.evolution_settings_pulse;
            result.add(sir);
            return result;
        }

        @Override
        public List<String> getNonIndexableKeys(Context context) {
            final List<String> keys = super.getNonIndexableKeys(context);
            return keys;
        }
    };
}
