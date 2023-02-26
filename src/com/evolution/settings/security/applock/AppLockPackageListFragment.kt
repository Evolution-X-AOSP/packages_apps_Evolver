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
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Bundle
import android.view.View

import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.forEach

import com.android.internal.util.evolution.EvolutionUtils

import com.android.settings.R
import com.android.settings.core.SubSettingLauncher
import com.android.settingslib.PrimarySwitchPreference
import com.android.settingslib.widget.TwoTargetPreference.ICON_SIZE_SMALL
import com.evolution.settings.EvolutionDashboardFragment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val TAG = AppLockPackageListFragment::class.simpleName
internal const val PACKAGE_INFO = "package_info"

class AppLockPackageListFragment : EvolutionDashboardFragment() {

    private lateinit var appLockManager: AppLockManager
    private lateinit var pm: PackageManager
    private lateinit var whiteListedPackages: Array<String>
    private lateinit var launchablePackages: List<String>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appLockManager = context.getSystemService(AppLockManager::class.java)
        pm = context.packageManager
        launchablePackages = EvolutionUtils.launchablePackages(context)
        whiteListedPackages = resources.getStringArray(
            com.android.internal.R.array.config_appLockAllowedSystemApps)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        lifecycleScope.launch {
            val selectedPackages = getSelectedPackages()
            val preferences = withContext(Dispatchers.Default) {
                pm.getInstalledPackages(
                    PackageInfoFlags.of(PackageManager.MATCH_ALL.toLong())
                ).filter {
                    !it.applicationInfo.isSystemApp() ||
                        launchablePackages.contains(it.packageName) ||
                        whiteListedPackages.contains(it.packageName)
                }.sortedWith { first, second ->
                    getLabel(first).compareTo(getLabel(second))
                }
            }.map { packageInfo ->
                createPreference(packageInfo, selectedPackages.contains(packageInfo.packageName))
            }
            preferenceScreen?.let {
                preferences.forEach { pref ->
                    it.addPreference(pref)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val selectedPackages = getSelectedPackages()
            preferenceScreen?.forEach {
                if (it is PrimarySwitchPreference) {
                    it.isChecked = selectedPackages.contains(it.key)
                }
            }
        }
    }

    private suspend fun getSelectedPackages(): Set<String> {
        return withContext(Dispatchers.IO) {
            appLockManager.packageData.map { it.packageName }.toSet()
        }
    }

    private fun getLabel(packageInfo: PackageInfo) =
        packageInfo.applicationInfo.loadLabel(pm).toString()

    private fun createPreference(packageInfo: PackageInfo, isProtected: Boolean): Preference {
        val label = getLabel(packageInfo)
        return PrimarySwitchPreference(requireContext()).apply {
            key = packageInfo.packageName
            title = label
            icon = packageInfo.applicationInfo.loadIcon(pm)
            setIconSize(ICON_SIZE_SMALL)
            isChecked = isProtected
            setOnPreferenceChangeListener { _, newValue ->
                lifecycleScope.launch(Dispatchers.IO) {
                    if (newValue as Boolean) {
                        appLockManager.addPackage(packageInfo.packageName)
                    } else {
                        appLockManager.removePackage(packageInfo.packageName)
                    }
                }
                return@setOnPreferenceChangeListener true
            }
            setOnPreferenceClickListener {
                SubSettingLauncher(requireContext())
                    .setDestination(AppLockPackageConfigFragment::class.qualifiedName)
                    .setSourceMetricsCategory(metricsCategory)
                    .setTitleText(label)
                    .setArguments(
                        Bundle(1).apply {
                            putParcelable(PACKAGE_INFO, packageInfo)
                        }
                    )
                    .launch()
                true
            }
        }
    }

    override protected fun getPreferenceScreenResId() = R.xml.app_lock_package_list_settings

    override protected fun getLogTag() = TAG
}
