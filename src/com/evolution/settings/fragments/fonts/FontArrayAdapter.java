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
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class FontArrayAdapter extends ArrayAdapter<String> {
    private SparseArray<Typeface> typefaceCache = new SparseArray<>();
    private FontManager fontManager;
    private List<String> fontPackageNames;
    private List<Typeface> typefaces;
    private Context mContext;

    public FontArrayAdapter(Context context, int textViewResourceId, List<String> objects, FontManager fontManager) {
        super(context, textViewResourceId, objects);
        this.mContext = context;
        this.fontPackageNames = objects;
        this.fontManager = fontManager;
        this.typefaces = fontManager.getFonts();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        Typeface typeface = getTypefaceForPosition(position);
        if (typeface != null) {
            view.setTypeface(typeface);
        }
        view.setText(getLabelForPosition(position));
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        Typeface typeface = getTypefaceForPosition(position);
        if (typeface != null) {
            view.setTypeface(typeface);
        }
        view.setText(getLabelForPosition(position));
        return view;
    }

    private String getLabelForPosition(int position) {
        String packageName = fontPackageNames.get(position);
        return fontManager.getLabel(mContext, packageName);
    }

    private Typeface getTypefaceForPosition(int position) {
        Typeface typeface = typefaceCache.get(position);
        if (typeface == null && position < typefaces.size()) {
            typeface = typefaces.get(position);
            typefaceCache.put(position, typeface);
        }
        return typeface;
    }
}
