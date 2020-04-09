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

public class AccentColorPreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String ACCENT_COLOR = "accent_color";
    private static final String GRADIENT_COLOR = "gradient_color";
    static final int DEFAULT_ACCENT_COLOR = 0xff56ab2f
    static final int DEFAULT_GRADIENT_COLOR = 0xffa8e063

    private ColorPickerPreference mAccentColor;
    private ColorPickerPreference mGradientColor;

    public AccentColorPreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return ACCENT_COLOR;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);

        final ContentResolver resolver = mContext.getContentResolver();

        mAccentColor = (ColorPickerPreference) screen.findPreference(ACCENT_COLOR);
        mAccentColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getIntForUser(resolver,
                Settings.System.ACCENT_COLOR, DEFAULT_ACCENT_COLOR, UserHandle.USER_CURRENT);
        String hexColor = String.format("#%08x", (0xff56ab2f & intColor));
        if (hexColor.equals("#ff56ab2f")) {
            mAccentColor.setSummary(R.string.default_string);
        } else {
            mAccentColor.setSummary(hexColor);
        }
        mAccentColor.setNewPreviewColor(intColor);

        mGradientColor = (ColorPickerPreference) screen.findPreference(GRADIENT_COLOR);
        mGradientColor.setOnPreferenceChangeListener(this);
        int color = Settings.System.getIntForUser(resolver,
                Settings.System.GRADIENT_COLOR, DEFAULT_GRADIENT_COLOR, UserHandle.USER_CURRENT);
        String gradientHex = String.format("#%08x", (0xffa8e063 & color));
        if (gradientHex.equals("#ffa8e063")) {
            mGradientColor.setSummary(R.string.default_string);
        } else {
            mGradientColor.setSummary(gradientHex);
        }
        mGradientColor.setNewPreviewColor(color);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAccentColor) {
        final ContentResolver resolver = mContext.getContentResolver();
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ff56ab2f")) {
                mAccentColor.setSummary(R.string.default_string);
            } else {
                mAccentColor.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putIntForUser(resolver,
                    Settings.System.ACCENT_COLOR, intHex, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mGradientColor) {
        final ContentResolver resolver = mContext.getContentResolver();
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ffa8e063")) {
                mGradientColor.setSummary(R.string.default_string);
            } else {
                mGradientColor.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putIntForUser(resolver,
                    Settings.System.GRADIENT_COLOR, intHex, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }
}
