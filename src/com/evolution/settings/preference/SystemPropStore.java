/*
 * Copyright (C) 2019 Android Ice Cold Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolution.settings.preference;

import android.os.SystemProperties;
import android.preference.PreferenceDataStore;

public class SystemPropStore extends androidx.preference.PreferenceDataStore
        implements PreferenceDataStore {

    public boolean getBoolean(String key, boolean defValue) {
        return SystemProperties.getBoolean(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return Float.parseFloat(SystemProperties.get(key, "" + defValue));
    }

    public int getInt(String key, int defValue) {
        return SystemProperties.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return SystemProperties.getLong(key, defValue);
    }

    public String getString(String key, String defValue) {
        return SystemProperties.get(key, defValue);
    }

    public void putBoolean(String key, boolean value) {
        SystemProperties.set(key, "" + value);
    }

    public void putFloat(String key, float value) {
        SystemProperties.set(key, "" + value);
    }

    public void putInt(String key, int value) {
        SystemProperties.set(key, "" + value);
    }

    public void putLong(String key, long value) {
        SystemProperties.set(key, "" + value);
    }

    public void putString(String key, String value) {
        SystemProperties.set(key, value);
    }

}
