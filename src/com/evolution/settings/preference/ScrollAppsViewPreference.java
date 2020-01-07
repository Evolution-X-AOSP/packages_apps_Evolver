/*
 * Copyright (C) 2019-2020 The Evolution X Project
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

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScrollAppsViewPreference extends Preference {
    private static final String TAG = "ScrollAppsPreference";

    private Context mContext;
    private List<String> mValues = new ArrayList<String>();
    private PackageManager mPm;
    private LayoutInflater mInflater;

    public ScrollAppsViewPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initPreference(context);
    }

    public ScrollAppsViewPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPreference(context);
    }

    public ScrollAppsViewPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPreference(context);
    }

    public void setValues(Collection<String> values) {
        mValues.clear();
        mValues.addAll(values);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        LinearLayout linearLayout = (LinearLayout) holder.findViewById(R.id.selected_apps);
        if (linearLayout.getChildCount() > 0) linearLayout.removeAllViews();

        for (String value : mValues) {
            try {
                View v = mInflater.inflate(R.layout.app_grid_item, null);
                ComponentName componentName = ComponentName.unflattenFromString(value);
                Drawable icon = mPm.getActivityIcon(componentName);
                ((ImageView) v.findViewById(R.id.appIcon)).setImageDrawable(icon);
                v.setPadding(10, 5, 10, 5);
                linearLayout.addView(v);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Set app icon", e);
            }
        }
    }

    private void initPreference(Context context) {
        mContext = context;
        setLayoutResource(R.layout.preference_selected_apps_view);
        mPm = context.getPackageManager();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
}
