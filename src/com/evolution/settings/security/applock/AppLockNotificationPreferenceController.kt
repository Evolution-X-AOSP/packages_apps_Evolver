/*
 * Copyright (C) 2022 AOSP-Krypton Project
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

package com.evolution.settings.security.applock

import android.app.AppLockManager
import android.content.Context
import android.os.UserHandle

import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.preference.Preference
import androidx.preference.PreferenceScreen

import com.android.internal.widget.LockPatternUtils
import com.android.settings.R
import com.android.settingslib.core.lifecycle.Lifecycle
import com.android.settings.core.BasePreferenceController

class AppLockNotificationPreferenceController(
    private val context: Context,
    lifecycle: Lifecycle?,
) : BasePreferenceController(context, KEY),
    LifecycleEventObserver {

    private val appLockManager = context.getSystemService(AppLockManager::class.java)

    private var preference: Preference? = null

    init {
        lifecycle?.addObserver(this)
    }

    override fun getAvailabilityStatus() =
        if (appLockManager.packages.isNotEmpty()) AVAILABLE else DISABLED_DEPENDENT_SETTING

    override fun onStateChanged(owner: LifecycleOwner, event: Event) {
        if (event == Event.ON_START) {
            preference?.let {
                updateState(it)
            }
        }
    }

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        preference = screen.findPreference(preferenceKey)
    }

    override fun updateState(preference: Preference) {
        if (getAvailabilityStatus() == AVAILABLE) {
            preference.setEnabled(true)
            preference.summary = context.getString(R.string.app_lock_notifications_summary)
        } else {
            preference.setEnabled(false)
            preference.summary = context.getString(R.string.app_lock_notifications_disabled_summary)
        }
    }

    companion object {
        private const val KEY = "app_lock_notifications"
    }
}
