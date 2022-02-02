/*
 * Copyright (C) 2019-2022 The Evolution X Project
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
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.evolution.EvolutionUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.preference.SecureSettingSwitchPreference;
import com.evolution.settings.preference.SystemSettingSeekBarPreference;
import com.evolution.settings.preference.SystemSettingSwitchPreference;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class StatusbarSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String COMBINED_STATUSBAR_ICONS = "show_combined_status_bar_signal_icons";
    private static final String CONFIG_RESOURCE_NAME = "flag_combined_status_bar_signal_icons";
    private static final String KEY_VOLTE_ICON_STYLE = "volte_icon_style";
    private static final String KEY_VOWIFI_ICON_STYLE = "vowifi_icon_style";
    private static final String KEY_SHOW_ROAMING = "roaming_indicator_icon";
    private static final String KEY_SHOW_FOURG = "show_fourg_icon";
    private static final String KEY_SHOW_DATA_DISABLED = "data_disabled_icon";
    private static final String KEY_USE_OLD_MOBILETYPE = "use_old_mobiletype";
    private static final String SYSTEMUI_PACKAGE = "com.android.systemui";
    private static final String PREF_STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String PREF_STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String LEFT_BATTERY_TEXT = "do_left_battery_text";

    private static final int BATTERY_STYLE_PORTRAIT = 0;
    private static final int BATTERY_STYLE_TEXT = 4;
    private static final int BATTERY_STYLE_HIDDEN = 5;
    private static final int BATTERY_PERCENT_HIDDEN = 0;
    //private static final int BATTERY_PERCENT_SHOW_INSIDE = 1;
    //private static final int BATTERY_PERCENT_SHOW_OUTSIDE = 2;

    private ListPreference mBatteryPercent;
    private ListPreference mBatteryStyle;
    private SwitchPreference mShowRoaming;
    private SwitchPreference mShowFourg;
    private SwitchPreference mDataDisabled;
    private SwitchPreference mOldMobileType;
    private SystemSettingSeekBarPreference mVolteIconStyle;
    private SystemSettingSeekBarPreference mVowifiIconStyle;
    private SecureSettingSwitchPreference mCombinedIcons;
    private SystemSettingSwitchPreference mLeftBatteryText;
    private int mBatteryPercentValue;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.evolution_settings_status_bar);

        final ContentResolver resolver = getActivity().getContentResolver();
        final Context mContext = getActivity().getApplicationContext();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mCombinedIcons = (SecureSettingSwitchPreference)
                findPreference(COMBINED_STATUSBAR_ICONS);
        Resources sysUIRes = null;
        boolean def = false;
        int resId = 0;
        try {
            sysUIRes = getActivity().getPackageManager()
                    .getResourcesForApplication(SYSTEMUI_PACKAGE);
        } catch (Exception ignored) {
            // If you don't have system UI you have bigger issues
        }
        if (sysUIRes != null) {
            resId = sysUIRes.getIdentifier(
                    CONFIG_RESOURCE_NAME, "bool", SYSTEMUI_PACKAGE);
            if (resId != 0) def = sysUIRes.getBoolean(resId);
        }
        boolean enabled = Settings.Secure.getInt(resolver,
                COMBINED_STATUSBAR_ICONS, def ? 1 : 0) == 1;
        mCombinedIcons.setChecked(enabled);
        mCombinedIcons.setOnPreferenceChangeListener(this);

        int batterystyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_PORTRAIT, UserHandle.USER_CURRENT);

        mBatteryStyle = (ListPreference) findPreference(PREF_STATUS_BAR_BATTERY_STYLE);
        mBatteryStyle.setValue(String.valueOf(batterystyle));
        mBatteryStyle.setSummary(mBatteryStyle.getEntry());
        mBatteryStyle.setOnPreferenceChangeListener(this);

        mBatteryPercentValue = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, BATTERY_PERCENT_HIDDEN, UserHandle.USER_CURRENT);
        mBatteryPercent = (ListPreference) findPreference(PREF_STATUS_BAR_SHOW_BATTERY_PERCENT);
        mBatteryPercent.setValue(String.valueOf(mBatteryPercentValue));
        mBatteryPercent.setSummary(mBatteryPercent.getEntry());
        mBatteryPercent.setOnPreferenceChangeListener(this);
        mBatteryPercent.setEnabled(
                batterystyle != BATTERY_STYLE_TEXT && batterystyle != BATTERY_STYLE_HIDDEN);

        mLeftBatteryText = (SystemSettingSwitchPreference) findPreference(LEFT_BATTERY_TEXT);
        mLeftBatteryText.setEnabled(
                batterystyle != BATTERY_STYLE_TEXT && batterystyle != BATTERY_STYLE_HIDDEN);

        mVolteIconStyle = (SystemSettingSeekBarPreference) findPreference(KEY_VOLTE_ICON_STYLE);
        mVowifiIconStyle = (SystemSettingSeekBarPreference) findPreference(KEY_VOWIFI_ICON_STYLE);
        mShowRoaming = (SwitchPreference) findPreference(KEY_SHOW_ROAMING);
        mShowFourg = (SwitchPreference) findPreference(KEY_SHOW_FOURG);
        mDataDisabled = (SwitchPreference) findPreference(KEY_SHOW_DATA_DISABLED);
        mOldMobileType = (SwitchPreference) findPreference(KEY_USE_OLD_MOBILETYPE);

        if (!EvolutionUtils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(mVolteIconStyle);
            prefScreen.removePreference(mVowifiIconStyle);
            prefScreen.removePreference(mShowRoaming);
            prefScreen.removePreference(mShowFourg);
            prefScreen.removePreference(mDataDisabled);
            prefScreen.removePreference(mOldMobileType);
        } else {
            boolean mConfigUseOldMobileType = mContext.getResources().getBoolean(
                    com.android.internal.R.bool.config_useOldMobileIcons);
            boolean showing = Settings.System.getIntForUser(resolver,
                    Settings.System.USE_OLD_MOBILETYPE,
                    mConfigUseOldMobileType ? 1 : 0, UserHandle.USER_CURRENT) != 0;
            mOldMobileType.setChecked(showing);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryStyle) {
            int batterystyle = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_BATTERY_STYLE, batterystyle,
                UserHandle.USER_CURRENT);
            int index = mBatteryStyle.findIndexOfValue((String) newValue);
            mBatteryStyle.setSummary(mBatteryStyle.getEntries()[index]);
            mBatteryPercent.setEnabled(
                    batterystyle != BATTERY_STYLE_TEXT && batterystyle != BATTERY_STYLE_HIDDEN);
            mLeftBatteryText.setEnabled(
                    batterystyle != BATTERY_STYLE_TEXT && batterystyle != BATTERY_STYLE_HIDDEN);
            return true;
        } else if (preference == mBatteryPercent) {
            mBatteryPercentValue = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, mBatteryPercentValue,
                    UserHandle.USER_CURRENT);
            int index = mBatteryPercent.findIndexOfValue((String) newValue);
            mBatteryPercent.setSummary(mBatteryPercent.getEntries()[index]);
            return true;
        } else if (preference == mCombinedIcons) {
            boolean enabled = (boolean) newValue;
            Settings.Secure.putInt(resolver,
                    COMBINED_STATUSBAR_ICONS, enabled ? 1 : 0);
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

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.evolution_settings_status_bar);
}
