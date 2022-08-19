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

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle

import androidx.lifecycle.lifecycleScope

import com.android.settings.R
import com.android.settings.widget.EntityHeaderController
import com.android.settingslib.applications.ApplicationsState.AppEntry
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.widget.LayoutPreference
import com.evolution.settings.EvolutionDashboardFragment

private val TAG = AppLockPackageConfigFragment::class.simpleName
private const val KEY_HEADER = "header_view"

class AppLockPackageConfigFragment : EvolutionDashboardFragment() {

    private lateinit var packageInfo: PackageInfo

    override fun onAttach(context: Context) {
        packageInfo = arguments?.getParcelable(PACKAGE_INFO, PackageInfo::class.java)!!
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        val appEntry = AppEntry(requireContext(), packageInfo.applicationInfo, 0)
        val header = preferenceScreen.findPreference<LayoutPreference>(KEY_HEADER)
        EntityHeaderController.newInstance(
            requireActivity(),
            this,
            header?.findViewById(R.id.entity_header)
        ).setRecyclerView(listView, settingsLifecycle)
            .setPackageName(packageInfo.packageName)
            .setButtonActions(
                EntityHeaderController.ActionType.ACTION_NONE,
                EntityHeaderController.ActionType.ACTION_NONE
            )
            .bindHeaderButtons()
            .setLabel(appEntry)
            .setIcon(appEntry)
            .done(requireActivity(), false /* rebindActions */)
    }

    override protected fun createPreferenceControllers(
        context: Context
    ) : List<AbstractPreferenceController> = listOf(
        AppLockPackageProtectionPC(context, packageInfo.packageName, lifecycleScope),
        AppLockNotificationRedactionPC(context, packageInfo.packageName, lifecycleScope),
        AppLockHideAppPC(context, packageInfo.packageName, lifecycleScope)
    )

    override protected fun getPreferenceScreenResId() = R.xml.app_lock_package_config_settings

    override protected fun getLogTag() = TAG
}
