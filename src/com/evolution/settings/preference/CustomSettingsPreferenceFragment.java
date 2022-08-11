/*
 * Copyright (C) 2018 CarbonROM
 * Copyright (C) 2018 Adin Kwok
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

import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public abstract class CustomSettingsPreferenceFragment extends SettingsPreferenceFragment {
    protected static final int STATE_ON = 1;
    protected static final int STATE_OFF = 0;

    /*
     * TwoStatePreference subtypes include CheckBoxPreference and SwitchPreference
     */
    protected static final int SYSTEM_TWO_STATE = 0;
    protected static final int SECURE_TWO_STATE = 1;
    protected static final int GLOBAL_TWO_STATE = 2;

    /*
     * ArrayList holds preferenceType and defaultValue
     */
    private HashMap<Preference, ArrayList<Integer>> mCustomPreferences;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mCustomPreferences = new HashMap<>();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAllCustomPreferences();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (mCustomPreferences.containsKey(preference)) {
            for (Preference customPreference : mCustomPreferences.keySet()) {
                if (customPreference == preference) {
                        return updateCustomPreference(customPreference, true);
                }
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    protected void updateAllCustomPreferences() {
        for (Preference customPreference : mCustomPreferences.keySet()) {
            updateCustomPreference(customPreference, false);
        }
    }

    protected boolean updateCustomPreference(Preference preference, boolean clicked) {
        if (mCustomPreferences.containsKey(preference)) {
            int preferenceType = mCustomPreferences.get(preference).get(0).intValue();
            if (preferenceType == SYSTEM_TWO_STATE || preferenceType == SECURE_TWO_STATE
                    || preferenceType == GLOBAL_TWO_STATE) {
                updateTwoStatePreference(preference, preferenceType, clicked);
                return true;
            }
        }
        return false;
    }

    private void updateTwoStatePreference(Preference preference, int switchType, boolean clicked) {
        TwoStatePreference switchPreference = (TwoStatePreference) preference;
        int defaultValue = mCustomPreferences.get(preference).get(1).intValue();
        boolean isEnable = false;
        if (switchType == SYSTEM_TWO_STATE) {
            if (clicked) {
                Settings.System.putInt(getActivity().getContentResolver(), switchPreference.getKey(),
                        (switchPreference.isChecked() ? STATE_ON : STATE_OFF));
            }
            isEnable = (Settings.System.getInt(
                    getActivity().getContentResolver(), switchPreference.getKey(), defaultValue) == 1);
        } else if (switchType == SECURE_TWO_STATE) {
            if (clicked) {
                Settings.Secure.putInt(getActivity().getContentResolver(), switchPreference.getKey(),
                        (switchPreference.isChecked() ? STATE_ON : STATE_OFF));
            }
            isEnable = (Settings.Secure.getInt(
                    getActivity().getContentResolver(), switchPreference.getKey(), defaultValue) == 1);
        } else if (switchType == GLOBAL_TWO_STATE) {
            if (clicked) {
                Settings.Global.putInt(getActivity().getContentResolver(), switchPreference.getKey(),
                        (switchPreference.isChecked() ? STATE_ON : STATE_OFF));
            }
            isEnable = (Settings.Global.getInt(
                    getActivity().getContentResolver(), switchPreference.getKey(), defaultValue) == 1);
        }
        switchPreference.setChecked(isEnable);
    }

    protected void addCustomPreference(Preference preference, int preferenceType, int defaultValue) {
        ArrayList<Integer> prefAndDefault = new ArrayList<>();
        prefAndDefault.add(new Integer(preferenceType));
        prefAndDefault.add(new Integer(defaultValue));
        mCustomPreferences.put(preference, prefAndDefault);
    }

    protected Set<Preference> getCustomPreferenceSet() {
        return Collections.unmodifiableSet(mCustomPreferences.keySet());
    }

    protected Preference getCustomPreference(String key) {
        for (Preference preference : mCustomPreferences.keySet()) {
            if (preference.getKey().equals(key)) {
                return preference;
            }
        }
        return null;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }
}
