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
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.fingerprint.FingerprintManager;
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
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.evolution.udfps.UdfpsUtils;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.preference.SystemSettingListPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class LockScreen extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "LockScreen";
    private static final String AOD_SCHEDULE_KEY = "always_on_display_schedule";
    private static final String FINGERPRINT_CATEGORY = "lockscreen_fingerprint_category";
    private static final String SHORTCUT_START_KEY = "lockscreen_shortcut_start";
    private static final String SHORTCUT_END_KEY = "lockscreen_shortcut_end";
    private static final String SHORTCUT_ENFORCE_KEY = "lockscreen_shortcut_enforce";
    private static final String UDFPS_CATEGORY = "udfps_category";

    private static final String[] DEFAULT_START_SHORTCUT = new String[] { "home", "flashlight" };
    private static final String[] DEFAULT_END_SHORTCUT = new String[] { "wallet", "qr", "camera" };

    private FingerprintManager mFingerprintManager;
    private PreferenceCategory mFingerprintCategory;
    private PreferenceCategory mUdfpsCategory;

    static final int MODE_DISABLED = 0;
    static final int MODE_NIGHT = 1;
    static final int MODE_TIME = 2;
    static final int MODE_MIXED_SUNSET = 3;
    static final int MODE_MIXED_SUNRISE = 4;

    private Preference mAODPref;
    private SystemSettingListPreference mStartShortcut;
    private SystemSettingListPreference mEndShortcut;
    private SwitchPreference mEnforceShortcut;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.evolution_settings_lockscreen;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final PackageManager mPm = getActivity().getPackageManager();

        mFingerprintCategory = (PreferenceCategory) findPreference(FINGERPRINT_CATEGORY);
        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        if (mPm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) &&
                 mFingerprintManager != null) {
            if (!mFingerprintManager.isHardwareDetected()) {
                prefScreen.removePreference(mFingerprintCategory);
            }
        }

        mUdfpsCategory = findPreference(UDFPS_CATEGORY);
        if (!UdfpsUtils.hasUdfpsSupport(getContext())) {
            prefScreen.removePreference(mUdfpsCategory);
        }

        mAODPref = findPreference(AOD_SCHEDULE_KEY);
        updateAlwaysOnSummary();

        mStartShortcut = findPreference(SHORTCUT_START_KEY);
        mEndShortcut = findPreference(SHORTCUT_END_KEY);
        mEnforceShortcut = findPreference(SHORTCUT_ENFORCE_KEY);
        updateShortcutSelection();
        mStartShortcut.setOnPreferenceChangeListener(this);
        mEndShortcut.setOnPreferenceChangeListener(this);
        mEnforceShortcut.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAlwaysOnSummary();
        updateShortcutSelection();
    }

    private void updateAlwaysOnSummary() {
        if (mAODPref == null) return;
        int mode = Settings.Secure.getIntForUser(getActivity().getContentResolver(),
                Settings.Secure.DOZE_ALWAYS_ON_AUTO_MODE, 0, UserHandle.USER_CURRENT);
        switch (mode) {
            default:
            case MODE_DISABLED:
                mAODPref.setSummary(R.string.disabled);
                break;
            case MODE_NIGHT:
                mAODPref.setSummary(R.string.night_display_auto_mode_twilight);
                break;
            case MODE_TIME:
                mAODPref.setSummary(R.string.night_display_auto_mode_custom);
                break;
            case MODE_MIXED_SUNSET:
                mAODPref.setSummary(R.string.always_on_display_schedule_mixed_sunset);
                break;
            case MODE_MIXED_SUNRISE:
                mAODPref.setSummary(R.string.always_on_display_schedule_mixed_sunrise);
                break;
        }
    }

    private String getSettingsShortcutValue() {
        String value = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.KEYGUARD_QUICK_TOGGLES);
        if (value == null || value.isEmpty()) {
            StringBuilder sb = new StringBuilder(DEFAULT_START_SHORTCUT[0]);
            for (int i = 1; i < DEFAULT_START_SHORTCUT.length; i++) {
                sb.append(",").append(DEFAULT_START_SHORTCUT[i]);
            }
            sb.append(";" + DEFAULT_END_SHORTCUT[0]);
            for (int i = 1; i < DEFAULT_END_SHORTCUT.length; i++) {
                sb.append(",").append(DEFAULT_END_SHORTCUT[i]);
            }
            value = sb.toString();
        }
        return value;
    }

    private void updateShortcutSelection() {
        final String value = getSettingsShortcutValue();
        final String[] split = value.split(";");
        final String[] start = split[0].split(",");
        final String[] end = split[1].split(",");
        mStartShortcut.setValue(start[0]);
        mStartShortcut.setSummary(mStartShortcut.getEntry());
        mEndShortcut.setValue(end[0]);
        mEndShortcut.setSummary(mEndShortcut.getEntry());
        mEnforceShortcut.setChecked(start.length == 1 && end.length == 1);
    }

    private void setShortcutSelection(String value, boolean start) {
        setShortcutSelection(value, start, mEnforceShortcut.isChecked());
    }

    private void setShortcutSelection(String value, boolean start, boolean single) {
        final String oldValue = getSettingsShortcutValue();
        final int splitIndex = start ? 0 : 1;
        String[] split = oldValue.split(";");
        if (value.equals("none") || single) {
            split[splitIndex] = value;
        } else {
            StringBuilder sb = new StringBuilder(value);
            final String[] def = start ? DEFAULT_START_SHORTCUT : DEFAULT_END_SHORTCUT;
            for (String str : def) {
                if (str.equals(value)) continue;
                sb.append(",").append(str);
            }
            split[splitIndex] = sb.toString();
        }
        Settings.System.putString(getActivity().getContentResolver(),
                Settings.System.KEYGUARD_QUICK_TOGGLES, split[0] + ";" + split[1]);

        if (start) {
            mStartShortcut.setValue(value);
            mStartShortcut.setSummary(mStartShortcut.getEntry());
        } else {
            mEndShortcut.setValue(value);
            mEndShortcut.setSummary(mEndShortcut.getEntry());
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStartShortcut) {
            setShortcutSelection((String) newValue, true);
            return true;
        } else if (preference == mEndShortcut) {
            setShortcutSelection((String) newValue, false);
            return true;
        } else if (preference == mEnforceShortcut) {
            final boolean value = (Boolean) newValue;
            setShortcutSelection(mStartShortcut.getValue(), true, value);
            setShortcutSelection(mEndShortcut.getValue(), false, value);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.evolution_settings_lockscreen);
}
