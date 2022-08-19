/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.app.Activity
import android.app.AppLockManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.biometrics.BiometricConstants
import android.hardware.biometrics.BiometricManager.Authenticators
import android.hardware.biometrics.BiometricPrompt
import android.hardware.biometrics.BiometricPrompt.AuthenticationCallback
import android.hardware.biometrics.PromptInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle.USER_NULL
import android.os.UserManager
import android.util.Log
import android.view.WindowManager

import androidx.fragment.app.commit
import androidx.fragment.app.FragmentActivity

import com.android.internal.widget.LockPatternUtils
import com.android.settings.R
import com.android.settings.password.BiometricFragment
import com.android.settings.password.ConfirmDeviceCredentialUtils

class AppLockCredentialActivity : FragmentActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var lockPatternUtils: LockPatternUtils
    private lateinit var userManager: UserManager
    private lateinit var appLockManager: AppLockManager

    private var packageName: String? = null
    private var label: String? = null
    private var userId: Int = USER_NULL
    private var biometricFragment: BiometricFragment? = null
    private var goingToBackground = false
    private var waitingForBiometricCallback = false

    private val authenticationCallback = object : AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            if (!goingToBackground) {
                waitingForBiometricCallback = false
                if (errorCode == BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED
                        || errorCode == BiometricPrompt.BIOMETRIC_ERROR_CANCELED) {
                    finish()
                }
            } else if (waitingForBiometricCallback) { // goingToBackground is true
                waitingForBiometricCallback = false
                finish()
            }
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            waitingForBiometricCallback = false
            appLockManager.unlockPackage(packageName)
            ConfirmDeviceCredentialUtils.checkForPendingIntent(this@AppLockCredentialActivity)
            setResult(Activity.RESULT_OK)
            finish()
        }

        override fun onAuthenticationFailed() {
            waitingForBiometricCallback = false
        }

        override fun onSystemEvent(event: Int) {
            if (event == BiometricConstants.BIOMETRIC_SYSTEM_EVENT_EARLY_USER_CANCEL) {
                finish()
            }
        }
    }

    override protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }

        appLockManager = getSystemService(AppLockManager::class.java)
        userManager = UserManager.get(this)
        lockPatternUtils = LockPatternUtils(this)

        packageName = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME)
        if (packageName == null) {
            Log.e(TAG, "Failed to get package name, aborting unlock")
            finish()
            return
        }

        label = intent.getStringExtra(AppLockManager.EXTRA_PACKAGE_LABEL)

        userId = intent.getIntExtra(Intent.EXTRA_USER_ID, USER_NULL)
        if (userId == USER_NULL) {
            Log.e(TAG, "Invalid user id, aborting")
            finish()
            return
        }

        val biometricsAllowed = intent.getBooleanExtra(
            AppLockManager.EXTRA_ALLOW_BIOMETRICS,
            AppLockManager.DEFAULT_BIOMETRICS_ALLOWED
        )
        var allowedAuthenticators = Authenticators.DEVICE_CREDENTIAL
        if (biometricsAllowed) {
            allowedAuthenticators = allowedAuthenticators or Authenticators.BIOMETRIC_STRONG
        }

        val promptInfo = PromptInfo().apply {
            title = getString(com.android.internal.R.string.unlock_application, label)
            isDisallowBiometricsIfPolicyExists = true
            authenticators = allowedAuthenticators
            isAllowBackgroundAuthentication = true
        }

        if (isBiometricAllowed()) {
            // Don't need to check if biometrics / pin/pattern/pass are enrolled. It will go to
            // onAuthenticationError and do the right thing automatically.
            showBiometricPrompt(promptInfo)
            waitingForBiometricCallback = true
        } else {
            finish()
        }
    }

    override protected fun onStart() {
        super.onStart()
        // Translucent activity that is "visible", so it doesn't complain about finish()
        // not being called before onResume().
        setVisible(true)
    }

    override fun onPause() {
        super.onPause()
        if (!isChangingConfigurations()) {
            goingToBackground = true
            if (!waitingForBiometricCallback) {
                finish()
            }
        } else {
            goingToBackground = false
        }
    }

    // User could be locked while Effective user is unlocked even though the effective owns the
    // credential. Otherwise, biometric can't unlock fbe/keystore through
    // verifyTiedProfileChallenge. In such case, we also wanna show the user message that
    // biometric is disabled due to device restart.
    private fun isStrongAuthRequired() =
        !lockPatternUtils.isBiometricAllowedForUser(userId) ||
            !userManager.isUserUnlocked(userId)

    private fun isBiometricAllowed() =
        !isStrongAuthRequired() && !lockPatternUtils.hasPendingEscrowToken(userId)

    private fun showBiometricPrompt(promptInfo: PromptInfo) {
        biometricFragment = supportFragmentManager.findFragmentByTag(TAG_BIOMETRIC_FRAGMENT)
            as? BiometricFragment
        var newFragment = false
        if (biometricFragment == null) {
            biometricFragment = BiometricFragment.newInstance(promptInfo)
            newFragment = true
        }
        biometricFragment?.also {
            it.setCallbacks({
                handler.post(it)
            }, authenticationCallback)
            it.setUser(userId)
        }
        if (newFragment) {
            biometricFragment?.let {
                supportFragmentManager.commit {
                    add(it, TAG_BIOMETRIC_FRAGMENT)
                }
            }
        }
    }

    companion object {
        private const val TAG = "AppLockCredentialActivity"
        private const val TAG_BIOMETRIC_FRAGMENT = "fragment"
    }
}
