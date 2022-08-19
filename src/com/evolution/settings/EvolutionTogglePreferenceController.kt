/*
 * Copyright (C) 2022 FlamingoOS Project
 *               2019-2022 Evolution X
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
import android.widget.Switch

import androidx.preference.Preference
import androidx.preference.PreferenceScreen

import com.android.settings.R
import com.android.settings.core.TogglePreferenceController
import com.android.settingslib.widget.MainSwitchPreference
import com.android.settingslib.widget.OnMainSwitchChangeListener

abstract class EvolutionTogglePreferenceController(
    context: Context,
    key: String,
) : TogglePreferenceController(context, key),
    OnMainSwitchChangeListener {

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        val preference = screen.findPreference<Preference>(preferenceKey) ?: return
        if (preference is MainSwitchPreference) {
            preference.addOnSwitchChangeListener(this)
        }
    }

    override fun onSwitchChanged(switchView: Switch, isChecked: Boolean) {
        setChecked(isChecked)
    }

    override fun getSliceHighlightMenuRes() = R.string.menu_key_evolver
}
