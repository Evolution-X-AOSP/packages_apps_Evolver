/*
 * Copyright (C) 2019-2020 The Evolution X Project
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
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.preference.SecureSettingMasterSwitchPreference;
import com.evolution.settings.preference.SystemSettingSeekBarPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class LockScreenClockSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String CLOCK_FONT_SIZE  = "lockclock_font_size";
    private static final String LOCKSCREEN_CLOCK_SELECTION = "lockscreen_clock_selection";
    private static final String LOCK_CLOCK_FONTS = "lock_clock_fonts";
    private static final String TEXT_CLOCK_ALIGNMENT = "text_clock_alignment";
    private static final String TEXT_CLOCK_PADDING = "text_clock_padding";

    private ListPreference mLockClockFonts;
    private ListPreference mLockClockSelection;
    private ListPreference mTextClockAlign;
    private SystemSettingSeekBarPreference mClockFontSize;
    private SystemSettingSeekBarPreference mTextClockPadding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.evolution_settings_lockscreen_clock);

        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getResources();

        // Lockscreen Clock
        mLockClockSelection = findPreference(LOCKSCREEN_CLOCK_SELECTION);
        boolean mClockSelection = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_CLOCK_SELECTION, 0, UserHandle.USER_CURRENT) == 10
                || Settings.Secure.getIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_CLOCK_SELECTION, 0, UserHandle.USER_CURRENT) == 11;
        mLockClockSelection.setOnPreferenceChangeListener(this);

        // Lockscreen Clock Fonts
        mLockClockFonts = findPreference(LOCK_CLOCK_FONTS);
        mLockClockFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_CLOCK_FONTS, 28)));
        mLockClockFonts.setSummary(mLockClockFonts.getEntry());
        mLockClockFonts.setOnPreferenceChangeListener(this);

        // Lockscreen Clock Size
        mClockFontSize = findPreference(CLOCK_FONT_SIZE);
        mClockFontSize.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKCLOCK_FONT_SIZE, 54));
        mClockFontSize.setOnPreferenceChangeListener(this);

        // Text Clock Alignment
        mTextClockAlign = findPreference(TEXT_CLOCK_ALIGNMENT);
        mTextClockAlign.setEnabled(mClockSelection);
        mTextClockAlign.setOnPreferenceChangeListener(this);

        // Text Clock Padding
        mTextClockPadding = findPreference(TEXT_CLOCK_PADDING);
        boolean mTextClockAlignx = Settings.System.getIntForUser(resolver,
                    Settings.System.TEXT_CLOCK_ALIGNMENT, 0, UserHandle.USER_CURRENT) == 1;
        mTextClockPadding.setEnabled(!mTextClockAlignx);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLockClockFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_CLOCK_FONTS,
                    Integer.valueOf((String) newValue));
            mLockClockFonts.setValue(String.valueOf(newValue));
            mLockClockFonts.setSummary(mLockClockFonts.getEntry());
            return true;
        } else if (preference == mLockClockSelection) {
            boolean val = Integer.valueOf((String) newValue) == 10
                    || Integer.valueOf((String) newValue) == 11;
            mTextClockAlign.setEnabled(val);
            return true;
        } else if (preference == mClockFontSize) {
            int top = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKCLOCK_FONT_SIZE, top*1);
            return true;
        } else if (preference == mTextClockAlign) {
            boolean val = Integer.valueOf((String) newValue) == 1;
            mTextClockPadding.setEnabled(!val);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EVO_SETTINGS;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.evolution_settings_lockscreen_clock;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
}
