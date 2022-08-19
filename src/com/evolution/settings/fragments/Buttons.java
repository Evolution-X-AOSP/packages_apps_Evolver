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
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.lineage.hardware.LineageHardwareManager;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.evolution.EvolutionUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.preference.SecureSettingSwitchPreference;
import com.evolution.settings.preference.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class Buttons extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_NAVBAR_INVERSE = "navigation_bar_inverse";
    private static final String KEY_NAVIGATION_COMPACT_LAYOUT = "navigation_bar_compact_layout";
    private static final String KEY_SWAP_CAPACITIVE_KEYS = "swap_capacitive_keys";

    private SecureSettingSwitchPreference mSwapCapacitiveKeys;
    private SystemSettingSwitchPreference mNavbarInverse;
    private SystemSettingSwitchPreference mNavigationCompactLayout;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.evolution_settings_buttons);

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mSwapCapacitiveKeys = findPreference(KEY_SWAP_CAPACITIVE_KEYS);
        if (mSwapCapacitiveKeys != null && !isKeySwapperSupported(getActivity())) {
            prefScreen.removePreference(mSwapCapacitiveKeys);
            mSwapCapacitiveKeys = null;
        }

        final boolean isThreeButtonNavbarEnabled = EvolutionUtils.isThemeEnabled("com.android.internal.systemui.navbar.threebutton");
        mNavbarInverse = (SystemSettingSwitchPreference) findPreference(KEY_NAVBAR_INVERSE);
        mNavbarInverse.setEnabled(isThreeButtonNavbarEnabled);
        mNavigationCompactLayout = (SystemSettingSwitchPreference) findPreference(KEY_NAVIGATION_COMPACT_LAYOUT);
        mNavigationCompactLayout.setEnabled(isThreeButtonNavbarEnabled);
    }

    private static boolean isKeySwapperSupported(Context context) {
        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(context);
        return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_SWAP);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
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
            new BaseSearchIndexProvider(R.xml.evolution_settings_buttons);
}
