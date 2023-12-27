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
import com.evolution.settings.buttons.ButtonSettingsUtils;
import com.evolution.settings.preference.CustomDialogPreference;
import com.evolution.settings.preference.SecureSettingSwitchPreference;
import com.evolution.settings.preference.SystemSettingSwitchPreference;

import java.util.List;
import java.util.UUID;

@SearchIndexable
public class Buttons extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "Buttons";

    private static final String KEY_BUTTON_BACKLIGHT = "button_backlight";
    private static final String KEY_BACK_LONG_PRESS = "hardware_keys_back_long_press";
    private static final String KEY_HOME_LONG_PRESS = "hardware_keys_home_long_press";
    private static final String KEY_HOME_DOUBLE_TAP = "hardware_keys_home_double_tap";
    private static final String KEY_MENU_PRESS = "hardware_keys_menu_press";
    private static final String KEY_MENU_LONG_PRESS = "hardware_keys_menu_long_press";
    private static final String KEY_ASSIST_PRESS = "hardware_keys_assist_press";
    private static final String KEY_ASSIST_LONG_PRESS = "hardware_keys_assist_long_press";
    private static final String KEY_APP_SWITCH_PRESS = "hardware_keys_app_switch_press";
    private static final String KEY_APP_SWITCH_LONG_PRESS = "hardware_keys_app_switch_long_press";
    private static final String DISABLE_NAV_KEYS = "disable_nav_keys";
    private static final String KEY_SWAP_CAPACITIVE_KEYS = "swap_capacitive_keys";

    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";
    private static final String CATEGORY_CAMERA = "camera_key";

    private ListPreference mBackLongPressAction;
    private ListPreference mHomeLongPressAction;
    private ListPreference mHomeDoubleTapAction;
    private ListPreference mMenuPressAction;
    private ListPreference mMenuLongPressAction;
    private ListPreference mAssistPressAction;
    private ListPreference mAssistLongPressAction;
    private ListPreference mAppSwitchPressAction;
    private ListPreference mAppSwitchLongPressAction;
    private ListPreference mNavigationBackLongPressAction;
    private ListPreference mNavigationHomeLongPressAction;
    private ListPreference mNavigationHomeDoubleTapAction;
    private ListPreference mNavigationAppSwitchLongPressAction;
    private ListPreference mEdgeLongSwipeAction;
    private SwitchPreference mCameraWakeScreen;
    private SwitchPreference mCameraSleepOnRelease;
    private SwitchPreference mCameraLaunch;
    private SwitchPreference mDisableNavigationKeys;
    private SecureSettingSwitchPreference mSwapCapacitiveKeys;

    private Handler mHandler;

    // Nav
    private static final String KEY_NAV_MENU_ARROW_KEYS = "navigation_bar_menu_arrow_keys";
    private static final String KEY_NAV_INVERSE = "sysui_nav_bar_inverse";
    private static final String KEY_NAV_GESTURES = "navbar_gestures";
    private static final String KEY_NAV_COMPACT_LAYOUT = "navigation_bar_compact_layout";
    private static final String KEY_NAVIGATION_BACK_LONG_PRESS =
            "navigation_back_long_press";
    private static final String KEY_NAVIGATION_HOME_LONG_PRESS = "navigation_home_long_press";
    private static final String KEY_NAVIGATION_HOME_DOUBLE_TAP = "navigation_home_double_tap";
    private static final String KEY_NAVIGATION_APP_SWITCH_LONG_PRESS =
            "navigation_app_switch_long_press";
    private static final String KEY_EDGE_LONG_SWIPE = "navigation_bar_edge_long_swipe";

    // Plus
    private static final String KEY_POWER_END_CALL = "power_end_call";
    private static final String KEY_HOME_ANSWER_CALL = "home_answer_call";

    private static final String CATEGORY_BACKLIGHT = "button_backlight_cat";
    private static final String CATEGORY_SWAP_CAPACITIVE_KEYS = "swap_capacitive_keys_cat";
    private static final String CATEGORY_NAVBAR = "navbar_key";
    private static final String CATEGORY_NAVBAR_ACTIONS = "navbar_actions_category";
    private static final String CATEGORY_POWER = "power_key";

    private SystemSettingSwitchPreference mNavigationMenuArrowKeys;
    private SecureSettingSwitchPreference mNavigationInverse;
    private Preference mNavigationGestures;
    private SystemSettingSwitchPreference mNavigationCompactLayout;
    private SwitchPreference mPowerEndCall;
    private SwitchPreference mHomeAnswerCall;
    private PreferenceCategory mNavbarCategory;
    private PreferenceCategory mNavbarActionsCategory;

    private IOverlayManager mOverlayManager;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.evolution_settings_buttons;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final int deviceKeys = res.getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        final int deviceWakeKeys = res.getInteger(
                com.android.internal.R.integer.config_deviceHardwareWakeKeys);

        final boolean hasHomeKey = ButtonSettingsUtils.hasHomeKey(getActivity());
        final boolean hasBackKey = ButtonSettingsUtils.hasBackKey(getActivity());
        final boolean hasMenuKey = ButtonSettingsUtils.hasMenuKey(getActivity());
        final boolean hasAssistKey = ButtonSettingsUtils.hasAssistKey(getActivity());
        final boolean hasAppSwitchKey = ButtonSettingsUtils.hasAppSwitchKey(getActivity());
        final boolean hasCameraKey = ButtonSettingsUtils.hasCameraKey(getActivity());
        final boolean hasPowerKey = ButtonSettingsUtils.hasPowerKey();

        final boolean showHomeWake = ButtonSettingsUtils.canWakeUsingHomeKey(getActivity());
        final boolean showBackWake = ButtonSettingsUtils.canWakeUsingBackKey(getActivity());
        final boolean showMenuWake = ButtonSettingsUtils.canWakeUsingMenuKey(getActivity());
        final boolean showAssistWake = ButtonSettingsUtils.canWakeUsingAssistKey(getActivity());
        final boolean showAppSwitchWake = ButtonSettingsUtils.canWakeUsingAppSwitchKey(getActivity());
        final boolean showCameraWake = ButtonSettingsUtils.canWakeUsingCameraKey(getActivity());

        boolean hasAnyBindableKey = false;
        final PreferenceCategory homeCategory = prefScreen.findPreference(CATEGORY_HOME);
        final PreferenceCategory backCategory = prefScreen.findPreference(CATEGORY_BACK);
        final PreferenceCategory menuCategory = prefScreen.findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory = prefScreen.findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory = prefScreen.findPreference(CATEGORY_APPSWITCH);
        final PreferenceCategory cameraCategory = prefScreen.findPreference(CATEGORY_CAMERA);
        final PreferenceCategory backlightCat = findPreference(CATEGORY_BACKLIGHT);
        final PreferenceCategory swapCapacitiveKeysCat = findPreference(CATEGORY_SWAP_CAPACITIVE_KEYS);
        final PreferenceCategory powerCategory = prefScreen.findPreference(CATEGORY_POWER);

        mHandler = new Handler();

        // Force Navigation bar related options
        mDisableNavigationKeys = findPreference(DISABLE_NAV_KEYS);
        mNavbarCategory = prefScreen.findPreference(CATEGORY_NAVBAR);
        mNavbarActionsCategory = prefScreen.findPreference(CATEGORY_NAVBAR_ACTIONS);
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
        Action defaultAppSwitchPressAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_pressOnAppSwitchBehavior));
        Action defaultAppSwitchLongPressAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_longPressOnAppSwitchBehavior));
        Action defaultAssistPressAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_pressOnAssistBehavior));
        Action defaultAssistLongPressAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_longPressOnAssistBehavior));
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
            updateDisableNavkeysCategories(mDisableNavigationKeys.isChecked());
            mNavigationMenuArrowKeys.setDependency(DISABLE_NAV_KEYS);
            mNavigationInverse.setDependency(DISABLE_NAV_KEYS);
            mNavigationGestures.setDependency(DISABLE_NAV_KEYS);
            mNavigationCompactLayout.setDependency(DISABLE_NAV_KEYS);
            mNavigationBackLongPressAction.setDependency(DISABLE_NAV_KEYS);
            mNavigationHomeLongPressAction.setDependency(DISABLE_NAV_KEYS);
            mNavigationHomeDoubleTapAction.setDependency(DISABLE_NAV_KEYS);
            mNavigationAppSwitchLongPressAction.setDependency(DISABLE_NAV_KEYS);
            mEdgeLongSwipeAction.setDependency(DISABLE_NAV_KEYS);
        } else {
            mNavbarCategory.removePreference(mDisableNavigationKeys);
            mNavbarActionsCategory.removePreference(mDisableNavigationKeys);
            mDisableNavigationKeys = null;
        }

        if (hasHomeKey) {
            if (!showHomeWake) {
                homeCategory.removePreference(findPreference(Settings.System.HOME_WAKE_SCREEN));
            }

            if (!Utils.isVoiceCapable(getActivity())) {
                homeCategory.removePreference(mHomeAnswerCall);
                mHomeAnswerCall = null;
            }

            mHomeLongPressAction = initList(KEY_HOME_LONG_PRESS, homeLongPressAction);
            mHomeDoubleTapAction = initList(KEY_HOME_DOUBLE_TAP, homeDoubleTapAction);

            hasAnyBindableKey = true;
        }
        if (!hasHomeKey || homeCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(homeCategory);
        }

        if (hasBackKey) {
            if (!showBackWake) {
                backCategory.removePreference(findPreference(Settings.System.BACK_WAKE_SCREEN));
            }

            mBackLongPressAction = initList(KEY_BACK_LONG_PRESS, backLongPressAction);
            if (mDisableNavigationKeys.isChecked()) {
                mBackLongPressAction.setEnabled(false);
            }

            hasAnyBindableKey = true;
        }
        if (!hasBackKey || backCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(backCategory);
        }

        if (hasMenuKey) {
            if (!showMenuWake) {
                menuCategory.removePreference(findPreference(Settings.System.MENU_WAKE_SCREEN));
            }

            Action pressAction = Action.fromSettings(resolver,
                    Settings.System.KEY_MENU_ACTION, Action.MENU);
            mMenuPressAction = initList(KEY_MENU_PRESS, pressAction);

            Action longPressAction = Action.fromSettings(resolver,
                        Settings.System.KEY_MENU_LONG_PRESS_ACTION,
                        hasAssistKey ? Action.NOTHING : Action.APP_SWITCH);
            mMenuLongPressAction = initList(KEY_MENU_LONG_PRESS, longPressAction);

            hasAnyBindableKey = true;
        }
        if (!hasMenuKey || menuCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(menuCategory);
        }

        if (hasAssistKey) {
            if (!showAssistWake) {
                assistCategory.removePreference(findPreference(Settings.System.ASSIST_WAKE_SCREEN));
            }

            Action pressAction = Action.fromSettings(resolver,
                    Settings.System.KEY_ASSIST_ACTION, defaultAssistPressAction);
            mAssistPressAction = initList(KEY_ASSIST_PRESS, pressAction);

            Action longPressAction = Action.fromSettings(resolver,
                    Settings.System.KEY_ASSIST_LONG_PRESS_ACTION, defaultAssistLongPressAction);
            mAssistLongPressAction = initList(KEY_ASSIST_LONG_PRESS, longPressAction);

            hasAnyBindableKey = true;
        }
        if (!hasAssistKey || assistCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(assistCategory);
        }

        if (hasAppSwitchKey) {
            if (!showAppSwitchWake) {
                appSwitchCategory.removePreference(findPreference(
                        Settings.System.APP_SWITCH_WAKE_SCREEN));
            }

            Action pressAction = Action.fromSettings(resolver,
                    Settings.System.KEY_APP_SWITCH_ACTION, defaultAppSwitchPressAction);
            mAppSwitchPressAction = initList(KEY_APP_SWITCH_PRESS, pressAction);

            mAppSwitchLongPressAction = initList(KEY_APP_SWITCH_LONG_PRESS, appSwitchLongPressAction);

            hasAnyBindableKey = true;
        }
        if (!hasAppSwitchKey || appSwitchCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(appSwitchCategory);
        }

        if (hasCameraKey) {
            mCameraWakeScreen = findPreference(Settings.System.CAMERA_WAKE_SCREEN);
            mCameraSleepOnRelease = findPreference(Settings.System.CAMERA_SLEEP_ON_RELEASE);
            mCameraLaunch = findPreference(Settings.System.CAMERA_LAUNCH);

            if (!showCameraWake) {
                prefScreen.removePreference(mCameraWakeScreen);
            }
            // Only show 'Camera sleep on release' if the device has a focus key
            if (res.getBoolean(com.android.internal.R.bool.config_singleStageCameraKey)) {
                prefScreen.removePreference(mCameraSleepOnRelease);
            }
        }
        if (!hasCameraKey || cameraCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(cameraCategory);
        }

        final ButtonBacklightBrightness backlight = findPreference(KEY_BUTTON_BACKLIGHT);
        if (!ButtonSettingsUtils.hasButtonBacklightSupport(getActivity())
                && !ButtonSettingsUtils.hasKeyboardBacklightSupport(getActivity())) {
            prefScreen.removePreference(backlightCat);
        }

        if (mCameraWakeScreen != null) {
            if (mCameraSleepOnRelease != null && !res.getBoolean(
                    com.android.internal.R.bool.config_singleStageCameraKey)) {
                mCameraSleepOnRelease.setDependency(Settings.System.CAMERA_WAKE_SCREEN);
            }
        }

        // Power button
        mPowerEndCall = findPreference(KEY_POWER_END_CALL);

        if (hasPowerKey) {
            if (!Utils.isVoiceCapable(getActivity())) {
                powerCategory.removePreference(mPowerEndCall);
                mPowerEndCall = null;
            }
        } else {
            prefScreen.removePreference(powerCategory);
        }

        // Navigation bar modes
        updateNavigationBarModeState();

        // Edge swipe gesture
        updateEdgeSwipeGesturePreference();
    }

    private static boolean isKeySwapperSupported(Context context) {
        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(context);
        return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_SWAP);
    }

    private void updateNavigationBarModeState(){
        final ContentResolver resolver = getActivity().getContentResolver();
        String mode = NavbarUtils.getNavigationBarModeOverlay(getActivity(), mOverlayManager);
        if (!mode.equals(NAV_BAR_MODE_3BUTTON_OVERLAY) && !mode.equals(NAV_BAR_MODE_2BUTTON_OVERLAY)){
            if (mNavigationMenuArrowKeys != null){
                mNavbarCategory.removePreference(mNavigationMenuArrowKeys);
                mNavigationMenuArrowKeys = null;
            }
            if (mNavigationInverse != null){
                mNavbarCategory.removePreference(mNavigationInverse);
                mNavigationInverse = null;
            }
            if (mNavigationCompactLayout != null){
                mNavbarCategory.removePreference(mNavigationCompactLayout);
                mNavigationCompactLayout = null;
            }
            if (mNavigationBackLongPressAction != null){
                mNavbarActionsCategory.removePreference(mNavigationBackLongPressAction);
                mNavigationBackLongPressAction = null;
            }
            if (mNavigationHomeLongPressAction != null){
                mNavbarActionsCategory.removePreference(mNavigationHomeLongPressAction);
                mNavigationHomeLongPressAction = null;
            }
            if (mNavigationHomeDoubleTapAction != null){
                mNavbarActionsCategory.removePreference(mNavigationHomeDoubleTapAction);
                mNavigationHomeDoubleTapAction = null;
            }
            if (mNavigationAppSwitchLongPressAction != null){
                mNavbarActionsCategory.removePreference(mNavigationAppSwitchLongPressAction);
                mNavigationAppSwitchLongPressAction = null;
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
        if (mNavbarActionsCategory != null && mNavbarActionsCategory.getPreferenceCount() == 0){
            getPreferenceScreen().removePreference(mNavbarActionsCategory);
            mNavbarActionsCategory = null;
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

        // Power button ends calls.
        if (mPowerEndCall != null) {
            final int incallPowerBehavior = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_DEFAULT);
            final boolean powerButtonEndsCall =
                    (incallPowerBehavior == Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP);
            mPowerEndCall.setChecked(powerButtonEndsCall);
        }

        // Home button answers calls.
        if (mHomeAnswerCall != null) {
            final int incallHomeBehavior = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.RING_HOME_BUTTON_BEHAVIOR,
                    Settings.Secure.RING_HOME_BUTTON_BEHAVIOR_DEFAULT);
            final boolean homeButtonAnswersCall =
                (incallHomeBehavior == Settings.Secure.RING_HOME_BUTTON_BEHAVIOR_ANSWER);
            mHomeAnswerCall.setChecked(homeButtonAnswersCall);
        }

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

    private void handleTogglePowerButtonEndsCallPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR, (mPowerEndCall.isChecked()
                        ? Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP
                        : Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_SCREEN_OFF));
    }

    private void handleToggleHomeButtonAnswersCallPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.RING_HOME_BUTTON_BEHAVIOR, (mHomeAnswerCall.isChecked()
                        ? Settings.Secure.RING_HOME_BUTTON_BEHAVIOR_ANSWER
                        : Settings.Secure.RING_HOME_BUTTON_BEHAVIOR_DO_NOTHING));
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
        if (preference == mBackLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_BACK_LONG_PRESS_ACTION);
            return true;
        }else if (preference == mHomeLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mHomeDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mMenuPressAction) {
            handleListChange(mMenuPressAction, newValue,
                    Settings.System.KEY_MENU_ACTION);
            return true;
        } else if (preference == mMenuLongPressAction) {
            handleListChange(mMenuLongPressAction, newValue,
                    Settings.System.KEY_MENU_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAssistPressAction) {
            handleListChange(mAssistPressAction, newValue,
                    Settings.System.KEY_ASSIST_ACTION);
            return true;
        } else if (preference == mAssistLongPressAction) {
            handleListChange(mAssistLongPressAction, newValue,
                    Settings.System.KEY_ASSIST_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAppSwitchPressAction) {
            handleListChange(mAppSwitchPressAction, newValue,
                    Settings.System.KEY_APP_SWITCH_ACTION);
            return true;
        } else if (preference == mAppSwitchLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mNavigationBackLongPressAction) {
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

    private void updateDisableNavkeysCategories(boolean navbarEnabled) {
        final PreferenceScreen prefScreen = getPreferenceScreen();

        /* Disable hw-key options if they're disabled */
        final PreferenceCategory homeCategory = prefScreen.findPreference(CATEGORY_HOME);
        final PreferenceCategory backCategory = prefScreen.findPreference(CATEGORY_BACK);
        final PreferenceCategory menuCategory = prefScreen.findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory = prefScreen.findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory = prefScreen.findPreference(CATEGORY_APPSWITCH);
        final ButtonBacklightBrightness backlight = prefScreen.findPreference(KEY_BUTTON_BACKLIGHT);

        /* Toggle backlight control depending on navbar state, force it to
           off if enabling */
        if (backlight != null) {
            backlight.setEnabled(!navbarEnabled);
            backlight.updateSummary();
        }

        if (backCategory != null) {
            if (mBackLongPressAction != null) {
                mBackLongPressAction.setEnabled(!navbarEnabled);
            }
        }
        if (homeCategory != null) {
            if (mHomeAnswerCall != null) {
                mHomeAnswerCall.setEnabled(!navbarEnabled);
            }
            if (mHomeLongPressAction != null) {
                mHomeLongPressAction.setEnabled(!navbarEnabled);
            }
            if (mHomeDoubleTapAction != null) {
                mHomeDoubleTapAction.setEnabled(!navbarEnabled);
            }
        }
        if (backCategory != null) {
            backCategory.setEnabled(!navbarEnabled);
        }
        if (menuCategory != null) {
            if (mMenuPressAction != null) {
                mMenuPressAction.setEnabled(!navbarEnabled);
            }
            if (mMenuLongPressAction != null) {
                mMenuLongPressAction.setEnabled(!navbarEnabled);
            }
        }
        if (assistCategory != null) {
            if (mAssistPressAction != null) {
                mAssistPressAction.setEnabled(!navbarEnabled);
            }
            if (mAssistLongPressAction != null) {
                mAssistLongPressAction.setEnabled(!navbarEnabled);
            }
        }
        if (appSwitchCategory != null) {
            if (mAppSwitchPressAction != null) {
                mAppSwitchPressAction.setEnabled(!navbarEnabled);
            }
            if (mAppSwitchLongPressAction != null) {
                mAppSwitchLongPressAction.setEnabled(!navbarEnabled);
            }
        }
        if (mSwapCapacitiveKeys != null){
            mSwapCapacitiveKeys.setEnabled(!navbarEnabled);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mDisableNavigationKeys) {
            mDisableNavigationKeys.setEnabled(false);
            writeDisableNavkeysOption(mDisableNavigationKeys.isChecked());
            updateDisableNavkeysOption();
            updateDisableNavkeysCategories(true);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mDisableNavigationKeys.setEnabled(true);
                        updateDisableNavkeysCategories(mDisableNavigationKeys.isChecked());
                    }catch(Exception e){
                    }
                }
            }, 1000);
        } else if (preference == mPowerEndCall) {
            handleTogglePowerButtonEndsCallPreferenceClick();
            return true;
        } else if (preference == mHomeAnswerCall) {
            handleToggleHomeButtonAnswersCallPreferenceClick();
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference.getKey() == null) {
            // Auto-key preferences that don't have a key, so the dialog can find them.
            preference.setKey(UUID.randomUUID().toString());
        }
        DialogFragment f = null;
        if (preference instanceof CustomDialogPreference) {
            f = CustomDialogPreference.CustomPreferenceDialogFragment
                    .newInstance(preference.getKey());
        } else {
            super.onDisplayPreferenceDialog(preference);
            return;
        }
        f.setTargetFragment(this, 0);
        f.show(getFragmentManager(), "dialog_preference");
        onDialogShowing();
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
            new BaseSearchIndexProvider(R.xml.evolution_settings_buttons);
}
