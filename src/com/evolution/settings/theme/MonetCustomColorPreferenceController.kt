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

import com.android.settings.R
import com.android.settings.core.BasePreferenceController

class MonetCustomColorPreferenceController(
    context: Context,
    key: String,
) : BasePreferenceController(context, key) {

    override fun getAvailabilityStatus(): Int = AVAILABLE

    override fun getSummary(): CharSequence? {
        val customColor = Settings.Secure.getStringForUser(
            mContext.contentResolver,
            Settings.Secure.MONET_ENGINE_COLOR_OVERRIDE,
            UserHandle.USER_CURRENT,
        )
        return if (customColor == null || customColor.isBlank()) {
            mContext.getString(R.string.color_override_default_summary)
        } else {
            mContext.getString(
                R.string.custom_color_override_summary_placeholder,
                customColor
            )
        }
    }
}
