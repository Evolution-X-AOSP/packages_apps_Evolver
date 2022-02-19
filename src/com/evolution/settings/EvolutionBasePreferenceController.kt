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

package com.evolution.settings

import android.content.Context
import android.util.Log

import com.android.settings.core.BasePreferenceController
import com.android.settings.core.PreferenceControllerMixin
import com.android.settingslib.search.SearchIndexableRaw

abstract class EvolutionBasePreferenceController(
    private val context: Context,
    private val key: String,
): BasePreferenceController(context, key),
        PreferenceControllerMixin {

    /**
     * Updates non-indexable keys for search provider.
     *
     * Called by SearchIndexProvider#getNonIndexableKeys
     */
    override fun updateNonIndexableKeys(keys: MutableList<String>) {
        val shouldSuppressFromSearch = !isAvailable()
                || getAvailabilityStatus() == AVAILABLE_UNSEARCHABLE
        if (shouldSuppressFromSearch) {
            if (preferenceKey?.isBlank() == true) {
                Log.w(TAG, "Skipping updateNonIndexableKeys due to empty key " + toString())
                return
            }
            if (keys.contains(key)) {
                Log.w(TAG, "Skipping updateNonIndexableKeys, key already in list. " + toString())
                return
            }
            keys.add(key)
        }
    }

    /**
     * Updates raw data for search provider.
     *
     * Called by SearchIndexProvider#getRawDataToIndex
     */
    override open fun updateRawDataToIndex(rawData: MutableList<SearchIndexableRaw>) {}

    /**
     * Updates dynamic raw data for search provider.
     *
     * Called by SearchIndexProvider#getDynamicRawDataToIndex
     */
    override open fun updateDynamicRawDataToIndex(rawData: MutableList<SearchIndexableRaw>) {}

    companion object {
        private const val TAG = "EvolutionBasePreferenceController"
    }
}
