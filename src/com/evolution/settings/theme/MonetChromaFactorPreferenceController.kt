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

package com.evolution.settings.theme

import android.content.Context
import android.os.UserHandle
import android.provider.Settings

import androidx.preference.Preference

import com.android.settings.core.BasePreferenceController
import com.evolution.settings.preference.CustomSeekBarPreference

class MonetChromaFactorPreferenceController(
    context: Context,
    key: String,
) : BasePreferenceController(context, key),
    Preference.OnPreferenceChangeListener {

    override fun getAvailabilityStatus(): Int = AVAILABLE

    override fun updateState(preference: Preference) {
        super.updateState(preference)
        val chromaFactor = Settings.Secure.getFloatForUser(
            mContext.contentResolver,
            Settings.Secure.MONET_ENGINE_CHROMA_FACTOR,
            CHROMA_DEFAULT,
            UserHandle.USER_CURRENT
        ) * 100
        (preference as CustomSeekBarPreference).apply {
            setValue(chromaFactor.toInt())
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        return Settings.Secure.putFloatForUser(
            mContext.contentResolver,
            Settings.Secure.MONET_ENGINE_CHROMA_FACTOR,
            (newValue as Int) / 100f,
            UserHandle.USER_CURRENT
        )
    }

    companion object {
        private const val CHROMA_DEFAULT = 1f
    }
}
