/*
 * Copyright (C) 2020 The Evolution X Project
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
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

import com.android.settingslib.Utils;
import com.android.settingslib.widget.LayoutPreference;

public class FODIconPicker extends LayoutPreference {

    private boolean mAllowDividerAbove;
    private boolean mAllowDividerBelow;

    private View mRootView;

    private static ImageButton ButtonOne;
    private static ImageButton ButtonTwo;
    private static ImageButton ButtonThree;
    private static ImageButton ButtonFour;

    private static final String TAG = "FODIconPicker";

    public FODIconPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0 /* defStyleAttr */);
    }

    public FODIconPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference);
        mAllowDividerAbove = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerAbove,
                R.styleable.Preference_allowDividerAbove, false);
        mAllowDividerBelow = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerBelow,
                R.styleable.Preference_allowDividerBelow, false);
        a.recycle();

        a = context.obtainStyledAttributes(
                attrs, R.styleable.Preference, defStyleAttr, 0);
        int layoutResource = a.getResourceId(R.styleable.Preference_android_layout, 0);
        if (layoutResource == 0) {
            throw new IllegalArgumentException("LayoutPreference requires a layout to be defined");
        }
        a.recycle();

        // Need to create view now so that findViewById can be called immediately.
        final View view = LayoutInflater.from(getContext())
                .inflate(layoutResource, null, false);
        setView(view, context);
    }

    private void setView(View view, Context context) {
        setLayoutResource(R.layout.layout_preference_frame);
        mRootView = view;
        setShouldDisableView(false);
        ButtonOne = findViewById(R.id.fodiconone_button);
        ButtonTwo = findViewById(R.id.fodicontwo_button);
        ButtonThree = findViewById(R.id.fodiconthree_button);
        ButtonFour = findViewById(R.id.fodiconfour_button);

        int defaultfodicon = Settings.System.getInt(
                context.getContentResolver(), Settings.System.FOD_ICON, 0);
        if (defaultfodicon==0) {
            updateHighlightedItem(ButtonOne, context);
        } else if (defaultfodicon==1) {
            updateHighlightedItem(ButtonTwo, context);
        } else if (defaultfodicon==2) {
            updateHighlightedItem(ButtonThree, context);
        } else if (defaultfodicon==3) {
            updateHighlightedItem(ButtonFour, context);
        }

        ButtonOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(0, context);
                updateHighlightedItem(ButtonOne, context);
            }
        });
        ButtonTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(1, context);
                updateHighlightedItem(ButtonTwo, context);
            }
        });
        ButtonThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(2, context);
                updateHighlightedItem(ButtonThree, context);
            }
        });
        ButtonFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings(3, context);
                updateHighlightedItem(ButtonFour, context);
            }
        });
    }

    private void updateSettings(int fodicon, Context context) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.FOD_ICON, fodicon);
    }

    private void updateHighlightedItem(ImageButton activebutton, Context context) {
        int defaultcolor = context.getResources().getColor(R.color.fod_item_background_stroke_color);
        ColorStateList defaulttint = ColorStateList.valueOf(defaultcolor);
        ButtonOne.setBackgroundTintList(defaulttint);
        ButtonTwo.setBackgroundTintList(defaulttint);
        ButtonThree.setBackgroundTintList(defaulttint);
        ButtonFour.setBackgroundTintList(defaulttint);
        activebutton.setBackgroundTintList(Utils.getColorAttr(getContext(), android.R.attr.colorAccent));
    }
}
