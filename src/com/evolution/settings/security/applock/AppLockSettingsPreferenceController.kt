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

import android.app.Activity
import android.app.AppLockManager
import android.content.Context
import android.content.Intent
import android.os.UserHandle

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.preference.Preference
import androidx.preference.PreferenceScreen

import com.android.internal.widget.LockPatternUtils
import com.android.settings.R
import com.android.settings.core.SubSettingLauncher
import com.android.settings.password.ConfirmDeviceCredentialActivity
import com.android.settings.security.SecuritySettings
import com.android.settingslib.core.lifecycle.Lifecycle
import com.android.settingslib.transition.SettingsTransitionHelper.TransitionType
import com.android.settings.core.BasePreferenceController

class AppLockSettingsPreferenceController(
    context: Context,
    preferenceKey: String,
    private val host: SecuritySettings?,
    lifecycle: Lifecycle?,
) : BasePreferenceController(context, preferenceKey),
        LifecycleEventObserver {

    private val lockPatternUtils = LockPatternUtils(context)
    private val appLockManager = context.getSystemService(AppLockManager::class.java)
    private var preference: Preference? = null
    private val securityPromptLauncher: ActivityResultLauncher<Intent>?

    init {
        lifecycle?.addObserver(this)
        securityPromptLauncher = host?.registerForActivityResult(
            StartActivityForResult()
        ) {
            if (it?.resultCode == Activity.RESULT_OK) {
                SubSettingLauncher(mContext)
                    .setDestination(AppLockSettingsFragment::class.qualifiedName)
                    .setSourceMetricsCategory(host.metricsCategory)
                    .setTransitionType(TransitionType.TRANSITION_SLIDE)
                    .addFlags(
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                            Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                    .launch()
            }
        }
    }

    override fun getAvailabilityStatus() =
        if (lockPatternUtils.isSecure(UserHandle.myUserId()))
            AVAILABLE
        else
            DISABLED_DEPENDENT_SETTING

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
            preference.summary = getSummaryForListSize(appLockManager.getPackages().size)
        } else {
            preference.setEnabled(false)
            preference.summary = mContext.getString(R.string.disabled_because_no_backup_security)
        }
    }

    private fun getSummaryForListSize(size: Int): CharSequence? =
        when {
            size == 0 -> null
            size == 1 -> mContext.getString(R.string.app_lock_summary_singular)
            else -> mContext.getString(R.string.app_lock_summary_plural, size)
        }

    override fun handlePreferenceTreeClick(preference: Preference): Boolean {
        if (preference.key == preferenceKey && securityPromptLauncher != null) {
            securityPromptLauncher.launch(
                ConfirmDeviceCredentialActivity.createIntent(
                    mContext.getString(R.string.app_lock_authentication_dialog_title),
                    null /* details */,
                )
            )
            return true
        }
        return super.handlePreferenceTreeClick(preference)
    }
}
