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

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.evolution.EvolutionUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.preference.CustomSeekBarPreference;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class PowerMenuSettings extends SettingsPreferenceFragment
                implements Preference.OnPreferenceChangeListener, Indexable {

    private static final String KEY_POWERMENU_LOCKSCREEN = "powermenu_lockscreen";
    private static final String KEY_POWERMENU_LS_REBOOT = "powermenu_ls_reboot";
    private static final String KEY_POWERMENU_LS_ADVANCED_REBOOT = "powermenu_ls_advanced_reboot";
    private static final String KEY_POWERMENU_LS_SCREENSHOT = "powermenu_ls_screenshot";
    private static final String KEY_POWERMENU_LS_SCREENRECORD = "powermenu_ls_screenrecord";
    private static final String KEY_POWERMENU_TORCH = "powermenu_torch";
    private static final String KEY_POWERMENU_LS_TORCH = "powermenu_ls_torch";

    private SwitchPreference mPowermenuTorch;
    private SwitchPreference mPowerMenuLockscreen;
    private SwitchPreference mPowerMenuReboot;
    private SwitchPreference mPowerMenuAdvancedReboot;
    private SwitchPreference mPowerMenuScreenshot;
    private SwitchPreference mPowerMenuScreenrecord;
    private SwitchPreference mPowerMenuLSTorch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.evolution_settings_power_menu);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mPowermenuTorch = findPreference(KEY_POWERMENU_TORCH);
        mPowermenuTorch.setOnPreferenceChangeListener(this);
        if (!EvolutionUtils.deviceHasFlashlight(getActivity())) {
            prefScreen.removePreference(mPowermenuTorch);
            prefScreen.removePreference(mPowerMenuLSTorch);
        } else {
        mPowermenuTorch.setChecked((Settings.System.getInt(resolver,
                Settings.System.POWERMENU_TORCH, 0) == 1));
        }

        mPowerMenuLockscreen = findPreference(KEY_POWERMENU_LOCKSCREEN);
        mPowerMenuLockscreen.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWERMENU_LOCKSCREEN, 1) == 1));
        mPowerMenuLockscreen.setOnPreferenceChangeListener(this);

        mPowerMenuReboot = findPreference(KEY_POWERMENU_LS_REBOOT);
        mPowerMenuReboot.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWERMENU_LS_REBOOT, 1) == 1));
        mPowerMenuReboot.setOnPreferenceChangeListener(this);

        mPowerMenuAdvancedReboot = findPreference(KEY_POWERMENU_LS_ADVANCED_REBOOT);
        mPowerMenuAdvancedReboot.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWERMENU_LS_ADVANCED_REBOOT, 1) == 1));
        mPowerMenuAdvancedReboot.setOnPreferenceChangeListener(this);

        mPowerMenuScreenshot = findPreference(KEY_POWERMENU_LS_SCREENSHOT);
        mPowerMenuScreenshot.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWERMENU_LS_SCREENSHOT, 0) == 1));
        mPowerMenuScreenshot.setOnPreferenceChangeListener(this);

        mPowerMenuScreenrecord = findPreference(KEY_POWERMENU_LS_SCREENRECORD);
        mPowerMenuScreenrecord.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWERMENU_LS_SCREENRECORD, 0) == 1));
        mPowerMenuScreenrecord.setOnPreferenceChangeListener(this);

        mPowerMenuLSTorch = findPreference(KEY_POWERMENU_LS_TORCH);
        mPowerMenuLSTorch.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWERMENU_LS_TORCH, 0) == 1));
        mPowerMenuLSTorch.setOnPreferenceChangeListener(this);

        updateLockscreen();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPowermenuTorch) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWERMENU_TORCH, value ? 1 : 0);
            return true;
        } else if (preference == mPowerMenuLockscreen) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWERMENU_LOCKSCREEN, value ? 1 : 0);
            updateLockscreen();
            return true;
        } else if (preference == mPowerMenuReboot) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWERMENU_LS_REBOOT, value ? 1 : 0);
            return true;
        } else if (preference == mPowerMenuAdvancedReboot) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWERMENU_LS_ADVANCED_REBOOT, value ? 1 : 0);
            return true;
        } else if (preference == mPowerMenuScreenshot) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWERMENU_LS_SCREENSHOT, value ? 1 : 0);
            return true;
        } else if (preference == mPowerMenuScreenrecord) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWERMENU_LS_SCREENRECORD, value ? 1 : 0);
            return true;
        } else if (preference == mPowerMenuLSTorch) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWERMENU_LS_TORCH, value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EVO_SETTINGS;
    }

    private void updateLockscreen() {
        boolean lockscreenOptions = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.POWERMENU_LOCKSCREEN, 1) == 1;

        if (lockscreenOptions) {
            mPowerMenuReboot.setEnabled(true);
            mPowerMenuAdvancedReboot.setEnabled(true);
            mPowerMenuScreenshot.setEnabled(true);
            mPowerMenuScreenrecord.setEnabled(true);
            mPowerMenuLSTorch.setEnabled(true);
        } else {
            mPowerMenuReboot.setEnabled(false);
            mPowerMenuAdvancedReboot.setEnabled(false);
            mPowerMenuScreenshot.setEnabled(false);
            mPowerMenuScreenrecord.setEnabled(false);
            mPowerMenuLSTorch.setEnabled(false);
        }
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.evolution_settings_power_menu;
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
