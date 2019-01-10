/*
 * Copyright (C) 2019 The Evolution X Project
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
import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.android.settings.SettingsPreferenceFragment;
import com.evolution.settings.preferences.CustomSeekBarPreference;
import com.evolution.settings.preferences.SystemSettingSwitchPreference;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;

public class GestureSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String KEY_SWIPE_LENGTH = "gesture_swipe_length";
    private static final String KEY_SWIPE_TIMEOUT = "gesture_swipe_timeout";

    private CustomSeekBarPreference mSwipeTriggerLength;
    private CustomSeekBarPreference mSwipeTriggerTimeout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.evolution_settings_gestures);

        mSwipeTriggerLength = (CustomSeekBarPreference) findPreference(KEY_SWIPE_LENGTH);
        int value = Settings.System.getInt(getContentResolver(),
                Settings.System.BOTTOM_GESTURE_SWIPE_LIMIT,
                getSwipeLengthInPixel(getResources().getInteger(com.android.internal.R.integer.nav_gesture_swipe_min_length)));

        mSwipeTriggerLength.setMin(getSwipeLengthInPixel(40));
        mSwipeTriggerLength.setMax(getSwipeLengthInPixel(80));
        mSwipeTriggerLength.setValue(value);
        mSwipeTriggerLength.setOnPreferenceChangeListener(this);

        mSwipeTriggerTimeout = (CustomSeekBarPreference) findPreference(KEY_SWIPE_TIMEOUT);
        value = Settings.System.getInt(getContentResolver(),
                Settings.System.BOTTOM_GESTURE_TRIGGER_TIMEOUT,
                getResources().getInteger(com.android.internal.R.integer.nav_gesture_swipe_timout));
        mSwipeTriggerTimeout.setValue(value);
        mSwipeTriggerTimeout.setOnPreferenceChangeListener(this);
    }

    private int getSwipeLengthInPixel(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSwipeTriggerLength) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.BOTTOM_GESTURE_SWIPE_LIMIT, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mSwipeTriggerTimeout) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.BOTTOM_GESTURE_TRIGGER_TIMEOUT, value, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EVO_SETTINGS;
    }

}
