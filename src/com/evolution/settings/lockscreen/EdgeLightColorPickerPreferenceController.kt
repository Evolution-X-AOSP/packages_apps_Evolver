/*
 * Copyright (C) 2022 FlamingoOS Project
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

package com.evolution.settings.lockscreen

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.provider.Settings

import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.preference.Preference
import androidx.preference.PreferenceScreen

import com.android.settings.core.BasePreferenceController
import com.android.settingslib.core.lifecycle.Lifecycle

class EdgeLightColorPickerPreferenceController(
    context: Context,
    preferenceKey: String,
    lifecycle: Lifecycle?,
) : BasePreferenceController(context, preferenceKey),
    LifecycleEventObserver {

    private val settingsObserver = object : ContentObserver(
        Handler(Looper.getMainLooper())
    ) {
        override fun onChange(selfChange: Boolean) {
            preference?.let {
                updateState(it)
            }
        }
    }

    private var preference: Preference? = null

    init {
        lifecycle?.addObserver(this)
    }

    override fun onStateChanged(owner: LifecycleOwner, event: Event) {
        if (event == Event.ON_START) {
            mContext.contentResolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.EDGE_LIGHT_COLOR_MODE),
                false /* notifyDescendants */,
                settingsObserver,
            )
        } else if (event == Event.ON_STOP) {
            mContext.contentResolver.unregisterContentObserver(settingsObserver)
        }
    }

    override fun getAvailabilityStatus(): Int {
        val isCustomColorMode = Settings.System.getIntForUser(
            mContext.contentResolver,
            Settings.System.EDGE_LIGHT_COLOR_MODE,
            0, UserHandle.USER_CURRENT
        ) == 3
        return if (isCustomColorMode) {
            AVAILABLE
        } else {
            DISABLED_DEPENDENT_SETTING
        }
    }

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        preference = screen.findPreference(preferenceKey)
    }

    override fun updateState(preference: Preference) {
        super.updateState(preference)
        preference.setEnabled(getAvailabilityStatus() == AVAILABLE)
    }
}
