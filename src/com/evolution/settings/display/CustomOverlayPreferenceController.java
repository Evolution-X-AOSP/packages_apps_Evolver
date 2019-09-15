/*
 * Copyright (C) 2018 The Android Open Source Project
 * Copyright (C) 2019 ArrowOS
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

package com.evolution.settings.display;

import static android.os.UserHandle.USER_SYSTEM;

import android.app.UiModeManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.VisibleForTesting;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Preference controller to allow users to choose an overlay from a list for a given category.
 * The chosen overlay is enabled along with its Ext overlays belonging to the same category.
 * A default option is also exposed that disables all overlays in the given category.
 */
public class CustomOverlayPreferenceController extends DeveloperOptionsPreferenceController
        implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String TAG = "CustomOverlayCategoryPC";
    @VisibleForTesting
    static final String PACKAGE_DEVICE_DEFAULT = "package_device_default";

    /* Define system target packages here */
    private static final List<String> SYSTEM_TARGET_PACKAGES = Arrays.asList
    (
        "android",
        "com.android.settings",
        "com.android.systemui",
        "com.google.android.inputmethod.latin"
    );

    /* Define custom app target packages here */
    private static final List<String> CUSTOM_APP_TARGET_PACKAGES = Arrays.asList
    (
        "org.lineageos.updater"
    );

    private static final Comparator<OverlayInfo> OVERLAY_INFO_COMPARATOR =
            Comparator.comparingInt(a -> a.priority);
    private final IOverlayManager mOverlayManager;
    private final boolean mAvailable;
    private final String mCategory;
    private final PackageManager mPackageManager;

    private ListPreference mPreference;
    private UiModeManager mUiModeManager = mContext.getSystemService(UiModeManager.class);
    private ContentResolver resolver = mContext.getContentResolver();
    private Handler mHandler;

    @VisibleForTesting
    CustomOverlayPreferenceController(Context context, PackageManager packageManager,
            IOverlayManager overlayManager, String category) {
        super(context);
	mHandler = startHandlerThread();
	mCustomSettingsObserver.observe();
        mOverlayManager = overlayManager;
        mPackageManager = packageManager;
        mCategory = category;
        mAvailable = overlayManager != null
                     && !getOverlayInfos().isEmpty()
                     && mUiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES;
    }

    public CustomOverlayPreferenceController(Context context, String category) {
        this(context, context.getPackageManager(), IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE)), category);
    }

    @VisibleForTesting
    Handler startHandlerThread() {
        HandlerThread thread = new HandlerThread("CustomOverlayPreferenceController");
        thread.start();
        return thread.getThreadHandler();
    }

    @Override
    public boolean isAvailable() {
        return mAvailable;
    }

    @Override
    public String getPreferenceKey() {
        return mCategory;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        setPreference(screen.findPreference(getPreferenceKey()));
    }

    @VisibleForTesting
    void setPreference(ListPreference preference) {
        mPreference = preference;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return setOverlay((String) newValue);
    }

    private boolean setOverlay(String packageName) {
        final String currentPackageName = getOverlayInfos().stream()
                .filter(info -> info.isEnabled())
                .map(info -> info.packageName)
                .findFirst()
                .orElse(null);

        if (PACKAGE_DEVICE_DEFAULT.equals(packageName) && TextUtils.isEmpty(currentPackageName)
                || TextUtils.equals(packageName, currentPackageName)) {
            // Already set.
            return true;
        }

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                if (PACKAGE_DEVICE_DEFAULT.equals(packageName)) {
                    return handleOverlays(currentPackageName, false);
                } else {
                    // first disable all the current enabled overlays and their extensions
                    handleOverlays(currentPackageName, false);
                    // enable all the selected overlays and their extensions
                    return handleOverlays(packageName, true);
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                updateState(mPreference);
                if (!success) {
                    Toast.makeText(
                            mContext, R.string.overlay_toast_failed_to_apply, Toast.LENGTH_LONG)
                            .show();
                }
            }
        }.execute();

        return true; // Assume success; toast on failure.
    }

    private Boolean handleOverlays(String currentPackageName, Boolean state) {
        try {
            for (OverlayInfo overlay : getOverlayInfos()) {
                if (overlay.packageName.equals(currentPackageName)
                        || overlay.packageName.equals(currentPackageName + "Ext")) {
                    mOverlayManager.setEnabled(overlay.packageName, state, USER_SYSTEM);
                }
            }
        } catch (RemoteException re) {
            Log.w(TAG, "Error handling overlays.", re);
            return false;
        }

        return true;
    }

    @Override
    public void updateState(Preference preference) {
        final List<String> pkgs = new ArrayList<>();
        final List<String> labels = new ArrayList<>();

        String selectedPkg = PACKAGE_DEVICE_DEFAULT;
        String selectedLabel = mContext.getString(R.string.overlay_option_device_default);

        // Add the default package / label before all of the overlays
        pkgs.add(selectedPkg);
        labels.add(selectedLabel);

        for (OverlayInfo overlayInfo : getOverlayInfos()) {
            if (!overlayInfo.packageName.endsWith("Ext")) {
                pkgs.add(overlayInfo.packageName);
                try {
                    labels.add(mPackageManager.getApplicationInfo(overlayInfo.packageName, 0)
                            .loadLabel(mPackageManager).toString());
                } catch (PackageManager.NameNotFoundException e) {
                    labels.add(overlayInfo.packageName);
                }

                if (overlayInfo.isEnabled()) {
                    selectedPkg = pkgs.get(pkgs.size() - 1);
                    selectedLabel = labels.get(labels.size() - 1);

                    Settings.System.putString(resolver,
                            Settings.System.COLOR_BUCKET_OVERLAY, selectedPkg);
                }
            }
        }

        mPreference.setEntries(labels.toArray(new String[labels.size()]));
        mPreference.setEntryValues(pkgs.toArray(new String[pkgs.size()]));
        mPreference.setValue(selectedPkg);
        mPreference.setSummary(selectedLabel);
    }

    private List<OverlayInfo> getOverlayInfos() {
        final List<OverlayInfo> filteredInfos = new ArrayList<>();
        List<OverlayInfo> overlayInfos = new ArrayList<>();

        Stream.of(SYSTEM_TARGET_PACKAGES.stream(), CUSTOM_APP_TARGET_PACKAGES.stream())
              .flatMap(target -> target)
              .forEach(targetPackageName -> {
                  try {
                      overlayInfos.addAll(mOverlayManager
                          .getOverlayInfosForTarget(targetPackageName, USER_SYSTEM));
                  } catch (RemoteException re) {
                      throw re.rethrowFromSystemServer();
                  }
             }
        );

        for (OverlayInfo overlay : overlayInfos) {
            if (mCategory.equals(overlay.category)) {
                filteredInfos.add(overlay);
            }
        }
        filteredInfos.sort(OVERLAY_INFO_COMPARATOR);
        return filteredInfos;
    }

    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {
        CustomSettingsObserver(Handler handler) {
            super(handler);
        }
         void observe() {
	    resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.UI_NIGHT_MODE),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.Secure.getUriFor(
                Settings.Secure.UI_NIGHT_MODE))) {
                if (mUiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_NO) {
		    Log.w(TAG, "Dark mode turned off, unloading all custom overlays");
                    setOverlay(PACKAGE_DEVICE_DEFAULT);
                } else if (mUiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES) {
                    // Set back previously selected overlay on re-enabling dark mode
                    setOverlay(Settings.System.getString(resolver, Settings.System.COLOR_BUCKET_OVERLAY));
                }
            }
        }
    }
}
