/*
 * Copyright (C) 2023 The LibreMobileOS Foundation
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
import android.graphics.Color;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.widget.Switch;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settingslib.widget.LayoutPreference;
import com.android.settingslib.widget.MainSwitchPreference;
import com.android.settingslib.widget.OnMainSwitchChangeListener;
import com.android.settingslib.widget.SelectorWithWidgetPreference;

import java.util.Arrays;
import java.util.List;

public class AmbientEdgeLightSettings extends SettingsPreferenceFragment implements
        SelectorWithWidgetPreference.OnClickListener, OnMainSwitchChangeListener,
        ColorSelectorAdapter.ColorSelectListener {

    private static final String PULSE_AMBIENT_LIGHT = "pulse_ambient_light";
    private static final String PULSE_AMBIENT_LIGHT_COLOR_MODE = "pulse_ambient_light_color_mode";
    private static final String PULSE_AMBIENT_LIGHT_COLOR_AUTO = "pulse_ambient_light_color_mode_auto";
    private static final String PULSE_AMBIENT_LIGHT_COLOR_APP = "pulse_ambient_light_color_mode_app";
    private static final String PULSE_AMBIENT_LIGHT_COLOR_MANUAL = "pulse_ambient_light_color_mode_manual";
    private static final String PULSE_AMBIENT_LIGHT_COLOR_SELECTOR = "pulse_ambient_light_color_selector";
    private static final List<String> COLOR_MODES = Arrays.asList(PULSE_AMBIENT_LIGHT_COLOR_APP,
            PULSE_AMBIENT_LIGHT_COLOR_AUTO,
            PULSE_AMBIENT_LIGHT_COLOR_MANUAL);

    private MainSwitchPreference mMainSwitchPref;
    private SelectorWithWidgetPreference mLightColorAutoPref;
    private SelectorWithWidgetPreference mLightColorAppPref;
    private SelectorWithWidgetPreference mLightColorManualPref;
    private LayoutPreference mColorSelectorPref;
    private RecyclerView mRecyclerView;

    private int mColorMode = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ambient_edge_light_settings);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final PreferenceScreen prefSet = getPreferenceScreen();
        mMainSwitchPref = prefSet.findPreference(PULSE_AMBIENT_LIGHT);
        mMainSwitchPref.addOnSwitchChangeListener(this);
        mLightColorAutoPref = prefSet.findPreference(PULSE_AMBIENT_LIGHT_COLOR_AUTO);
        mLightColorAutoPref.setOnClickListener(this);
        mLightColorAppPref = prefSet.findPreference(PULSE_AMBIENT_LIGHT_COLOR_APP);
        mLightColorAppPref.setOnClickListener(this);
        mLightColorManualPref = prefSet.findPreference(PULSE_AMBIENT_LIGHT_COLOR_MANUAL);
        mLightColorManualPref.setOnClickListener(this);
        mColorSelectorPref = prefSet.findPreference(PULSE_AMBIENT_LIGHT_COLOR_SELECTOR);

        // Setup manual color selector
        mRecyclerView = mColorSelectorPref.findViewById(R.id.color_selector_view);
        int selectedColor = getSelectedColor();
        String selectedColorHex = getColorHex(selectedColor);
        List<String> colors = Arrays.asList(getResources()
                .getStringArray(R.array.ambient_edge_light_manual_colors));
        int index = colors.indexOf(selectedColorHex);
        int defaultPosition = index;
        if (defaultPosition == -1) {
            // Set first color as default color
            int defaultColor = Color.parseColor(colors.get(0));
            setSelectedColor(defaultColor);
            defaultPosition = 0;
        }
        mRecyclerView.setAdapter(new ColorSelectorAdapter(colors, defaultPosition, this));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        enableColorSelector(mMainSwitchPref.isChecked());
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        enableColorSelector(isChecked);
    }

    @Override
    public void onRadioButtonClicked(SelectorWithWidgetPreference emiter) {
        if (emiter == null) return;
        String selectedKey = emiter.getKey();
        updateUI(selectedKey);
        int colorMode = getColorModeByKey(selectedKey);
        if (mColorMode == colorMode) return;
        mColorMode = colorMode;
        setColorMode(mColorMode);
    }

    @Override
    public void onColorSelect(int color) {
        setSelectedColor(color);
    }

    private void updateUI() {
        mColorMode = getColorMode();
        updateUI(mColorMode);
    }

    private void updateUI(int colorMode) {
        String selectedKey = getKeyByColorMode(colorMode);
        updateUI(selectedKey);
    }

    private void updateUI(String selectedKey) {
        mLightColorAutoPref.setChecked(selectedKey.equals(PULSE_AMBIENT_LIGHT_COLOR_AUTO));
        mLightColorAppPref.setChecked(selectedKey.equals(PULSE_AMBIENT_LIGHT_COLOR_APP));
        mLightColorManualPref.setChecked(selectedKey.equals(PULSE_AMBIENT_LIGHT_COLOR_MANUAL));
        mColorSelectorPref.setVisible(selectedKey.equals(PULSE_AMBIENT_LIGHT_COLOR_MANUAL));
    }

    private void enableColorSelector(boolean enabled) {
        // handle enable/disable state only in manual color mode.
        if (mColorMode != 2) return;
        // Enable/disable the color selector.
        float colorAlpha = enabled ? 1f : 0.3f;
        mRecyclerView.setAlpha(colorAlpha);
        mRecyclerView.suppressLayout(!enabled);
        mColorSelectorPref.setSelectable(enabled);
    }

    private int getColorModeByKey(String selectedKey) {
        int index = COLOR_MODES.indexOf(selectedKey);
        return (index == -1) ? 1 : index;
    }

    private String getKeyByColorMode(int colorMode) {
        if (colorMode < 0 || colorMode >= COLOR_MODES.size()) {
            return COLOR_MODES.get(1);
        } else {
            return COLOR_MODES.get(colorMode);
        }
    }

    private int getColorMode() {
        return Settings.Secure.getIntForUser(getActivity().getContentResolver(),
            Settings.Secure.PULSE_AMBIENT_LIGHT_COLOR_MODE, 1, UserHandle.USER_CURRENT);
    }

    private void setColorMode(int colorMode) {
        Settings.Secure.putIntForUser(getActivity().getContentResolver(),
                Settings.Secure.PULSE_AMBIENT_LIGHT_COLOR_MODE, colorMode, UserHandle.USER_CURRENT);
    }

    private int getSelectedColor() {
        return Settings.Secure.getIntForUser(getActivity().getContentResolver(),
            Settings.Secure.PULSE_AMBIENT_LIGHT_COLOR, 1, UserHandle.USER_CURRENT);
    }

    private void setSelectedColor(int color) {
        Settings.Secure.putIntForUser(getActivity().getContentResolver(),
            Settings.Secure.PULSE_AMBIENT_LIGHT_COLOR, color, UserHandle.USER_CURRENT);
    }

    private String getColorHex(int color) {
        // It will return in '#AARRGGBB' format.
        return "#" + Integer.toHexString(color).toUpperCase();
    }

}
