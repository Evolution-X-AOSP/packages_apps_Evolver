/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.evolution.settings.preference

import android.content.Context
import android.content.res.TypedArray
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Switch

import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceViewHolder

import com.android.settingslib.widget.TwoTargetPreference
import com.android.settings.R

/**
 * A primary switch that can observe changes to setting value and update
 * state accordingly. Based off PrimarySwitchPreference from Settings but
 * stripped of RestrictedPreference bits.
 */
abstract class SettingPrimarySwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
): TwoTargetPreference(context, attrs) {

    private var checkedSet = false
    private var checked = false
    var switch: Switch? = null

    private val observe: Boolean
    private var ignoreSettingsChange = false
    private val observer = object: ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            if (!ignoreSettingsChange) {
                setChecked(getPersistedBoolean(checked))
            }
        }
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.SettingPrimarySwitchPreference).use {
            observe = it.getBoolean(R.styleable.SettingPrimarySwitchPreference_observe, false)
        }
    }

    override protected fun getSecondTargetResId(): Int = R.layout.preference_widget_primary_switch

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        switch = (holder.findViewById(R.id.switchWidget) as? Switch)?.also {
            it.setContentDescription(getTitle())
            it.setChecked(checked)
        }
        holder.findViewById(android.R.id.widget_frame)?.let {
            it.setOnClickListener {
                val newValue = !isChecked()
                if (callChangeListener(newValue)) {
                    setChecked(newValue)
                }
            }
        }
    }

    override fun onAttached() {
        super.onAttached()
        if (observe) {
            context.contentResolver.registerContentObserver(getUri(), false, observer)
        }
    }

    abstract fun getUri(): Uri

    override fun onDetached() {
        if (observe) {
            context.contentResolver.unregisterContentObserver(observer)
        }
        super.onDetached()
    }

    fun isChecked() = checked

    fun setChecked(checked: Boolean) {
        val changed = this.checked != checked
        // Always persist/notify the first time; don't assume the field's default of false.
        if (changed || !checkedSet) {
            this.checked = checked
            checkedSet = true
            ignoreSettingsChange = true
            persistBoolean(checked)
            ignoreSettingsChange = false
            switch?.setChecked(checked)
            if (changed) notifyChanged()
        }
    }

    override protected fun onGetDefaultValue(a: TypedArray, index: Int) = a.getBoolean(index, false)

    override protected fun onSetInitialValue(defaultValue: Any?) {
        if (defaultValue is Boolean) {
            checked = getPersistedBoolean(defaultValue)
            switch?.setChecked(checked)
        }
    }
}
