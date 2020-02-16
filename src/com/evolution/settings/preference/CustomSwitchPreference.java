/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.evolution.settings.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.SwitchPreference;

import com.android.settings.R;

public class CustomSwitchPreference extends SwitchPreference {

    public CustomSwitchPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference_material_settings);
    }

    public CustomSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_material_settings);
    }
}
