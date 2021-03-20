/*
 * Copyright (C) 2010 Daniel Nilsson
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.android.settings.R;

import com.evolution.settings.ui.ColorPanelView;
import com.evolution.settings.ui.ColorPickerView;
import com.evolution.settings.ui.ColorPickerView.OnColorChangedListener;

import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.Locale;

public class ColorSelectDialog extends AlertDialog implements
        ColorPickerView.OnColorChangedListener, TextWatcher, OnFocusChangeListener {

    private static final String TAG = "ColorSelectDialog";
    private final static String STATE_KEY_COLOR = "BatteryLightDialog:color";

    private ColorPickerView mColorPicker;

    private EditText mHexColorInput;
    private ColorPanelView mNewColor;
    private LayoutInflater mInflater;
    private boolean mMultiColor = true;
    private Spinner mColorList;
    private LinearLayout mColorListView;
    private LinearLayout mColorPanelView;
    private ColorPanelView mNewListColor;
    private LedColorAdapter mLedColorAdapter;
    private boolean mWithAlpha;

    private boolean mShowLedPreview;
    private boolean mShowMultiColor;
    private NotificationManager mNoMan;
    private Context mContext;

    protected ColorSelectDialog(Context context, int initialColor, boolean showMultiColor, boolean showLedPreview, boolean withAlpha) {
        super(context);
        mContext = context;
        mShowLedPreview = showLedPreview;
        mWithAlpha = withAlpha;
        mShowMultiColor = showMultiColor;
        mMultiColor =
                (getContext().getResources().getBoolean(R.bool.config_has_multi_color_led) || mShowMultiColor);
        init(initialColor);
    }

    private void init(int color) {
        // To fight color banding.
        getWindow().setFormat(PixelFormat.RGBA_8888);
        setUp(color);
    }

    /**
     * This function sets up the dialog with the proper values.  If the speedOff parameters
     * has a -1 value disable both spinners
     *
     * @param color - the color to set
     * @param speedOn - the flash time in ms
     * @param speedOff - the flash length in ms
     */
    private void setUp(int color) {
        mInflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = mInflater.inflate(R.layout.dialog_battery_settings, null);

        mNoMan = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        mColorPicker = (ColorPickerView) layout.findViewById(R.id.color_picker_view);
        mHexColorInput = (EditText) layout.findViewById(R.id.hex_color_input);
        mNewColor = (ColorPanelView) layout.findViewById(R.id.color_panel);
        mColorPanelView = (LinearLayout) layout.findViewById(R.id.color_panel_view);

        mColorListView = (LinearLayout) layout.findViewById(R.id.color_list_view);
        mColorList = (Spinner) layout.findViewById(R.id.color_list_spinner);
        mNewListColor = (ColorPanelView) layout.findViewById(R.id.color_list_panel);

        mColorPicker.setOnColorChangedListener(this);
        mHexColorInput.setOnFocusChangeListener(this);
        setAlphaSliderVisible(mWithAlpha);
        mColorPicker.setColor(color, true);
        showLed(color);

        mColorList = (Spinner) layout.findViewById(R.id.color_list_spinner);
        mLedColorAdapter = new LedColorAdapter(
                R.array.entries_led_colors,
                R.array.values_led_colors);
        mColorList.setAdapter(mLedColorAdapter);
        mColorList.setSelection(mLedColorAdapter.getColorPosition(color));
        mColorList.setOnItemSelectedListener(mColorListListener);

        setView(layout);

        // show and hide the correct UI depending if we have multi-color led or not
        if (mMultiColor){
            mColorListView.setVisibility(View.GONE);
            mColorPicker.setVisibility(View.VISIBLE);
            mColorPanelView.setVisibility(View.VISIBLE);
        } else {
            mColorListView.setVisibility(View.VISIBLE);
            mColorPicker.setVisibility(View.GONE);
            mColorPanelView.setVisibility(View.GONE);
        }
    }

    private AdapterView.OnItemSelectedListener mColorListListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int color = mLedColorAdapter.getColor(position);
            mNewListColor.setColor(color);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(STATE_KEY_COLOR, getColor());
        switchOffLed();
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mColorPicker.setColor(state.getInt(STATE_KEY_COLOR), true);
    }

    @Override
    public void onColorChanged(int color) {
        final boolean hasAlpha = mWithAlpha;
        final String format = hasAlpha ? "%08x" : "%06x";
        final int mask = hasAlpha ? 0xFFFFFFFF : 0x00FFFFFF;

        mNewColor.setColor(color);
        mHexColorInput.setText(String.format(Locale.US, format, color & mask));

        showLed(color);
    }

    private void showLed(int color) {
        if (mShowLedPreview) {
            if (color == 0xFFFFFFFF) {
                // argb white doesn't work
                color = 0xffffff;
            }
            mNoMan.forceShowLedLight(color);
        }
    }

    public void switchOffLed() {
        if (mShowLedPreview) {
            mNoMan.forceShowLedLight(-1);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        switchOffLed();
    }

    public void setAlphaSliderVisible(boolean visible) {
        mHexColorInput.setFilters(new InputFilter[] { new InputFilter.LengthFilter(visible ? 8 : 6) } );
        mColorPicker.setAlphaSliderVisible(visible);
    }

    public int getColor() {
        if (mMultiColor){
            return mColorPicker.getColor();
        } else {
            return mNewListColor.getColor();
        }
    }

    class LedColorAdapter extends BaseAdapter implements SpinnerAdapter {
        private ArrayList<Pair<String, Integer>> mColors;

        public LedColorAdapter(int ledColorResource, int ledValueResource) {
            mColors = new ArrayList<Pair<String, Integer>>();

            String[] color_names = getContext().getResources().getStringArray(ledColorResource);
            String[] color_values = getContext().getResources().getStringArray(ledValueResource);

            for(int i = 0; i < color_values.length; ++i) {
                try {
                    int color = Color.parseColor(color_values[i]);
                    mColors.add(new Pair<String, Integer>(color_names[i], color));
                } catch (IllegalArgumentException ex) {
                    // Number format is incorrect, ignore entry
                }
            }
        }

        /**
         * Will return the position of the spinner entry with the specified
         * color. Returns 0 if there is no such entry.
         */
        public int getColorPosition(int color) {
            for (int position = 0; position < getCount(); ++position) {
                if (getItem(position).second.equals(color)) {
                    return position;
                }
            }

            return 0;
        }

        public int getColor(int position) {
            Pair<String, Integer> item = getItem(position);
            if (item != null){
                return item.second;
            }

            // -1 is white
            return -1;
        }

        @Override
        public int getCount() {
            return mColors.size();
        }

        @Override
        public Pair<String, Integer> getItem(int position) {
            return mColors.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = mInflater.inflate(R.layout.led_color_item, null);
            }

            Pair<String, Integer> entry = getItem(position);
            ((TextView) view.findViewById(R.id.textViewName)).setText(entry.first);

            return view;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String hexColor = mHexColorInput.getText().toString();
        if (!hexColor.isEmpty()) {
            try {
                int color = Color.parseColor('#' + hexColor);
                if (!mWithAlpha) {
                    color |= 0xFF000000; // set opaque
                }
                mColorPicker.setColor(color);
                mNewColor.setColor(color);
            } catch (IllegalArgumentException ex) {
                // Number format is incorrect, ignore
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            mHexColorInput.removeTextChangedListener(this);
            InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } else {
            mHexColorInput.addTextChangedListener(this);
        }
    }
}
