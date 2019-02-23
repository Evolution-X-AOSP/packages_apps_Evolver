/*
 * Copyright (C) 2013 Android Open Kang Project
 * Copyright (C) 2017 faust93 at monumentum@gmail.com
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

import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.preference.CustomSeekBarPreference;

import java.util.ArrayList;
import java.util.List;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

@SearchIndexable
public class BatteryBarSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, Indexable {

    private static final String PREF_BATT_BAR = "battery_bar_list";
    private static final String PREF_BATT_BAR_STYLE = "battery_bar_style";
    private static final String PREF_BATT_BAR_COLOR = "battery_bar_color";
    private static final String PREF_BATT_BAR_CHARGING_COLOR = "battery_bar_charging_color";
    private static final String PREF_BATT_BAR_LOW_COLOR_WARNING = "battery_bar_battery_low_color_warning";
    private static final String PREF_BATT_BAR_USE_GRADIENT_COLOR = "battery_bar_use_gradient_color";
    private static final String PREF_BATT_BAR_LOW_COLOR = "battery_bar_low_color";
    private static final String PREF_BATT_BAR_HIGH_COLOR = "battery_bar_high_color";
    private static final String PREF_BATT_BAR_WIDTH = "battery_bar_thickness";
    private static final String PREF_BATT_ANIMATE = "battery_bar_animate";

    private Context mContext;

    private ListPreference mBatteryBar;
    private ListPreference mBatteryBarStyle;
    private CustomSeekBarPreference mBatteryBarThickness;
    private SwitchPreference mBatteryBarChargingAnimation;
    private SwitchPreference mBatteryBarUseGradient;
    private ColorPickerPreference mBatteryBarColor;
    private ColorPickerPreference mBatteryBarChargingColor;
    private ColorPickerPreference mBatteryBarBatteryLowColor;
    private ColorPickerPreference mBatteryBarBatteryLowColorWarn;
    private ColorPickerPreference mBatteryBarBatteryHighColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.evolution_settings_battery_bar);

        mContext = (Context) getActivity();
        final ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefSet = getPreferenceScreen();

        mBatteryBar = findPreference(PREF_BATT_BAR);
        mBatteryBar.setOnPreferenceChangeListener(this);
        boolean enabled = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_LOCATION, 0) != 0;
        if (enabled) {
            mBatteryBar.setValue((Settings.System.getInt(resolver, Settings.System.BATTERY_BAR_LOCATION, 0)) + "");
            mBatteryBar.setSummary(mBatteryBar.getEntry());
        } else {
            mBatteryBar.setEnabled(false);
            mBatteryBar.setSummary(R.string.enable_first);
        }

        mBatteryBarStyle = findPreference(PREF_BATT_BAR_STYLE);
        mBatteryBarStyle.setOnPreferenceChangeListener(this);
        mBatteryBarStyle.setValue((Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_STYLE, 0)) + "");
        mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntry());

        mBatteryBarColor = prefSet.findPreference(PREF_BATT_BAR_COLOR);
        mBatteryBarColor.setNewPreviewColor(Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_COLOR, Color.WHITE));
        mBatteryBarColor.setOnPreferenceChangeListener(this);

        mBatteryBarChargingColor = prefSet.findPreference(PREF_BATT_BAR_CHARGING_COLOR);
        mBatteryBarChargingColor.setNewPreviewColor(Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_CHARGING_COLOR, Color.WHITE));
        mBatteryBarChargingColor.setOnPreferenceChangeListener(this);

        mBatteryBarBatteryLowColorWarn = prefSet.findPreference(PREF_BATT_BAR_LOW_COLOR_WARNING);
        mBatteryBarBatteryLowColorWarn.setNewPreviewColor(Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_BATTERY_LOW_COLOR_WARNING,Color.WHITE));
        mBatteryBarBatteryLowColorWarn.setOnPreferenceChangeListener(this);

        mBatteryBarBatteryLowColor = prefSet.findPreference(PREF_BATT_BAR_LOW_COLOR);
        mBatteryBarBatteryLowColor.setNewPreviewColor(Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_LOW_COLOR, Color.WHITE));
        mBatteryBarBatteryLowColor.setOnPreferenceChangeListener(this);

        mBatteryBarBatteryHighColor = prefSet.findPreference(PREF_BATT_BAR_HIGH_COLOR);
        mBatteryBarBatteryHighColor.setNewPreviewColor(Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_HIGH_COLOR, Color.WHITE));
        mBatteryBarBatteryHighColor.setOnPreferenceChangeListener(this);

        mBatteryBarUseGradient = findPreference(PREF_BATT_BAR_USE_GRADIENT_COLOR);
        mBatteryBarUseGradient.setChecked(Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_USE_GRADIENT_COLOR, 0) == 1);

        mBatteryBarChargingAnimation = findPreference(PREF_BATT_ANIMATE);
        mBatteryBarChargingAnimation.setChecked(Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_ANIMATE, 0) == 1);

        mBatteryBarThickness = prefSet.findPreference(PREF_BATT_BAR_WIDTH);
        mBatteryBarThickness.setValue(Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_THICKNESS, 1));
        mBatteryBarThickness.setOnPreferenceChangeListener(this);

        updateBatteryBarOptions();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryBarColor) {
            int intHex = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBarChargingColor) {
            int intHex = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_CHARGING_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBarBatteryLowColor) {
            int intHex = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_LOW_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBarBatteryLowColorWarn) {
            int intHex = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_BATTERY_LOW_COLOR_WARNING, intHex);
            return true;
        } else if (preference == mBatteryBarBatteryHighColor) {
            int intHex = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_HIGH_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBar) {
            int val = Integer.parseInt((String) newValue);
            int index = mBatteryBar.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_LOCATION, val);
            mBatteryBar.setSummary(mBatteryBar.getEntries()[index]);
            updateBatteryBarOptions();
            return true;
        } else if (preference == mBatteryBarStyle) {
            int val = Integer.parseInt((String) newValue);
            int index = mBatteryBarStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_STYLE, val);
            mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntries()[index]);
            return true;
        } else if (preference == mBatteryBarThickness) {
            int val = (Integer) newValue;
            Settings.System.putInt(resolver,
                Settings.System.BATTERY_BAR_THICKNESS, val);
            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryBarChargingAnimation) {
            boolean value = mBatteryBarChargingAnimation.isChecked();
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_ANIMATE, value ? 1 : 0);
            return true;
         } else if (preference == mBatteryBarUseGradient) {
            boolean value = mBatteryBarUseGradient.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.BATTERY_BAR_USE_GRADIENT_COLOR, value ? 1 : 0);
            return true;
        }
        return false;
    }

    private void updateBatteryBarOptions() {
        boolean enabled = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.BATTERY_BAR_LOCATION, 0) != 0;
        mBatteryBarStyle.setEnabled(enabled);
        mBatteryBarThickness.setEnabled(enabled);
        mBatteryBarChargingAnimation.setEnabled(enabled);
        mBatteryBarColor.setEnabled(enabled);
        mBatteryBarChargingColor.setEnabled(enabled);
        mBatteryBarUseGradient.setEnabled(enabled);
        mBatteryBarBatteryLowColor.setEnabled(enabled);
        mBatteryBarBatteryHighColor.setEnabled(enabled);
        mBatteryBarBatteryLowColorWarn.setEnabled(enabled);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVO_SETTINGS;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.evolution_settings_battery_bar;
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
