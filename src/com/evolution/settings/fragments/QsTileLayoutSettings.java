/*
 * Copyright (C) 2022 The Nameless-AOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.evolution.settings.fragments;

import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.evolution.settings.preference.SystemSettingSeekBarPreference;
import com.evolution.settings.preference.SystemSettingSwitchPreference;

public class QsTileLayoutSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_QS_ROW_PORTRAIT = "qs_layout_rows";
    private static final String KEY_QQS_ROW_PORTRAIT = "qqs_layout_rows";
    private static final String KEY_QS_HIDE_LABEL = "qs_tile_label_hide";
    private static final String KEY_QS_VERTICAL_LAYOUT = "qs_tile_vertical_layout";

    private SystemSettingSeekBarPreference mQsRows;
    private SystemSettingSeekBarPreference mQqsRows;

    private SystemSettingSwitchPreference mHide;
    private SystemSettingSwitchPreference mVertical;
    
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        addPreferencesFromResource(R.xml.qs_tile_layout);

        final int qs_rows = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.QS_LAYOUT, 4, UserHandle.USER_CURRENT);

        mQsRows = (SystemSettingSeekBarPreference) findPreference(KEY_QS_ROW_PORTRAIT);
        mQsRows.setOnPreferenceChangeListener(this);

        mQqsRows = (SystemSettingSeekBarPreference) findPreference(KEY_QQS_ROW_PORTRAIT);
        updateQqsRowsLimit(qs_rows);
        final boolean hideLabel = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.QS_TILE_LABEL_HIDE , 0, UserHandle.USER_CURRENT) == 1;

        mHide = (SystemSettingSwitchPreference) findPreference(KEY_QS_HIDE_LABEL);
        mHide.setOnPreferenceChangeListener(this);

        mVertical = (SystemSettingSwitchPreference) findPreference(KEY_QS_VERTICAL_LAYOUT);
        mVertical.setEnabled(!hideLabel);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mQsRows) {
            int qs_rows = Integer.parseInt(newValue.toString());
            updateQqsRowsLimit(qs_rows);
            return true;
        } else if (preference == mHide) {
            boolean hideLabel = (Boolean) newValue;
            mVertical.setEnabled(!hideLabel);
            return true;
        }
        return false;
    }

    private void updateQqsRowsLimit(int max_rows) {
        mQqsRows.setMax(max_rows);
        final int curr = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.QQS_LAYOUT, 2, UserHandle.USER_CURRENT);
        if (curr > max_rows) {
            mQqsRows.setValue(max_rows);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.QQS_LAYOUT, max_rows, UserHandle.USER_CURRENT);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EVOLVER;
    }
}
