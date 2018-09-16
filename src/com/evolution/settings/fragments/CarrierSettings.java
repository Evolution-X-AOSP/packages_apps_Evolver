/*
 *  Copyright (C) 2019 The Evolution X Project
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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.evolution.settings.preferences.SystemSettingSwitchPreference;
import com.evolution.settings.preferences.SystemSettingSeekBarPreference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CarrierSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String STATUS_BAR_CARRIER_FONT_SIZE  = "status_bar_carrier_font_size";
    private static final String CARRIER_FONT_STYLE  = "status_bar_carrier_font_style";

    private PreferenceScreen mCustomCarrierLabel;
    private String mCustomCarrierLabelText;
    private SystemSettingSeekBarPreference mStatusBarCarrierSize;
    private ListPreference mCarrierFontStyle;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVO_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.evolution_settings_statusbar_carrier);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        // custom carrier label
        mCustomCarrierLabel = (PreferenceScreen) findPreference(CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();

        mStatusBarCarrierSize = (SystemSettingSeekBarPreference) findPreference(STATUS_BAR_CARRIER_FONT_SIZE);
        int StatusBarCarrierSize = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CARRIER_FONT_SIZE, 14);
        mStatusBarCarrierSize.setValue(StatusBarCarrierSize / 1);
        mStatusBarCarrierSize.setOnPreferenceChangeListener(this);

        mCarrierFontStyle = (ListPreference) findPreference(CARRIER_FONT_STYLE);
        int showCarrierFont = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CARRIER_FONT_STYLE, 23);
        mCarrierFontStyle.setValue(String.valueOf(showCarrierFont));
        mCarrierFontStyle.setOnPreferenceChangeListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mStatusBarCarrierSize) {
            int width = ((Integer)newValue).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_CARRIER_FONT_SIZE, width);
            return true;
        }  else if (preference == mCarrierFontStyle) {
            int showCarrierFont = Integer.valueOf((String) newValue);
            int index = mCarrierFontStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.
                STATUS_BAR_CARRIER_FONT_STYLE, showCarrierFont);
            mCarrierFontStyle.setSummary(mCarrierFontStyle.getEntries()[index]);
            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        boolean value;
        if (preference.getKey().equals(CUSTOM_CARRIER_LABEL)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);
            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            int maxLength = 14;
            input.setText(TextUtils.isEmpty(mCustomCarrierLabelText) ? "" : mCustomCarrierLabelText);
            input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
            input.setSelection(input.getText().length());
            alert.setView(input);
            alert.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = ((Spannable) input.getText()).toString().trim();
                            Settings.System.putString(resolver, Settings.System.CUSTOM_CARRIER_LABEL, value);
                            updateCustomLabelTextSummary();
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_CUSTOM_CARRIER_LABEL_CHANGED);
                            getActivity().sendBroadcast(i);
                }
            });
            alert.setNegativeButton(getString(android.R.string.cancel), null);
            alert.show();
            return true;
        }
        return false;
    }

    private void updateCustomLabelTextSummary() {
        mCustomCarrierLabelText = Settings.System.getString(
            getContentResolver(), Settings.System.CUSTOM_CARRIER_LABEL);
        if (TextUtils.isEmpty(mCustomCarrierLabelText)) {
            mCustomCarrierLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomCarrierLabel.setSummary(mCustomCarrierLabelText);
        }
    }
}

