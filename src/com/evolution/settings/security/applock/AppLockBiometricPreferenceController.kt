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
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG

import androidx.preference.Preference
import androidx.preference.PreferenceScreen

import com.evolution.settings.EvolutionTogglePreferenceController

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val KEY = "app_lock_biometrics_allowed"

class AppLockBiometricPreferenceController(
    context: Context,
    private val coroutineScope: CoroutineScope
) : EvolutionTogglePreferenceController(context, KEY) {

    private val appLockManager = context.getSystemService(AppLockManager::class.java)
    private val biometricManager = context.getSystemService(BiometricManager::class.java)

    private var preference: Preference? = null
    private var isBiometricsAllowed = false

    init {
        coroutineScope.launch {
            isBiometricsAllowed = withContext(Dispatchers.Default) {
                appLockManager.isBiometricsAllowed()
            }
            preference?.let {
                updateState(it)
            }
        }
    }

    override fun getAvailabilityStatus(): Int {
        val result = biometricManager.canAuthenticate(BIOMETRIC_STRONG)
        return if (result == BiometricManager.BIOMETRIC_SUCCESS) AVAILABLE else CONDITIONALLY_UNAVAILABLE
    }

    override fun isChecked() = isBiometricsAllowed

    override fun setChecked(checked: Boolean): Boolean {
        if (isBiometricsAllowed == checked) return false
        isBiometricsAllowed = checked
        coroutineScope.launch(Dispatchers.Default) {
            appLockManager.setBiometricsAllowed(isBiometricsAllowed)
        }
        return true
    }

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        preference = screen.findPreference(preferenceKey)
    }
}
