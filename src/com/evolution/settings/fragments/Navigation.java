/*
 * Copyright (C) 2023 Evolution X
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

import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_2BUTTON_OVERLAY;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_3BUTTON_OVERLAY;
import static com.android.internal.util.custom.hwkeys.DeviceKeysConstants.*;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.custom.hardware.LineageHardwareManager;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.custom.NavbarUtils;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.buttons.preference.*;
import com.evolution.settings.preference.SecureSettingSwitchPreference;
import com.evolution.settings.preference.SystemSettingSwitchPreference;

import java.util.List;
import java.util.UUID;

@SearchIndexable
public class Navigation extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "Navigation";

    private static final String CATEGORY_NAVBAR = "navbar_key";
    private static final String CATEGORY_NAVBAR_ACTIONS = "navbar_actions_category";
    private static final String CATEGORY_NAVBAR_LAYOUT = "navbar_layout_category";
    private static final String KEY_NAV_MENU_ARROW_KEYS = "navigation_bar_menu_arrow_keys";
    private static final String KEY_NAV_INVERSE = "sysui_nav_bar_inverse";
    private static final String KEY_NAV_GESTURES = "navbar_gestures";
    private static final String KEY_NAV_COMPACT_LAYOUT = "navigation_bar_compact_layout";
    private static final String DISABLE_NAV_KEYS = "disable_nav_keys";
    private static final String KEY_NAVIGATION_BACK_LONG_PRESS =
            "navigation_back_long_press";
    private static final String KEY_NAVIGATION_HOME_LONG_PRESS = "navigation_home_long_press";
    private static final String KEY_NAVIGATION_HOME_DOUBLE_TAP = "navigation_home_double_tap";
    private static final String KEY_NAVIGATION_APP_SWITCH_LONG_PRESS =
            "navigation_app_switch_long_press";
    private static final String KEY_EDGE_LONG_SWIPE = "navigation_bar_edge_long_swipe";

    private ListPreference mNavigationBackLongPressAction;
    private ListPreference mNavigationHomeLongPressAction;
    private ListPreference mNavigationHomeDoubleTapAction;
    private ListPreference mNavigationAppSwitchLongPressAction;
    private ListPreference mEdgeLongSwipeAction;
    private SwitchPreference mDisableNavigationKeys;
    private SystemSettingSwitchPreference mNavigationMenuArrowKeys;
    private SecureSettingSwitchPreference mNavigationInverse;
    private Preference mNavigationGestures;
    private SystemSettingSwitchPreference mNavigationCompactLayout;
    private PreferenceCategory mNavbarCategory;
    private PreferenceCategory mNavbarActionsCategory;
    private PreferenceCategory mNavbarLayoutCategory;

    private Handler mHandler;

    private IOverlayManager mOverlayManager;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.evolution_settings_navigation;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mHandler = new Handler();

        // Force Navigation bar related options
        mDisableNavigationKeys = findPreference(DISABLE_NAV_KEYS);
        mNavbarCategory = prefScreen.findPreference(CATEGORY_NAVBAR);
        mNavbarActionsCategory = prefScreen.findPreference(CATEGORY_NAVBAR_ACTIONS);
        mNavbarLayoutCategory = prefScreen.findPreference(CATEGORY_NAVBAR_LAYOUT);
        mNavigationMenuArrowKeys = findPreference(KEY_NAV_MENU_ARROW_KEYS);
        mNavigationInverse = findPreference(KEY_NAV_INVERSE);
        mNavigationGestures = findPreference(KEY_NAV_GESTURES);
        mNavigationCompactLayout = findPreference(KEY_NAV_COMPACT_LAYOUT);

        Action defaultBackLongPressAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_longPressOnBackBehavior));
        Action defaultHomeLongPressAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_longPressOnHomeBehavior));
        Action defaultHomeDoubleTapAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_doubleTapOnHomeBehavior));
        Action defaultAppSwitchLongPressAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_longPressOnAppSwitchBehavior));
        Action backLongPressAction = Action.fromSettings(resolver,
                Settings.System.KEY_BACK_LONG_PRESS_ACTION,
                defaultBackLongPressAction);
        Action homeLongPressAction = Action.fromSettings(resolver,
                Settings.System.KEY_HOME_LONG_PRESS_ACTION,
                defaultHomeLongPressAction);
        Action homeDoubleTapAction = Action.fromSettings(resolver,
                Settings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                defaultHomeDoubleTapAction);
        Action appSwitchLongPressAction = Action.fromSettings(resolver,
                Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION,
                defaultAppSwitchLongPressAction);
        Action edgeLongSwipeAction = Action.fromSettings(resolver,
                Settings.System.KEY_EDGE_LONG_SWIPE_ACTION,
                Action.NOTHING);

        // Navigation bar back long press
        mNavigationBackLongPressAction = initList(KEY_NAVIGATION_BACK_LONG_PRESS,
                backLongPressAction);

        // Navigation bar home long press
        mNavigationHomeLongPressAction = initList(KEY_NAVIGATION_HOME_LONG_PRESS,
                homeLongPressAction);

        // Navigation bar home double tap
        mNavigationHomeDoubleTapAction = initList(KEY_NAVIGATION_HOME_DOUBLE_TAP,
                homeDoubleTapAction);

        // Navigation bar app switch long press
        mNavigationAppSwitchLongPressAction = initList(KEY_NAVIGATION_APP_SWITCH_LONG_PRESS,
                appSwitchLongPressAction);

        // Edge long swipe gesture
        mEdgeLongSwipeAction = initList(KEY_EDGE_LONG_SWIPE, edgeLongSwipeAction);

        // Only visible on devices that does not have a navigation bar already
        if (NavbarUtils.canDisable(getActivity())) {
            // Remove keys that can be provided by the navbar
            updateDisableNavkeysOption();
            mNavigationMenuArrowKeys.setDependency(DISABLE_NAV_KEYS);
            mNavigationInverse.setDependency(DISABLE_NAV_KEYS);
            mNavigationGestures.setDependency(DISABLE_NAV_KEYS);
            mNavigationCompactLayout.setDependency(DISABLE_NAV_KEYS);
            mEdgeLongSwipeAction.setDependency(DISABLE_NAV_KEYS);
        } else {
            mNavbarCategory.removePreference(mDisableNavigationKeys);
            mDisableNavigationKeys = null;
        }

        // Navigation bar modes
        updateNavigationBarModeState();

        // Edge swipe gesture
        updateEdgeSwipeGesturePreference();
    }

    private void updateNavigationBarModeState(){
        final ContentResolver resolver = getActivity().getContentResolver();
        String mode = NavbarUtils.getNavigationBarModeOverlay(getActivity(), mOverlayManager);
        if (!mode.equals(NAV_BAR_MODE_3BUTTON_OVERLAY) && !mode.equals(NAV_BAR_MODE_2BUTTON_OVERLAY)){
            if (mNavigationMenuArrowKeys != null){
                mNavbarLayoutCategory.removePreference(mNavigationMenuArrowKeys);
                mNavigationMenuArrowKeys = null;
            }
            if (mNavigationInverse != null){
                mNavbarLayoutCategory.removePreference(mNavigationInverse);
                mNavigationInverse = null;
            }
            if (mNavigationCompactLayout != null){
                mNavbarLayoutCategory.removePreference(mNavigationCompactLayout);
                mNavigationCompactLayout = null;
            }
        }else{
            if (mEdgeLongSwipeAction != null){
                mNavbarActionsCategory.removePreference(mEdgeLongSwipeAction);
                mEdgeLongSwipeAction = null;
            }
        }
        if (mNavbarCategory != null && mNavbarCategory.getPreferenceCount() == 0){
            getPreferenceScreen().removePreference(mNavbarCategory);
            mNavbarCategory = null;
        }
        if (mNavigationMenuArrowKeys != null){
            mNavigationMenuArrowKeys.setChecked(
                Settings.System.getInt(resolver, Settings.System.NAVIGATION_BAR_MENU_ARROW_KEYS, 0) != 0);
        }
        if (mNavigationInverse != null){
            mNavigationInverse.setChecked(
                Settings.Secure.getInt(resolver, KEY_NAV_INVERSE, 0) != 0);
        }
        if (mNavigationCompactLayout != null){
            mNavigationCompactLayout.setChecked(
                Settings.System.getInt(resolver, Settings.System.NAV_BAR_COMPACT_LAYOUT, 0) != 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Navigation bar modes
        updateNavigationBarModeState();

        // Edge swipe gesture
        updateEdgeSwipeGesturePreference();
    }

    private void updateEdgeSwipeGesturePreference(){
        final ContentResolver resolver = getActivity().getContentResolver();
        if (mEdgeLongSwipeAction != null){
            mEdgeLongSwipeAction.setValue(Integer.toString(Action.fromSettings(resolver,
                Settings.System.KEY_EDGE_LONG_SWIPE_ACTION,
                Action.NOTHING).ordinal()));
            mEdgeLongSwipeAction.setSummary(mEdgeLongSwipeAction.getEntry());
        }
    }

    private ListPreference initList(String key, Action value) {
        return initList(key, value.ordinal());
    }

    private ListPreference initList(String key, int value) {
        ListPreference list = getPreferenceScreen().findPreference(key);
        if (list == null) return null;
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    private void handleSystemListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavigationBackLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_BACK_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mNavigationHomeLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mNavigationHomeDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mNavigationAppSwitchLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mEdgeLongSwipeAction) {
            handleListChange(mEdgeLongSwipeAction, newValue,
                    Settings.System.KEY_EDGE_LONG_SWIPE_ACTION);
            return true;
        }
        return false;
    }

    private void writeDisableNavkeysOption(boolean enabled) {
        NavbarUtils.setEnabled(getActivity(), enabled);
    }

    private void updateDisableNavkeysOption() {
        mDisableNavigationKeys.setChecked(NavbarUtils.isEnabled(getActivity()));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mDisableNavigationKeys) {
            mDisableNavigationKeys.setEnabled(false);
            writeDisableNavkeysOption(mDisableNavigationKeys.isChecked());
            updateDisableNavkeysOption();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mDisableNavigationKeys.setEnabled(true);
                    } catch(Exception e) {
                    }
                }
            }, 1000);
        }
        return super.onPreferenceTreeClick(preference);
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
            new BaseSearchIndexProvider(R.xml.evolution_settings_navigation);
}
