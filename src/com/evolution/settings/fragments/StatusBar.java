/*
 * Copyright (C) 2019-2022 Evolution X
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
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.fragments.Clock;
import com.evolution.settings.preference.SystemSettingListPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class StatusBar extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_NOTIFICATION_COUNT = "status_bar_notif_count";
    private static final String KEY_STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String KEY_STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String KEY_STATUS_BAR_BATTERY_TEXT_CHARGING = "status_bar_battery_text_charging";
    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";

    private static final int BATTERY_STYLE_PORTRAIT = 0;
    private static final int BATTERY_STYLE_TEXT = 4;
    private static final int BATTERY_STYLE_HIDDEN = 5;

    private SwitchPreference mBatteryTextCharging;
    private SystemSettingListPreference mBatteryPercent;
    private SystemSettingListPreference mBatteryStyle;
    private SystemSettingListPreference mStatusBarClock;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.evolution_settings_status_bar);

        final ContentResolver resolver = getActivity().getContentResolver();
        final Context mContext = getActivity().getApplicationContext();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        int batterystyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_PORTRAIT, UserHandle.USER_CURRENT);
        int batterypercent = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0, UserHandle.USER_CURRENT);

        mBatteryStyle = (SystemSettingListPreference) findPreference(KEY_STATUS_BAR_BATTERY_STYLE);
        mBatteryStyle.setOnPreferenceChangeListener(this);

        mBatteryPercent = (SystemSettingListPreference) findPreference(KEY_STATUS_BAR_SHOW_BATTERY_PERCENT);
        mBatteryPercent.setEnabled(
                batterystyle != BATTERY_STYLE_TEXT && batterystyle != BATTERY_STYLE_HIDDEN);
        mBatteryPercent.setOnPreferenceChangeListener(this);

        mBatteryTextCharging = (SwitchPreference) findPreference(KEY_STATUS_BAR_BATTERY_TEXT_CHARGING);
        mBatteryTextCharging.setEnabled(batterystyle == BATTERY_STYLE_HIDDEN ||
                (batterystyle != BATTERY_STYLE_TEXT && batterypercent != 2));

        mStatusBarClock =
                (SystemSettingListPreference) findPreference(STATUS_BAR_CLOCK_STYLE);

        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_rtl);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_rtl);
        }

        // Get config_statusBarShowNumber from SystemUI
        Context sysUiContext;
        try {
            sysUiContext = getActivity().createPackageContext(SYSTEMUI_PACKAGE,
                    Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
        } catch (NameNotFoundException e) {
            // Nothing to do, If SystemUI was not found you have bigger issues :)
            sysUiContext = getActivity();
        }
        Resources sysUiRes = sysUiContext.getResources();
        final int resId = sysUiRes.getIdentifier("config_statusBarShowNumber", "bool", SYSTEMUI_PACKAGE);
        final boolean isDefaultEnabled = sysUiRes.getBoolean(resId);

        SwitchPreference notifCountPref = findPreference(KEY_NOTIFICATION_COUNT);
        enabled = Settings.System.getIntForUser(resolver,
                KEY_NOTIFICATION_COUNT, isDefaultEnabled ? 1 : 0,
                UserHandle.USER_CURRENT) == 1;
        notifCountPref.setChecked(enabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryStyle) {
            int value = Integer.parseInt((String) newValue);
            int batterypercent = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0, UserHandle.USER_CURRENT);
            mBatteryPercent.setEnabled(
                    value != BATTERY_STYLE_TEXT && value != BATTERY_STYLE_HIDDEN);
            mBatteryTextCharging.setEnabled(value == BATTERY_STYLE_HIDDEN ||
                    (value != BATTERY_STYLE_TEXT && batterypercent != 2));
            return true;
        } else if (preference == mBatteryPercent) {
            int value = Integer.parseInt((String) newValue);
            int batterystyle = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_PORTRAIT, UserHandle.USER_CURRENT);
            mBatteryTextCharging.setEnabled(batterystyle == BATTERY_STYLE_HIDDEN ||
                    (batterystyle != BATTERY_STYLE_TEXT && value != 2));
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.evolution_settings_status_bar);
}
