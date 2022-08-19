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

package com.evolution.settings.qs

import android.content.Context

import androidx.preference.Preference

import com.android.settings.core.BasePreferenceController
import com.evolution.settings.preference.SystemSettingEditTextPreference

class QSFooterTextPreferenceController(
    context: Context,
    key: String,
) : BasePreferenceController(context, key),
    Preference.OnPreferenceChangeListener {

    override fun getAvailabilityStatus(): Int = AVAILABLE

    override fun updateState(preference: Preference) {
        super.updateState(preference)
        (preference as SystemSettingEditTextPreference).apply {
            if (text == null || text.isBlank()) {
                text = DEFAULT_TEXT
            }
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (newValue is String && newValue.isBlank()) {
            (preference as SystemSettingEditTextPreference).text = DEFAULT_TEXT
            return false
        }
        return true
    }

    companion object {
        private const val DEFAULT_TEXT = "Evolution X"
    }
}
