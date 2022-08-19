/*
 * Copyright (C) 2021 AOSiP
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

package com.evolution.settings.preference;

import android.content.Context;
import androidx.preference.Preference;
import android.util.AttributeSet;

import com.android.settings.R;

public class AppListPreference extends Preference {

    public AppListPreference(Context context, AttributeSet attrs,
                               int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public AppListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public AppListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AppListPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        setLayoutResource(R.layout.app_list_preference_view);
    }
}
