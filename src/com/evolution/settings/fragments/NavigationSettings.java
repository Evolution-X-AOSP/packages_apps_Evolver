/*
 * Copyright (C) 2019-2021 The Evolution X Project
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

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.evolution.EvolutionUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;

import com.evolution.settings.preference.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

public class NavigationSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String GESTURE_SYSTEM_NAVIGATION = "gesture_system_navigation";
    private static final String LAYOUT_SETTINGS = "navbar_layout_views";
    private static final String NAVIGATION_BAR_INVERSE = "navbar_inverse_layout";
    private static final String PIXEL_NAV_ANIMATION = "pixel_nav_animation";

    private Preference mGestureSystemNavigation;
    private Preference mLayoutSettings;
    private SwitchPreference mSwapNavButtons;
    private SystemSettingSwitchPreference mPixelNavAnimation;

    private boolean mIsNavSwitchingMode = false;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.evolution_settings_navigation);

        final PreferenceScreen prefScreen = getPreferenceScreen();

        mGestureSystemNavigation = findPreference(GESTURE_SYSTEM_NAVIGATION);
        mPixelNavAnimation = findPreference(PIXEL_NAV_ANIMATION);
        mLayoutSettings = findPreference(LAYOUT_SETTINGS);
        mSwapNavButtons = findPreference(NAVIGATION_BAR_INVERSE);

        // On three button nav
        if (EvolutionUtils.isThemeEnabled("com.android.internal.systemui.navbar.threebutton")) {
            mGestureSystemNavigation.setSummary(getString(R.string.legacy_navigation_title));
            mPixelNavAnimation.setSummary(getString(R.string.pixel_navbar_anim_summary));
        // On two button nav
        } else if (EvolutionUtils.isThemeEnabled("com.android.internal.systemui.navbar.twobutton")) {
            mGestureSystemNavigation.setSummary(getString(R.string.swipe_up_to_switch_apps_title));
            mPixelNavAnimation.setSummary(getString(R.string.pixel_navbar_anim_summary));
        // On gesture nav
        } else {
            mGestureSystemNavigation.setSummary(getString(R.string.edge_to_edge_navigation_title));
            mLayoutSettings.setSummary(getString(R.string.unsupported_gestures));
            mPixelNavAnimation.setSummary(getString(R.string.unsupported_gestures));
            mSwapNavButtons.setSummary(getString(R.string.unsupported_gestures));
            mLayoutSettings.setEnabled(false);
            mPixelNavAnimation.setEnabled(false);
            mSwapNavButtons.setEnabled(false);
        }

        mHandler = new Handler();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }

    /**
     * For Search.
     */

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.evolution_settings_navigation);
}
