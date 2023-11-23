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
import android.content.pm.PackageManager;
import android.content.om.OverlayInfo;
import android.graphics.Typeface;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.android.internal.util.evolution.ThemeUtils;

public class FontManager {

    private ThemeUtils mThemeUtils;

    public FontManager(Context context) {
        mThemeUtils = new ThemeUtils(context);
    }

    /**
     * Get all available fonts and return as a list of typefaces.
     */
    public List<Typeface> getFonts() {
        return mThemeUtils.getFonts();
    }

    /**
     * Get all available font packages.
     */
    public List<String> getAllFontPackages() {
        return mThemeUtils.getOverlayPackagesForCategory("android.theme.customization.font", "android");
    }

    /**
     * Get the currently selected font package.
     */
    public String getCurrentFontPackage() {
        List<OverlayInfo> overlayInfos = mThemeUtils.getOverlayInfos("android.theme.customization.font");
        return overlayInfos.stream()
                .filter(OverlayInfo::isEnabled)
                .map(OverlayInfo::getPackageName)
                .findFirst()
                .orElse("android");
    }

    /**
     * Enable a selected font package.
     */
    public void enableFontPackage(int position) {
        if (position < 0 || position >= getAllFontPackages().size()) {
            throw new IllegalArgumentException("Invalid font package position: " + position);
        }
        String selectedPackage = getAllFontPackages().get(position);
        mThemeUtils.setOverlayEnabled("android.theme.customization.font", selectedPackage, "android");
    }

    /**
     * Gets the font package label. NOTE: we use the actual font family alias for label
     */
    public String getLabel(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationInfo(pkg, 0).loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {}
        return pkg;
    }
}

