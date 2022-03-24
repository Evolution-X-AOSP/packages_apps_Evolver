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
import android.os.Bundle
import android.view.View

import com.android.settings.R
import com.evolution.settings.fragment.AppListFragment

class AppLockNotificationListFragment : AppListFragment() {

    private lateinit var appLockManager: AppLockManager
    private val lockedPackages = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appLockManager = requireContext().getSystemService(AppLockManager::class.java)
        lockedPackages.addAll(appLockManager.packages)
    }

    override protected fun getTitle(): Int = R.string.app_lock_notifications_title

    override protected fun getInitialCheckedList(): List<String> =
        appLockManager.packagesWithSecureNotifications

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setDisplayCategory(CATEGORY_BOTH)
        setCustomFilter {
            lockedPackages.contains(it.packageName)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override protected fun onAppSelected(packageName: String) {
        appLockManager.setSecureNotification(packageName, true)
    }

    override protected fun onAppDeselected(packageName: String) {
        appLockManager.setSecureNotification(packageName, false)
    }
}
