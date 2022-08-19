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

package com.evolution.settings.preference

import android.content.Context
import android.provider.Settings
import android.util.AttributeSet

class GlobalSettingPrimarySwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
): SettingPrimarySwitchPreference(context, attrs) {
    init {
        setPreferenceDataStore(GlobalSettingsStore(context.contentResolver))
    }

    override fun getUri() = Settings.Global.getUriFor(key)
}
