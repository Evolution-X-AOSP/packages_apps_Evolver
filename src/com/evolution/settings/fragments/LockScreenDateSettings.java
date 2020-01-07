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

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.evolution.settings.preference.SecureSettingMasterSwitchPreference;
import com.evolution.settings.preference.SystemSettingSeekBarPreference;

public class LockScreenDateSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String DATE_FONT_SIZE = "lockdate_font_size";
    private static final String LOCK_DATE_FONTS = "lock_date_fonts";

    private ListPreference mLockDateFonts;
    private SystemSettingSeekBarPreference mDateFontSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.evolution_settings_lockscreen_date);

        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getResources();

        // Lockscreen Date Fonts
        mLockDateFonts = (ListPreference) findPreference(LOCK_DATE_FONTS);
        mLockDateFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_DATE_FONTS, 28)));
        mLockDateFonts.setSummary(mLockDateFonts.getEntry());
        mLockDateFonts.setOnPreferenceChangeListener(this);

        // Lockscreen Date Size
        mDateFontSize = (SystemSettingSeekBarPreference) findPreference(DATE_FONT_SIZE);
        mDateFontSize.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKDATE_FONT_SIZE, 18));
        mDateFontSize.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLockDateFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_DATE_FONTS,
                    Integer.valueOf((String) newValue));
            mLockDateFonts.setValue(String.valueOf(newValue));
            mLockDateFonts.setSummary(mLockDateFonts.getEntry());
            return true;
        } else if (preference == mDateFontSize) {
            int top = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKDATE_FONT_SIZE, top*1);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EVO_SETTINGS;
    }
}
