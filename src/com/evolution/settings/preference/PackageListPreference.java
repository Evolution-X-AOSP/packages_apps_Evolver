/*
 * Copyright (C) 2020 The exTHmUI Open Source Project
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
package com.evolution.settings.preference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import com.android.settings.R;

import java.util.Arrays;
import java.util.ArrayList;

import com.evolution.settings.preference.PackageListAdapter;
import com.evolution.settings.preference.PackageListAdapter.PackageItem;

public class PackageListPreference extends PreferenceCategory implements
        Preference.OnPreferenceClickListener {

    private Context mContext;
    private String mRemovedListKey;

    private PackageListAdapter mPackageAdapter;
    private PackageManager mPackageManager;

    private Preference mAddPackagePref;

    private ContentResolver mContentResolver;

    private ArrayList<String> mGamingPackages = new ArrayList<>();
    private ArrayList<String> mRemovedPackages = new ArrayList<>();

    public PackageListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        mPackageManager = mContext.getPackageManager();
        mPackageAdapter = new PackageListAdapter(mContext);

        mContentResolver = mContext.getApplicationContext().getContentResolver();

        mAddPackagePref = makeAddPref();

        this.setOrderingAsAdded(false);
    }

    private Preference makeAddPref() {
        Preference pref = new Preference(mContext);
        pref.setTitle(R.string.add_package_to_title);
        pref.setIcon(R.drawable.ic_add);
        pref.setPersistent(false);
        pref.setOnPreferenceClickListener(this);
        return pref;
    }

    public void setRemovedListKey(String key) {
        mRemovedListKey = key;
        if (isAttached()) {
            refreshCustomApplicationPrefs();
        }
    }

    private ApplicationInfo getAppInfo(String packageName) {
        try {
            return mPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void parsePackageList() {
        mGamingPackages.clear();
        mRemovedPackages.clear();

        String packageListData = Settings.System.getString(mContentResolver, getKey());
        if (!TextUtils.isEmpty(packageListData)) {
            String[] packageListArray = packageListData.split(";");
            mGamingPackages.addAll(Arrays.asList(packageListArray));
        }
        if (!TextUtils.isEmpty(mRemovedListKey)) {
            String removedPackageListData = Settings.System.getString(mContentResolver, mRemovedListKey);
            if (!TextUtils.isEmpty(removedPackageListData)) {
                String[] packageListArray = removedPackageListData.split(";");
                mRemovedPackages.addAll(Arrays.asList(packageListArray));
            }
        }
    }

    private void refreshCustomApplicationPrefs() {
        parsePackageList();
        removeAll();
        addPreference(mAddPackagePref);
        for (String pkg : mGamingPackages) {
            addPackageToPref(pkg);
        }
    }

    private void savePackagesList() {
        String packageListData = String.join(";", mGamingPackages);
        Settings.System.putString(mContentResolver, getKey(), packageListData);
        if (!TextUtils.isEmpty(mRemovedListKey)) {
            String removedPackageListData = String.join(";", mRemovedPackages);
            Settings.System.putString(mContentResolver, mRemovedListKey, removedPackageListData);
        }
    }

    private void addPackageToPref(String packageName) {
        Preference pref = new Preference(mContext);
        ApplicationInfo appInfo = getAppInfo(packageName);
        if (appInfo == null) return;
        pref.setKey(packageName);
        pref.setTitle(appInfo.loadLabel(mPackageManager));
        pref.setIcon(appInfo.loadIcon(mPackageManager));
        pref.setPersistent(false);
        pref.setOnPreferenceClickListener(this);
        addPreference(pref);
    }

    private void addPackageToList(String packageName) {
        if (!mGamingPackages.contains(packageName)) {
            mGamingPackages.add(packageName);
            addPackageToPref(packageName);
        }
        mRemovedPackages.remove(packageName);
        savePackagesList();
    }

    private void removePackageFromList(String packageName) {
        if (!mRemovedPackages.contains(packageName)) {
            mRemovedPackages.add(packageName);
        }
        mGamingPackages.remove(packageName);
        savePackagesList();
    }

    @Override
    public void onAttached() {
        super.onAttached();
        refreshCustomApplicationPrefs();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        if (preference == mAddPackagePref) {
            ListView appsList = new ListView(mContext);
            appsList.setAdapter(mPackageAdapter);
            builder.setTitle(R.string.profile_choose_app);
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setView(appsList);
            final Dialog dialog = builder.create();
            appsList.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    PackageItem info = (PackageItem) parent.getItemAtPosition(position);
                    addPackageToList(info.packageName);
                    dialog.cancel();
                }
            });
            dialog.show();
        } else if (preference == findPreference(preference.getKey())) {
            builder.setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removePackageFromList(preference.getKey());
                        removePreference(preference);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null).show();
        }
        return true;
    }

}
