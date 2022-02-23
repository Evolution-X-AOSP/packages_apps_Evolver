/*
 * Copyright (C) 2021 AOSP-Krypton Project
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

package com.evolution.settings.fragments

import android.content.Context
import android.os.UserHandle
import android.provider.Settings

import com.evolution.settings.EvolutionBasePreferenceController

class NavBarInversePreferenceController(
    context: Context,
    key: String,
): EvolutionBasePreferenceController(context, key) {

    override fun getAvailabilityStatus(): Int {
        val threeButtonMode = Settings.Secure.getIntForUser(
            mContext.contentResolver,
            Settings.Secure.NAVIGATION_MODE,
            0,
            UserHandle.USER_CURRENT,
        ) == 0
        return if (threeButtonMode)
            AVAILABLE
        else
            DISABLED_DEPENDENT_SETTING
    }
}
