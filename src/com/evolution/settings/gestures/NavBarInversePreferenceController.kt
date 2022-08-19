/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.evolution.settings.gestures

import android.content.Context
import android.database.ContentObserver
import android.os.UserHandle
import android.provider.Settings
import android.provider.Settings.System.NAVIGATION_BAR_INVERSE
import android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_3BUTTON

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceScreen

import com.android.settings.R
import com.android.settings.core.TogglePreferenceController
import com.android.settingslib.core.lifecycle.Lifecycle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NavBarInversePreferenceController(
    context: Context,
    preferenceKey: String,
    lifecycle: Lifecycle?,
    private val host: Fragment?
) : TogglePreferenceController(context, preferenceKey),
    LifecycleEventObserver {

    private val settingsObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            host?.lifecycleScope?.launch {
                isInverted = withContext(Dispatchers.Default) {
                    Settings.System.getIntForUser(
                        mContext.contentResolver,
                        NAVIGATION_BAR_INVERSE,
                        0,
                        UserHandle.USER_CURRENT
                    ) == 1
                }
                preference?.let {
                    updateState(it)
                }
            }
        }
    }

    private var preference: Preference? = null
    private var isInverted = false

    init {
        if (getAvailabilityStatus() == AVAILABLE) {
            lifecycle?.addObserver(this)
        }
    }

    override fun onStateChanged(owner: LifecycleOwner, event: Event) {
        if (event == Event.ON_START) {
            mContext.contentResolver.registerContentObserver(
                Settings.System.getUriFor(NAVIGATION_BAR_INVERSE),
                false,
                settingsObserver,
                UserHandle.USER_CURRENT
            )
        } else if (event == Event.ON_STOP) {
            mContext.contentResolver.unregisterContentObserver(settingsObserver)
        }
    }

    override fun getAvailabilityStatus(): Int {
        val isLegacyMode = mContext.resources.getInteger(
            com.android.internal.R.integer.config_navBarInteractionMode
        ) == NAV_BAR_MODE_3BUTTON
        return if (isLegacyMode) AVAILABLE else CONDITIONALLY_UNAVAILABLE
    }

    override fun setChecked(isChecked: Boolean): Boolean {
        return host?.lifecycleScope?.launch(Dispatchers.Default) {
            Settings.System.putIntForUser(
                mContext.contentResolver,
                NAVIGATION_BAR_INVERSE,
                if (isChecked) 1 else 0,
                UserHandle.USER_CURRENT
            )
        } != null
    }

    override fun isChecked() = isInverted

    override fun getSliceHighlightMenuRes() = R.string.menu_key_system

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        preference = screen.findPreference(preferenceKey)
    }
}
