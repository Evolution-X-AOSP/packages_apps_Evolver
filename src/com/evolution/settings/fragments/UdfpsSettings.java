/*
 * Copyright (C) 2017-2022 The Project-Xtended
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

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.evolution.EvolutionUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.preference.SystemSettingListPreference;
import com.evolution.settings.preference.SystemSettingSwitchPreference;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class UdfpsSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private boolean mShowUdfpsPressedColor;
    private boolean mShowUdfpsScreenOff;

    private static final String UDFPS_CUSTOMIZATION = "udfps_customization";
    private static final String UDFPS_PRESSED_COLOR = "fod_color";
    private static final String UDFPS_SCREEN_OFF = "screen_off_fod";

    private static final int REQUEST_PICK_IMAGE = 0;

    private PreferenceCategory mUdfpsCustomization;
    private SystemSettingListPreference mUdfpsPressedColor;
    private SystemSettingSwitchPreference mUdfpsScreenOff;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.evolution_settings_udfps);

        final PreferenceScreen prefSet = getPreferenceScreen();
        Resources resources = getResources();

        final boolean udfpsResPkgInstalled = EvolutionUtils.isPackageInstalled(getContext(),
                "com.evolution.udfps.resources");
	mUdfpsCustomization = (PreferenceCategory) findPreference(UDFPS_CUSTOMIZATION);
        if (!udfpsResPkgInstalled) {
            prefSet.removePreference(mUdfpsCustomization);
        }

        mShowUdfpsPressedColor = getContext().getResources().getBoolean(R.bool.config_show_fod_pressed_color_settings);
        mUdfpsPressedColor = (SystemSettingListPreference) findPreference(UDFPS_PRESSED_COLOR);
        if (!mShowUdfpsPressedColor) {
            prefSet.removePreference(mUdfpsPressedColor);
        }

        mShowUdfpsScreenOff = getContext().getResources().getBoolean(R.bool.config_supportScreenOffFod);
        mUdfpsScreenOff = findPreference(UDFPS_SCREEN_OFF);
        if (!mShowUdfpsScreenOff) {
            prefSet.removePreference(mUdfpsScreenOff);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EVOLVER;
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.evolution_settings_udfps);
}
