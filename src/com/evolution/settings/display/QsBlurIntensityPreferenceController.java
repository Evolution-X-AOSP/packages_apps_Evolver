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

package com.evolution.settings.display;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

import com.evolution.settings.preference.SystemSettingSeekBarPreference;

import java.util.ArrayList;
import java.util.List;

public class QsBlurIntensityPreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String QS_BLUR_INTENSITY = "qs_blur_intensity";

    private SystemSettingSeekBarPreference mQsBlurIntensity;

    public QsBlurIntensityPreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return QS_BLUR_INTENSITY;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mQsBlurIntensity = (SystemSettingSeekBarPreference) screen.findPreference(QS_BLUR_INTENSITY);
        int qsBlurIntensity = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QS_BLUR_INTENSITY, 100);
        mQsBlurIntensity.setValue(qsBlurIntensity);
        mQsBlurIntensity.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mQsBlurIntensity) {
            int value = (Integer) newValue;
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.QS_BLUR_INTENSITY, value);
            return true;
        }
        return false;
    }
}
