/*
 * Copyright (C) 2018 CarbonROM
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

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;

import com.evolution.settings.preference.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

public class SmartPixels extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "SmartPixels";
    private static final String ENABLE = "smart_pixels_enable";
    private static final String ON_POWER_SAVE = "smart_pixels_on_power_save";

    private Handler mHandler = new Handler();
    private SmartPixelsObserver mSmartPixelsObserver;
    private SystemSettingSwitchPreference mSmartPixelsEnable;
    private SystemSettingSwitchPreference mSmartPixelsOnPowerSave;

    private boolean mIsSmartPixelsEnabled;
    private boolean mIsSmartPixelsOnPowerSave;

    ContentResolver resolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.evolution_settings_smart_pixels);

        resolver = getActivity().getContentResolver();

        mSmartPixelsEnable = (SystemSettingSwitchPreference) findPreference(ENABLE);
        mSmartPixelsOnPowerSave = (SystemSettingSwitchPreference) findPreference(ON_POWER_SAVE);

        mSmartPixelsObserver = new SmartPixelsObserver(mHandler);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSmartPixelsObserver != null) {
            mSmartPixelsObserver.register();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSmartPixelsObserver != null) {
            mSmartPixelsObserver.unregister();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        return true;
    }

    private void updatePreferences() {
        mIsSmartPixelsEnabled = (Settings.System.getIntForUser(
                resolver, Settings.System.SMART_PIXELS_ENABLE,
                0, UserHandle.USER_CURRENT) == 1);
        mIsSmartPixelsOnPowerSave = (Settings.System.getIntForUser(
                resolver, Settings.System.SMART_PIXELS_ON_POWER_SAVE,
                0, UserHandle.USER_CURRENT) == 1);

        mSmartPixelsEnable.setChecked(mIsSmartPixelsEnabled);
        mSmartPixelsOnPowerSave.setChecked(mIsSmartPixelsOnPowerSave);
    }

    private class SmartPixelsObserver extends ContentObserver {
        public SmartPixelsObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.SMART_PIXELS_ENABLE),
                    false, this, UserHandle.USER_CURRENT);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.SMART_PIXELS_ON_POWER_SAVE),
                    false, this, UserHandle.USER_CURRENT);
        }

        public void unregister() {
            resolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            updatePreferences();
        }
    }

    /**
     * For Search.
     */

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.evolution_settings_smart_pixels);
}
