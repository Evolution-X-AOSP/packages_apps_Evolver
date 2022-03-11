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

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class QuickSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private ListPreference mQuickPulldown;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.evolution_settings_quicksettings);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

        int qpmode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 0, UserHandle.USER_CURRENT);
        mQuickPulldown = (ListPreference) findPreference("status_bar_quick_qs_pulldown");
        mQuickPulldown.setValue(String.valueOf(qpmode));
        mQuickPulldown.setSummary(mQuickPulldown.getEntry());
        mQuickPulldown.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mQuickPulldown) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, value,
                    UserHandle.USER_CURRENT);
            int index = mQuickPulldown.findIndexOfValue((String) newValue);
            mQuickPulldown.setSummary(
                    mQuickPulldown.getEntries()[index]);
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
            new BaseSearchIndexProvider(R.xml.evolution_settings_quicksettings);
}
