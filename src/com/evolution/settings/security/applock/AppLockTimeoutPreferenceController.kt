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

import androidx.preference.ListPreference
import androidx.preference.Preference

import com.android.settings.core.BasePreferenceController

class AppLockTimeoutPreferenceController(
    context: Context,
    key: String,
) : BasePreferenceController(context, key),
        Preference.OnPreferenceChangeListener {

    private val appLockManager = context.getSystemService(AppLockManager::class.java)

    override fun getAvailabilityStatus() = AVAILABLE

    override fun updateState(preference: Preference) {
        (preference as ListPreference).value = appLockManager.timeout.takeIf {
            it != -1L
        }?.toString()
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        appLockManager.timeout = (newValue as String).toLong()
        return true
    }
}
