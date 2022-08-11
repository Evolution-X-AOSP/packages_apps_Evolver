/*
 * Copyright (C) 2019-2024 The Evolution X Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.evolution.settings.fragments.statusbar;

import android.content.ContentResolver;
import android.content.Context;
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
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.TwoStatePreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.preference.CustomSeekBarPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class NetworkTrafficSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "NetworkTrafficSettings";

    private CustomSeekBarPreference mNetTrafficAutohideThreshold;
    private CustomSeekBarPreference mNetTrafficRefreshInterval;
    private ListPreference mNetTrafficLocation;
    private ListPreference mNetTrafficMode;
    private ListPreference mNetTrafficUnits;
    private TwoStatePreference mNetTrafficAutohide;
    private TwoStatePreference mNetTrafficHideArrow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.evolution_settings_network_traffic);

        final ContentResolver resolver = getActivity().getContentResolver();

        mNetTrafficAutohideThreshold = (CustomSeekBarPreference)
                findPreference(Settings.Secure.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD);
        mNetTrafficRefreshInterval = (CustomSeekBarPreference)
                findPreference(Settings.Secure.NETWORK_TRAFFIC_REFRESH_INTERVAL);
        mNetTrafficLocation = (ListPreference)
                findPreference(Settings.Secure.NETWORK_TRAFFIC_LOCATION);
        mNetTrafficLocation.setOnPreferenceChangeListener(this);
        mNetTrafficMode = (ListPreference)
                findPreference(Settings.Secure.NETWORK_TRAFFIC_MODE);
        mNetTrafficAutohide = (TwoStatePreference)
                findPreference(Settings.Secure.NETWORK_TRAFFIC_AUTOHIDE);
        mNetTrafficUnits = (ListPreference)
                findPreference(Settings.Secure.NETWORK_TRAFFIC_UNITS);
        mNetTrafficHideArrow = (TwoStatePreference)
                findPreference(Settings.Secure.NETWORK_TRAFFIC_HIDEARROW);

        int location = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.NETWORK_TRAFFIC_LOCATION, 0, UserHandle.USER_CURRENT);
        updateEnabledStates(location);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mNetTrafficLocation) {
            int location = Integer.valueOf((String) newValue);
            updateEnabledStates(location);
            return true;
        }
        return false;
    }

    private void updateEnabledStates(int location) {
        final boolean enabled = location != 0;
        mNetTrafficMode.setEnabled(enabled);
        mNetTrafficAutohide.setEnabled(enabled);
        mNetTrafficAutohideThreshold.setEnabled(enabled);
        mNetTrafficHideArrow.setEnabled(enabled);
        mNetTrafficRefreshInterval.setEnabled(enabled);
        mNetTrafficUnits.setEnabled(enabled);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.evolution_settings_network_traffic);
}
