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

package com.evolution.settings.security.applock

import android.app.AppLockManager
import android.content.Context

import androidx.preference.Preference
import androidx.preference.PreferenceScreen

import com.evolution.settings.EvolutionTogglePreferenceController

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val KEY = "hide_from_launcher"

class AppLockHideAppPC(
    context: Context,
    private val packageName: String,
    private val coroutineScope: CoroutineScope
) : EvolutionTogglePreferenceController(context, KEY) {

    private val appLockManager = context.getSystemService(AppLockManager::class.java)
    private var hideFromLauncher = AppLockManager.DEFAULT_HIDE_IN_LAUNCHER
    private var preference: Preference? = null

    init {
        coroutineScope.launch {
            hideFromLauncher = withContext(Dispatchers.Default) {
                appLockManager.hiddenPackages.any { it == packageName }
            }
            preference?.let {
                updateState(it)
            }
        }
    }

    override fun getAvailabilityStatus() = AVAILABLE

    override fun isChecked() = hideFromLauncher

    override fun setChecked(checked: Boolean): Boolean {
        if (hideFromLauncher == checked) return false
        hideFromLauncher = checked
        coroutineScope.launch(Dispatchers.Default) {
            appLockManager.setPackageHidden(packageName, hideFromLauncher)
        }
        return true
    }

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        preference = screen.findPreference(preferenceKey)
    }
}
