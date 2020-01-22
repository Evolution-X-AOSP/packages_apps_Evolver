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
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class QsbgColorPreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String QS_BG_COLOR = "qs_bg_color";
    static final int DEFAULT_QS_BG_COLOR = 0xff000000;

    private ColorPickerPreference mQSbgColor;

    public QsbgColorPreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return QS_BG_COLOR;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mQSbgColor = (ColorPickerPreference) screen.findPreference(QS_BG_COLOR);
        mQSbgColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.QS_BG_COLOR, DEFAULT_QS_BG_COLOR, UserHandle.USER_CURRENT);
        String hexColor = String.format("#%08x", (0xff000000 & intColor));
        if (hexColor.equals("#ff000000")) {
            mQSbgColor.setSummary(R.string.default_string);
        } else {
            mQSbgColor.setSummary(hexColor);
        }
        mQSbgColor.setAlphaSliderEnabled(true);
        mQSbgColor.setNewPreviewColor(intColor);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mQSbgColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ff000000")) {
                mQSbgColor.setSummary(R.string.default_string);
            } else {
                mQSbgColor.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.QS_BG_COLOR, intHex, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }
}
