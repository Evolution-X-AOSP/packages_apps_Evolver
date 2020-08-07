/*
 * Copyright (C) 2020-2021 The Dirty Unicorns Project
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
 * limitations under the License
 */

package com.evolution.settings.preference;

import com.evolution.settings.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.preference.*;

public class SimpleCustomSecureSeekBarPreference extends CustomSecureSeekBarPreference {

    public SimpleCustomSecureSeekBarPreference(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public SimpleCustomSecureSeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleCustomSecureSeekBarPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void init(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        mContext = context;
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.CustomSeekBarPreference);

        mMax = attrs.getAttributeIntValue(ANDROIDNS, "max", 100);
        mMin = attrs.getAttributeIntValue(ANDROIDNS, "min", 0);
        mDefaultValue = attrs.getAttributeIntValue(ANDROIDNS, "defaultValue", -1);
        if (mDefaultValue > mMax) {
            mDefaultValue = mMax;
        }
        mUnits = getAttributeStringValue(attrs, SETTINGS_NS, "units", "");

        Integer id = a.getResourceId(R.styleable.CustomSeekBarPreference_units, 0);
        if (id > 0) {
            mUnits = context.getResources().getString(id);
        }

        try {
            String newInterval = attrs.getAttributeValue(SETTINGS_NS, "interval");
            if (newInterval != null)
                mInterval = Integer.parseInt(newInterval);
        } catch (Exception e) {
            Log.e(TAG, "Invalid interval value", e);
        }

        a.recycle();
        setLayoutResource(R.layout.preference_evolution_seekbar);
    }

    @Override
    protected void handleBindViewHolder(PreferenceViewHolder view) {
        view.setDividerAllowedAbove(false);

        mStatusText = (TextView) view.findViewById(R.id.seekBarPrefValue);
        mStatusText.setText(String.valueOf(mCurrentValue) + mUnits);

        mSeekBar = (SeekBar) view.findViewById(R.id.customSeekBar);
        mSeekBar.setMax(mMax);
        mSeekBar.setMin(mMin);
        mSeekBar.setProgress(mCurrentValue);
        mSeekBar.setOnSeekBarChangeListener(this);

        mTitle = (TextView) view.findViewById(android.R.id.title);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser)
            return;

        int newValue = progress;
        if (mInterval != 1 && newValue % mInterval != 0)
            newValue = Math.round(((float) newValue) / mInterval) * mInterval;

        mCurrentValue = newValue;
        if (mStatusText != null) {
            mStatusText.setText(String.valueOf(newValue) + mUnits);
        }
        persistInt(newValue);
    }
}
