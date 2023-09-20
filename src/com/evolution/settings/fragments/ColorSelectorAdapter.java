/*
 * Copyright (C) 2023 The LibreMobileOS Foundation
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

package com.evolution.settings.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;

import java.util.List;

public class ColorSelectorAdapter extends
        RecyclerView.Adapter<ColorSelectorAdapter.ColorSelectorViewHolder> {

    private final List<String> mColors;
    private int selectedPosition = 0;
    private final ColorSelectListener mColorSelectListener;

    public ColorSelectorAdapter(List<String> mColors, int defaultPosition,
            ColorSelectListener listener) {
        this.mColors = mColors;
        this.selectedPosition = defaultPosition;
        this.mColorSelectListener = listener;
    }

    @NonNull
    @Override
    public ColorSelectorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ColorSelectorViewHolder.from(parent, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorSelectorViewHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return mColors.size();
    }

    public static class ColorSelectorViewHolder extends RecyclerView.ViewHolder {

        private final ColorSelectorAdapter mAdapter;

        private ColorSelectorViewHolder(View view, ColorSelectorAdapter adapter) {
            super(view);
            this.mAdapter = adapter;
        }

        public static ColorSelectorViewHolder from(ViewGroup parent, ColorSelectorAdapter adapter) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View rootView = inflater
                    .inflate(R.layout.ambient_edge_light_color_selector_item, parent, false);
            return new ColorSelectorViewHolder(rootView, adapter);
        }

        public void bind() {
            int position = getAdapterPosition();
            View colorView = itemView.findViewById(R.id.color_view);
            AppCompatImageView selectIcon = itemView.findViewById(R.id.select_icon);
            String colorHex = mAdapter.mColors.get(position);
            int colorInt = Color.parseColor(colorHex);
            colorView.setBackgroundTintList(ColorStateList.valueOf(colorInt));
            int selectIconVisibility = mAdapter.selectedPosition == position
                    ? View.VISIBLE : View.GONE;
            selectIcon.setVisibility(selectIconVisibility);
            colorView.setOnClickListener(view -> {
                if (mAdapter.selectedPosition != position) {
                    int prevSelect = mAdapter.selectedPosition;
                    mAdapter.selectedPosition = position;
                    mAdapter.notifyItemChanged(prevSelect);
                    mAdapter.notifyItemChanged(position);
                    mAdapter.mColorSelectListener.onColorSelect(colorInt);
                }
            });
        }
    }

    interface ColorSelectListener {
        void onColorSelect(int color);
    }

}
