/*
 * Copyright (C) 2023 The risingOS Android Project
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
package com.evolution.settings.fragments.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.SpannableString;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;

import androidx.fragment.app.Fragment;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.evolution.settings.fragments.fonts.FontManager;
import com.evolution.settings.fragments.fonts.FontArrayAdapter;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

public class FontPickerPreview extends SettingsPreferenceFragment {

    private Spinner fontSpinner;
    private TextView previewText;
    private FontManager fontManager;
    private ExtendedFloatingActionButton applyFab;
    private int currentFontPosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fontManager = new FontManager(getActivity());
        getActivity().setTitle(getActivity().getString(R.string.font_styles_title));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.font_picker_preview, container, false);
        fontSpinner = rootView.findViewById(R.id.font_spinner);
        previewText = rootView.findViewById(R.id.font_preview_text);
        String text = previewText.getText().toString();
        SpannableString spannableString = new SpannableString(text);
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
        int colorAccent = typedValue.data;
        int startIndex = text.indexOf("A");
        int endIndex = text.length();
        spannableString.setSpan(
            new ForegroundColorSpan(colorAccent), 
            startIndex, 
            endIndex, 
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        previewText.setText(spannableString);
        List<String> fontPackageNames = fontManager.getAllFontPackages();
        FontArrayAdapter fontAdapter = new FontArrayAdapter(
            getActivity(),
            android.R.layout.simple_spinner_dropdown_item,
            fontPackageNames,
            fontManager
        );
        fontSpinner.setAdapter(fontAdapter);
        String currentFontPackage = fontManager.getCurrentFontPackage();
        currentFontPosition = fontPackageNames.indexOf(currentFontPackage);
        if (currentFontPosition != -1) {
            fontSpinner.setSelection(currentFontPosition);
        }
        fontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFontPosition = position;
                applyFontToPreview(fontPackageNames.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        applyFab = rootView.findViewById(R.id.apply_extended_fab);
        applyFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fontManager.enableFontPackage(currentFontPosition);
            }
        });
        return rootView;
    }

    private void applyFontToPreview(String fontPackage) {
        String fontFamilyLabel = fontManager.getLabel(getContext(), fontPackage).toLowerCase();
        Typeface typeface = Typeface.create(fontFamilyLabel, Typeface.NORMAL);
        if (typeface != null && !fontFamilyLabel.equals("googlesans")) {
            previewText.setTypeface(typeface);
        } else {
            previewText.setTypeface(Typeface.create("googlesans", Typeface.NORMAL));
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EVOLVER;
    }
}
