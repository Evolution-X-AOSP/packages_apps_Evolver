/*
 * Copyright (C) 2017 AICP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolution.settings.preferences;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class LongClickablePreference extends Preference {

    private Handler mHandler = new Handler();
    private boolean mAllowNormalClick;
    private boolean mAllowBurst;

    private int mClickableViewId = 0;
    private int mLongClickDurationMillis;
    private int mLongClickBurstMillis = 0;
    private PreferenceViewHolder mViewHolder;
    private Preference.OnPreferenceClickListener mClickListener;
    private Preference.OnPreferenceClickListener mLongClickListener;

    public LongClickablePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LongClickablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LongClickablePreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mViewHolder = holder;

        setupClickListeners();
    }

    @Override
    public void setOnPreferenceClickListener(
            Preference.OnPreferenceClickListener onPreferenceClickListener) {
        mClickListener = onPreferenceClickListener;

        setupClickListeners();
    }

    public void setOnLongClickListener(int viewId, int longClickDurationMillis,
            Preference.OnPreferenceClickListener onPreferenceClickListener) {
        mClickableViewId = viewId;
        mLongClickDurationMillis = longClickDurationMillis;
        mLongClickListener = onPreferenceClickListener;

        setupClickListeners();
    }

    private Runnable mLongClickRunnable = new Runnable() {
            @Override
            public void run() {
                mAllowNormalClick = false;
                mLongClickListener.onPreferenceClick(LongClickablePreference.this);
                if (mAllowBurst && mLongClickBurstMillis > 0) {
                    mHandler.postDelayed(this, mLongClickBurstMillis);
                }
            }
    };

    public void setLongClickBurst(int intervalMillis) {
        mLongClickBurstMillis = intervalMillis;
    }

    private void setupClickListeners() {
        // We can't put long click listener on our view without sacrificing default
        // preference click functionality, so detect long clicks manually with touch listener
        if (mClickableViewId != 0 && mViewHolder != null) {
            View view = mViewHolder.findViewById(mClickableViewId);
            if (view != null) {
                view.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getActionMasked()) {
                                case MotionEvent.ACTION_DOWN:
                                    mAllowNormalClick = true;
                                    mAllowBurst = true;
                                    mHandler.postDelayed(mLongClickRunnable,
                                            mLongClickDurationMillis);
                                    break;
                                case MotionEvent.ACTION_UP:
                                    mHandler.removeCallbacks(mLongClickRunnable);
                                    mAllowBurst = false;
                                    break;
                            }
                            return false;
                        }
                });
            }
        }
        // Use our own preference click listener to handle both normal and long clicks
        if (getOnPreferenceClickListener() == null) {
            super.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        mAllowBurst = false;
                        mHandler.removeCallbacks(mLongClickRunnable);
                        if (mAllowNormalClick) {
                            return mClickListener != null &&
                                    mClickListener.onPreferenceClick(preference);
                        } else {
                            // Long press done
                            return true;
                        }
                    }

            });
        }
    }
}
