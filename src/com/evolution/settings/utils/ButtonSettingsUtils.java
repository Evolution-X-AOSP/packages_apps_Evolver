/*
* Copyright (C) 2018 The Pixel Experience Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.evolution.settings.utils;

import android.content.Context;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

public class ButtonSettingsUtils {

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    public static final int KEY_MASK_HOME = 0x01;
    public static final int KEY_MASK_BACK = 0x02;
    public static final int KEY_MASK_MENU = 0x04;
    public static final int KEY_MASK_ASSIST = 0x08;
    public static final int KEY_MASK_APP_SWITCH = 0x10;
    public static final int KEY_MASK_CAMERA = 0x20;
    public static final int KEY_MASK_VOLUME = 0x40;

    public static int getDeviceKeys(Context context) {
        return context.getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
    }

    /* returns whether the device has home key or not. */
    public static boolean hasHomeKey(Context context) {
        return (getDeviceKeys(context) & KEY_MASK_HOME) != 0;
    }

    /* returns whether the device has back key or not. */
    public static boolean hasBackKey(Context context) {
        return (getDeviceKeys(context) & KEY_MASK_BACK) != 0;
    }

    /* returns whether the device has menu key or not. */
    public static boolean hasMenuKey(Context context) {
        return (getDeviceKeys(context) & KEY_MASK_MENU) != 0;
    }

    /* returns whether the device has assist key or not. */
    public static boolean hasAssistKey(Context context) {
        return (getDeviceKeys(context) & KEY_MASK_ASSIST) != 0;
    }

    /* returns whether the device has app switch key or not. */
    public static boolean hasAppSwitchKey(Context context) {
        return (getDeviceKeys(context) & KEY_MASK_APP_SWITCH) != 0;
    }

    /* returns whether the device supports button backlight adjusment or not. */
    public static boolean hasButtonBacklightSupport(Context context) {
        final boolean buttonBrightnessControlSupported = context.getResources().getInteger(
                com.android.internal.R.integer
                        .config_deviceSupportsButtonBrightnessControl) != 0;

        // All hardware keys besides volume and camera can possibly have a backlight
        return buttonBrightnessControlSupported
                && (hasHomeKey(context) || hasBackKey(context) || hasMenuKey(context)
                || hasAssistKey(context) || hasAppSwitchKey(context));
    }

    /* returns whether the device supports keyboard backlight adjusment or not. */
    public static boolean hasKeyboardBacklightSupport(Context context) {
        return context.getResources().getInteger(com.android.internal.R.integer
                .config_deviceSupportsKeyboardBrightnessControl) != 0;
    }
}
