/*
 * Copyright (C) 2019 The Evolution X Project
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
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.evolution.settings.preference.SecureSettingSeekBarPreference;
import com.evolution.settings.preference.SecureSettingSwitchPreference;
import com.evolution.settings.preference.SystemSettingSwitchPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class VisualizerSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String LOCKSCREEN_VISUALIZER_ENABLED = "lockscreen_visualizer_enabled";
    private static final String KEY_AMBIENT_VIS = "ambient_visualizer_enabled";
    private static final String KEY_LAVALAMP = "lockscreen_lavalamp_enabled";
    private static final String KEY_LAVALAMP_SPEED = "lockscreen_lavalamp_speed";
    private static final String KEY_AUTOCOLOR = "lockscreen_visualizer_autocolor";
    private static final String KEY_SOLID_UNITS = "lockscreen_solid_units_count";
    private static final String KEY_FUDGE_FACTOR = "lockscreen_solid_fudge_factor";
    private static final String KEY_OPACITY = "lockscreen_solid_units_opacity";
    private static final String KEY_COLOR = "lockscreen_visualizer_color";

    private static final int DEFAULT_COLOR = 0xffffffff;

    private ColorPickerPreference mColor;
    private SecureSettingSeekBarPreference mFudgeFactor;
    private SecureSettingSeekBarPreference mOpacity;
    private SecureSettingSeekBarPreference mSolidUnits;
    private SecureSettingSwitchPreference mAutoColor;
    private SecureSettingSwitchPreference mLavaLamp;
    private SecureSettingSwitchPreference mVisualizerEnabled;
    private SystemSettingSwitchPreference mAmbientVisualizer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.evolution_settings_visualizer);

        ContentResolver resolver = getActivity().getContentResolver();

        boolean mLavaLampEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, 1) != 0;
        boolean mAutoColorEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_VISUALIZER_AUTOCOLOR, 1) != 0;

        mAutoColor = (SecureSettingSwitchPreference) findPreference(KEY_AUTOCOLOR);
        mAutoColor.setOnPreferenceChangeListener(this);
        mAutoColor.setChecked(mAutoColorEnabled);

        if (mLavaLampEnabled) {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_lavalamp));
        } else {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_summary));
        }

        mLavaLamp = (SecureSettingSwitchPreference) findPreference(KEY_LAVALAMP);
        mLavaLamp.setOnPreferenceChangeListener(this);

        mSolidUnits = (SecureSettingSeekBarPreference) findPreference(KEY_SOLID_UNITS);
        mSolidUnits.setOnPreferenceChangeListener(this);
        mSolidUnits.setValue(Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_SOLID_UNITS_COUNT, 32));

        mFudgeFactor = (SecureSettingSeekBarPreference) findPreference(KEY_FUDGE_FACTOR);
        mFudgeFactor.setOnPreferenceChangeListener(this);
        mFudgeFactor.setValue(Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_SOLID_FUDGE_FACTOR, 16));

        mOpacity = (SecureSettingSeekBarPreference) findPreference(KEY_OPACITY);
        mOpacity.setOnPreferenceChangeListener(this);
        mOpacity.setValue(Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_SOLID_UNITS_OPACITY, 140));

        mColor = (ColorPickerPreference) findPreference(KEY_COLOR);
        mColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCKSCREEN_VISUALIZER_COLOR, DEFAULT_COLOR);
        String hexColor = String.format("#%08x", (DEFAULT_COLOR & intColor));
        mColor.setSummary(hexColor);
        mColor.setNewPreviewColor(intColor);

        mVisualizerEnabled = (SecureSettingSwitchPreference) findPreference(LOCKSCREEN_VISUALIZER_ENABLED);
        mVisualizerEnabled.setOnPreferenceChangeListener(this);
        int visualizerEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, 0);
        mVisualizerEnabled.setChecked(visualizerEnabled != 0);

        UpdateEnablement(resolver);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mVisualizerEnabled) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, value ? 1 : 0);
            UpdateEnablement(resolver);
            return true;
        } else if (preference == mLavaLamp) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, value ? 1 : 0);
            if (value) {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_lavalamp));
            } else {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_summary));
            }
            UpdateEnablement(resolver);
            return true;
        } else if (preference == mAutoColor) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_VISUALIZER_AUTOCOLOR, value ? 1 : 0);
            UpdateEnablement(resolver);
            return true;
        } else if (preference == mSolidUnits) {
            int value = (int) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_SOLID_UNITS_COUNT, value);
            return true;
        } else if (preference == mFudgeFactor) {
            int value = (int) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_SOLID_FUDGE_FACTOR, value);
            return true;
        } else if (preference == mOpacity) {
            int value = (int) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_SOLID_UNITS_OPACITY, value);
            return true;
        } else if (preference == mColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.Secure.putInt(resolver,
                    Settings.Secure.LOCKSCREEN_VISUALIZER_COLOR, intHex);
            return true;
        }
        return false;
    }

    // Updates enablement of lockscreen visualizer toggles
    private void UpdateEnablement(ContentResolver resolver) {
        boolean mLavaLampEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, 1) != 0;
        boolean mAutoColorEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_VISUALIZER_AUTOCOLOR, 1) != 0;
        boolean visualizerEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, 0) != 0;
        mLavaLamp.setEnabled(visualizerEnabled);
        mAutoColor.setEnabled(visualizerEnabled && !mLavaLampEnabled);
        mSolidUnits.setEnabled(visualizerEnabled);
        mFudgeFactor.setEnabled(visualizerEnabled);
        mOpacity.setEnabled(visualizerEnabled);
        mColor.setEnabled(visualizerEnabled && !mAutoColorEnabled && !mLavaLampEnabled);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EVO_SETTINGS;
    }
}
