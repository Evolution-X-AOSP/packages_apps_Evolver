/*
 * Copyright (C) 2023 The risingOS Android Project
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

package com.evolution.settings.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.util.AttributeSet;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.internal.util.evolution.EvolutionUtils;

public class SystemPropertyListPreference extends ListPreference {

    private static final String PREFS_NAME = "system_property_store_";

    public SystemPropertyListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME + String.valueOf(getKey()), Context.MODE_PRIVATE);
        String savedValue = preferences.getString(getKey(), null);
        if (savedValue != null) {
            setValue(savedValue);
            setSummary("%s");
        } else {
            savedValue = SystemProperties.get(getKey());
            if (savedValue != null && !savedValue.isEmpty()) {
                setValue(savedValue);
                setSummary("%s");
            }
        }
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = (String) newValue;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getKey(), value);
                editor.apply();
                SystemProperties.set(getKey(), value);
                EvolutionUtils.showSystemRestartDialog(context);
                return true;
            }
        });
    }
}
