/*
 * Copyright (C) 2016-2021 crDroid Android Project
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
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

@SearchIndexable
public class BatteryBar extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String PREF_BATT_BAR = "statusbar_battery_bar";

    private SwitchPreference mBatteryBar;

    private boolean mIsBarSwitchingMode = false;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.evolution_settings_battery_bar);

        final ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int intColor;
        String hexColor;

        mBatteryBar = (SwitchPreference) findPreference(PREF_BATT_BAR);
        mHandler = new Handler();

        boolean showing = Settings.System.getIntForUser(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR, 0, UserHandle.USER_CURRENT) != 0;
        mBatteryBar.setChecked(showing);
        mBatteryBar.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryBar) {
            if (mIsBarSwitchingMode) {
                return false;
            }
            mIsBarSwitchingMode = true;
            boolean value = ((Boolean)newValue);
            Settings.System.putIntForUser(resolver, Settings.System.STATUSBAR_BATTERY_BAR,
                    value ? 1 : 0, UserHandle.USER_CURRENT);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsBarSwitchingMode = false;
                    boolean showing = Settings.System.getIntForUser(resolver,
                            Settings.System.STATUSBAR_BATTERY_BAR, 0, UserHandle.USER_CURRENT) != 0;
                    mBatteryBar.setChecked(showing);
                }
            }, 1500);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.evolution_settings_battery_bar);
}
