
/*
 * Copyright (C) 2012 The CyanogenMod Project
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

import com.evolution.settings.R;

public class ColorSelectPreference extends Preference implements DialogInterface.OnDismissListener {

    private static String TAG = "ColorSelectPreference";
    public static final int DEFAULT_COLOR = 0xFFFFFF; //White

    private ImageView mLightColorView;
    private Resources mResources;
    private int mColorValue;
    private Dialog mDialog;

    private boolean mShowLedPreview;
    private boolean mShowMultiColor;
    private boolean mWithAlpha;

    /**
     * @param context
     * @param attrs
     */
    public ColorSelectPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mColorValue = DEFAULT_COLOR;
        init(context, attrs);
    }

    public ColorSelectPreference(Context context, int color) {
        super(context, null);
        mColorValue = color;
        init(context, null);
    }

    public ColorSelectPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mColorValue = DEFAULT_COLOR;
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setLayoutResource(R.layout.preference_color_select);
        mResources = getContext().getResources();
        if (attrs != null) {
            final TypedArray attributes = context.obtainStyledAttributes(attrs,
                    R.styleable.ColorSelectPreference);
            final TypedValue useWithAlpha = attributes.peekValue(
                    R.styleable.ColorSelectPreference_withAlpha);
            if (useWithAlpha != null) {
                mWithAlpha = (useWithAlpha.type == TypedValue.TYPE_INT_BOOLEAN
                                && useWithAlpha.data != 0);
            }
            final TypedValue useLedPreview = attributes.peekValue(
                    R.styleable.ColorSelectPreference_ledPreview);
            if (useLedPreview != null) {
                mShowLedPreview = (useLedPreview.type == TypedValue.TYPE_INT_BOOLEAN
                                && useLedPreview.data != 0);
            }
            final TypedValue useMultiColor = attributes.peekValue(
                    R.styleable.ColorSelectPreference_multiColor);
            if (useMultiColor != null) {
                mShowMultiColor = (useMultiColor.type == TypedValue.TYPE_INT_BOOLEAN
                                && useMultiColor.data != 0);
            }
        }
    }

    public void setColor(int color) {
        mColorValue = color;
        updatePreferenceViews();
    }

    public int getColor() {
        return mColorValue;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mLightColorView = (ImageView) holder.findViewById(R.id.light_color);

        updatePreferenceViews();
    }

    private void updatePreferenceViews() {
        final int width = (int) mResources.getDimension(R.dimen.color_preference_width);
        final int height = (int) mResources.getDimension(R.dimen.color_preference_height);

        if (mLightColorView != null) {
            mLightColorView.setEnabled(true);
            mLightColorView.setImageDrawable(createRectShape(width, height, 0xFF000000 | mColorValue));
        }
    }

    @Override
    protected void onClick() {
        if (mDialog != null && mDialog.isShowing()) return;
        mDialog = getDialog();
        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    public Dialog getDialog() {
        final ColorSelectDialog d = new ColorSelectDialog(getContext(),
                0xFF000000 | mColorValue, mShowMultiColor, mShowLedPreview, mWithAlpha);

        d.setButton(AlertDialog.BUTTON_POSITIVE, mResources.getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mShowLedPreview) {
                    mColorValue =  d.getColor() & 0x00FFFFFF; // strip alpha, led does not support it
                    d.switchOffLed();
                } else {
                    mColorValue =  d.getColor();
                }
                updatePreferenceViews();
                callChangeListener(new Integer(mColorValue));
            }
        });
        d.setButton(AlertDialog.BUTTON_NEGATIVE, mResources.getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                d.switchOffLed();
            }
        });

        return d;
    }

    private static ShapeDrawable createRectShape(int width, int height, int color) {
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.setIntrinsicHeight(height);
        shape.setIntrinsicWidth(width);
        shape.getPaint().setColor(color);
        return shape;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mDialog = null;
    }

    public void setWithAlpha(boolean value) {
        mWithAlpha = value;
    }

    public void setWithLedPreview(boolean value) {
        mShowLedPreview = value;
    }

    public void setWithMultiColor(boolean value) {
        mShowMultiColor = value;
    }
}
