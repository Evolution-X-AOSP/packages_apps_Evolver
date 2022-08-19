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
import android.hardware.biometrics.BiometricManager.Authenticators

import androidx.preference.Preference
import androidx.preference.SwitchPreference

import com.android.settings.core.BasePreferenceController

class AppLockBiometricPreferenceController(
    context: Context,
    key: String,
) : BasePreferenceController(context, key),
        Preference.OnPreferenceChangeListener {

    private val appLockManager = context.getSystemService(AppLockManager::class.java)
    private val biometricManager = context.getSystemService(BiometricManager::class.java)

    override fun getAvailabilityStatus(): Int {
        val biometricsAllowed = biometricManager.canAuthenticate(
            Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        return if (biometricsAllowed)
            AVAILABLE
        else
            UNSUPPORTED_ON_DEVICE
    }

    override fun updateState(preference: Preference) {
        (preference as SwitchPreference).setChecked(appLockManager.isBiometricsAllowed())
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        appLockManager.setBiometricsAllowed(newValue as Boolean)
        return true
    }
}
