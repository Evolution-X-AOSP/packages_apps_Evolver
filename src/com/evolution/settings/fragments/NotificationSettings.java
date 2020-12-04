/*
 * Copyright (C) 2019-2020 The Evolution X Project
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.evolution.EvolutionUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.preference.SystemSettingSeekBarPreference;
import com.evolution.settings.preference.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class NotificationSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String AMBIENT_LIGHT_DURATION = "ambient_light_duration";
    private static final String AMBIENT_LIGHT_REPEAT_COUNT = "ambient_light_repeat_count";
    private static final String INCALL_VIB_OPTIONS = "incall_vib_options";
    private static final String KEY_AMBIENT = "ambient_notification_light_enabled";
    private static final String KEY_CHARGING_LIGHT = "charging_light";
    private static final String LED_CATEGORY = "led";
    private static final String NOTIFICATION_HEADERS = "notification_headers";
    private static final String NOTIFICATION_PULSE_COLOR = "ambient_notification_light_color";
    private static final String PULSE_COLOR_MODE_PREF = "ambient_notification_light_color_mode";

    private ColorPickerPreference mEdgeLightColorPreference;
    private ListPreference mColorMode;
    private Preference mChargingLeds;
    private PreferenceCategory mLedCategory;
    private SystemSettingSeekBarPreference mEdgeLightDurationPreference;
    private SystemSettingSeekBarPreference mEdgeLightRepeatCountPreference;
    private SystemSettingSwitchPreference mAmbientPref;
    private SystemSettingSwitchPreference mNotificationHeader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.evolution_settings_notifications);
        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefScreen = getPreferenceScreen();
        Resources res = getResources();

        boolean hasLED = res.getBoolean(
                com.android.internal.R.bool.config_hasNotificationLed);
        if (hasLED) {
            mChargingLeds = findPreference(KEY_CHARGING_LIGHT);
            if (mChargingLeds != null
                    && !res.getBoolean(
                            com.android.internal.R.bool.config_intrusiveBatteryLed)) {
                prefScreen.removePreference(mChargingLeds);
            }
        } else {
            mLedCategory = findPreference(LED_CATEGORY);
            mLedCategory.setVisible(false);
        }

        PreferenceCategory incallVibCategory = (PreferenceCategory) findPreference(INCALL_VIB_OPTIONS);
        if (!EvolutionUtils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(incallVibCategory);
        }

        mNotificationHeader = findPreference(NOTIFICATION_HEADERS);
        mNotificationHeader.setChecked((Settings.System.getInt(resolver,
                Settings.System.NOTIFICATION_HEADERS, 1) == 1));
        mNotificationHeader.setOnPreferenceChangeListener(this);

        mAmbientPref = (SystemSettingSwitchPreference) findPreference(KEY_AMBIENT);
        boolean aodEnabled = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.DOZE_ALWAYS_ON, 0, UserHandle.USER_CURRENT) == 1;
        if (!aodEnabled) {
            mAmbientPref.setChecked(false);
            mAmbientPref.setEnabled(false);
            mAmbientPref.setSummary(R.string.aod_disabled);
        }

        mEdgeLightRepeatCountPreference = (SystemSettingSeekBarPreference) findPreference(AMBIENT_LIGHT_REPEAT_COUNT);
        mEdgeLightRepeatCountPreference.setOnPreferenceChangeListener(this);
        int rCount = Settings.System.getInt(getContentResolver(),
                Settings.System.AMBIENT_LIGHT_REPEAT_COUNT, 0);
        mEdgeLightRepeatCountPreference.setValue(rCount);

        mEdgeLightDurationPreference = (SystemSettingSeekBarPreference) findPreference(AMBIENT_LIGHT_DURATION);
        mEdgeLightDurationPreference.setOnPreferenceChangeListener(this);
        int duration = Settings.System.getInt(getContentResolver(),
                Settings.System.AMBIENT_LIGHT_DURATION, 2);
        mEdgeLightDurationPreference.setValue(duration);

        mColorMode = (ListPreference) findPreference(PULSE_COLOR_MODE_PREF);
        int value;
        boolean colorModeAutomatic = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_COLOR_AUTOMATIC, 0) != 0;
        boolean colorModeAccent = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_ACCENT, 0) != 0;
        if (colorModeAutomatic) {
            value = 0;
        } else if (colorModeAccent) {
            value = 1;
        } else {
            value = 2;
        }

        mColorMode.setValue(Integer.toString(value));
        mColorMode.setSummary(mColorMode.getEntry());
        mColorMode.setOnPreferenceChangeListener(this);

        mEdgeLightColorPreference = (ColorPickerPreference) findPreference(NOTIFICATION_PULSE_COLOR);
        int edgeLightColor = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_COLOR, 0xFF1A73E8);
        mEdgeLightColorPreference.setNewPreviewColor(edgeLightColor);
        mEdgeLightColorPreference.setAlphaSliderEnabled(false);
        String edgeLightColorHex = String.format("#%08x", (0xFF1A73E8 & edgeLightColor));
        if (edgeLightColorHex.equals("#ff3980ff")) {
            mEdgeLightColorPreference.setSummary(R.string.color_default);
        } else {
            mEdgeLightColorPreference.setSummary(edgeLightColorHex);
        }
        mEdgeLightColorPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mNotificationHeader) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.NOTIFICATION_HEADERS, value ? 1 : 0);
            EvolutionUtils.showSystemUiRestartDialog(getContext());
            return true;
        } else if (preference == mEdgeLightColorPreference) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ff3980ff")) {
                preference.setSummary(R.string.color_default);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NOTIFICATION_PULSE_COLOR, intHex);
            return true;
        } else if (preference == mEdgeLightRepeatCountPreference) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.AMBIENT_LIGHT_REPEAT_COUNT, value);
            return true;
        } else if (preference == mEdgeLightDurationPreference) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.AMBIENT_LIGHT_DURATION, value);
            return true;
        } else if (preference == mColorMode) {
            int value = Integer.valueOf((String) newValue);
            int index = mColorMode.findIndexOfValue((String) newValue);
            mColorMode.setSummary(mColorMode.getEntries()[index]);
            if (value == 0) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NOTIFICATION_PULSE_COLOR_AUTOMATIC, 1);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NOTIFICATION_PULSE_ACCENT, 0);
            } else if (value == 1) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NOTIFICATION_PULSE_COLOR_AUTOMATIC, 0);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NOTIFICATION_PULSE_ACCENT, 1);
            } else {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NOTIFICATION_PULSE_COLOR_AUTOMATIC, 0);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.NOTIFICATION_PULSE_ACCENT, 0);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }

    /**
     * For Search.
     */

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.evolution_settings_notifications;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
}
