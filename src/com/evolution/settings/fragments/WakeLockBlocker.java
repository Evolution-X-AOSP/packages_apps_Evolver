/*
 * Copyright (C) 2014 The LiquidSmooth Project
 *           (C) 2018 crDroid Android Project
 *           (C) 2021 Havoc-OS
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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WakeLockBlocker extends SettingsPreferenceFragment implements
        CompoundButton.OnCheckedChangeListener  {

    private static final String TAG = "WakeLockBlocker";

    private TextView mTextView;
    private View mSwitchBar;

    private LinearLayout mFooter;
    private TextView mFooterText;
    private ListView mWakeLockList;
    private List<String> mSeenWakeLocks;
    private List<String> mBlockedWakeLocks;
    private LayoutInflater mInflater;
    private Map<String, Boolean> mWakeLockState;
    private WakeLockListAdapter mListAdapter;
    private AlertDialog mAlertDialog;
    private boolean mAlertShown = false;
    private Toast mToast;

    private static final int MENU_RELOAD = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;

    public class WakeLockListAdapter extends ArrayAdapter<String> {

        public WakeLockListAdapter(Context context, int resource, List<String> values) {
            super(context, R.layout.blocker_item, resource, values);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = mInflater.inflate(R.layout.blocker_item, parent, false);
            final CheckBox check = (CheckBox)rowView.findViewById(R.id.checkbox);
            check.setText(mSeenWakeLocks.get(position));

            Boolean checked = mWakeLockState.get(check.getText().toString());
            check.setChecked(checked.booleanValue());

            if(checked.booleanValue()) {
                check.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            }

            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean checked) {
                        mWakeLockState.put(v.getText().toString(), new Boolean(checked));
                        if(checked) {
                            check.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                            mListAdapter.notifyDataSetChanged();
                        } else {
                            check.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
                            mListAdapter.notifyDataSetChanged();
                        }
                    }
            });
            return rowView;
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EVOLVER;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.blocker, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean enabled = Settings.Global.getInt(getContentResolver(),
                Settings.Global.WAKELOCK_BLOCKING_ENABLED, 0) == 1;

        mTextView = view.findViewById(R.id.switch_text);
        mTextView.setText(getString(enabled ?
                R.string.switch_on_text : R.string.switch_off_text));

        mSwitchBar = view.findViewById(R.id.switch_bar);
        Switch switchWidget = mSwitchBar.findViewById(android.R.id.switch_widget);
        switchWidget.setChecked(enabled);
        switchWidget.setOnCheckedChangeListener(this);
        mSwitchBar.setActivated(enabled);
        mSwitchBar.setOnClickListener(v -> {
            switchWidget.setChecked(!switchWidget.isChecked());
            mSwitchBar.setActivated(switchWidget.isChecked());
        });

        if (mWakeLockList != null) {
            mWakeLockList.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        }

        if (mFooter != null) {
            mFooter.setVisibility(enabled ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Settings.Global.putInt(getContentResolver(),
                Settings.Global.WAKELOCK_BLOCKING_ENABLED, isChecked ? 1 : 0);

        mTextView.setText(getString(isChecked ? R.string.switch_on_text : R.string.switch_off_text));
        mSwitchBar.setActivated(isChecked);

        if (mWakeLockList != null) {
            mWakeLockList.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
        }

        if (mFooter != null) {
            mFooter.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        }

        if (isChecked && isFirstEnable() && !mAlertShown) {
            showAlert();
            mAlertShown = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(getString(R.string.wakelock_blocker_title));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mWakeLockState = new HashMap<String, Boolean>();
        updateSeenWakeLocksList();
        updateBlockedWakeLocksList();

        boolean enabled = Settings.Global.getInt(getContentResolver(),
                Settings.Global.WAKELOCK_BLOCKING_ENABLED, 0) == 1;

        mFooter = (LinearLayout) getActivity().findViewById(R.id.blocker_footer);
        mFooterText = (TextView) getActivity().findViewById(R.id.blocker_footer_text);
        mWakeLockList = (ListView) getActivity().findViewById(R.id.blocker_list);

        mListAdapter = new WakeLockListAdapter(getActivity(), android.R.layout.simple_list_item_multiple_choice,
                mSeenWakeLocks);
        mWakeLockList.setAdapter(mListAdapter);

        mWakeLockList.setDivider(new ColorDrawable(Color.TRANSPARENT));
        mWakeLockList.setDividerHeight(0);

        if (mWakeLockList != null) {
            mWakeLockList.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        }

        if (mFooter != null) {
            mFooter.setVisibility(enabled ? View.GONE : View.VISIBLE);
        }

        if (mFooterText != null) {
            mFooterText.setText(getActivity().getString(R.string.wakelock_blocker_display_text));
        }
    }

    private boolean isFirstEnable() {
        return Settings.Global.getString(getActivity().getContentResolver(),
                Settings.Global.WAKELOCK_BLOCKING_LIST) == null;
    }

    private void updateSeenWakeLocksList() {
        PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);

        String seenWakeLocks =  pm.getSeenWakeLocks();
        mSeenWakeLocks = new ArrayList<String>();

        if (seenWakeLocks!=null && seenWakeLocks.length()!=0) {
            String[] parts = seenWakeLocks.split("\\|");
            for(int i = 0; i < parts.length; i++) {
                mSeenWakeLocks.add(parts[i]);
                mWakeLockState.put(parts[i], new Boolean(false));
            }
        }
    }

    private void updateBlockedWakeLocksList() {
        String blockedWakelockList = Settings.Global.getString(getActivity().getContentResolver(),
                Settings.Global.WAKELOCK_BLOCKING_LIST);

        mBlockedWakeLocks = new ArrayList<String>();

        if (blockedWakelockList!=null && blockedWakelockList.length()!=0) {
            String[] parts = blockedWakelockList.split("\\|");
            for(int i = 0; i < parts.length; i++) {
                mBlockedWakeLocks.add(parts[i]);

                // add all blocked but not seen so far
                if(!mSeenWakeLocks.contains(parts[i])) {
                    mSeenWakeLocks.add(parts[i]);
                }
                mWakeLockState.put(parts[i], new Boolean(true));
            }
        }

        Collections.sort(mSeenWakeLocks);
    }

    private void save() {
        StringBuffer buffer = new StringBuffer();
        Iterator<String> nextState = mWakeLockState.keySet().iterator();

        while (nextState.hasNext()) {
            String name = nextState.next();
            Boolean state=mWakeLockState.get(name);
            if(state.booleanValue()) {
                buffer.append(name + "|");
            }
        }

        if ( buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }

        Settings.Global.putString(getActivity().getContentResolver(),
                Settings.Global.WAKELOCK_BLOCKING_LIST, buffer.toString());

        mToast = Toast.makeText(getActivity(), R.string.blocker_action_save_toast, Toast.LENGTH_LONG);
        mToast.show();
        
    }

    private void reload() {
        mWakeLockState = new HashMap<String, Boolean>();
        updateSeenWakeLocksList();
        updateBlockedWakeLocksList();

        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RELOAD, 0, R.string.blocker_action_reload)
                .setIcon(com.android.internal.R.drawable.ic_menu_refresh)
                .setAlphabeticShortcut('r')
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                        MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(0, MENU_SAVE, 0, R.string.blocker_action_save)
                .setIcon(R.drawable.ic_save)
                .setAlphabeticShortcut('s')
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                        MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean enabled = Settings.Global.getInt(getContentResolver(),
                Settings.Global.WAKELOCK_BLOCKING_ENABLED, 0) == 1;
        switch (item.getItemId()) {
            case MENU_RELOAD:
                if (enabled) {
                    reload();
                }
                return true;
            case MENU_SAVE:
                if (enabled) {
                    save();
                }
                return true;
            default:
                return false;
        }
    }

    private void showAlert() {
        /* Display the warning dialog */
        mAlertDialog = new AlertDialog.Builder(getActivity()).create();
        mAlertDialog.setTitle(R.string.blocker_warning_title);
        mAlertDialog.setMessage(getResources().getString(R.string.wakelock_blocker_warning));
        mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getResources().getString(com.android.internal.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        mAlertDialog.show();
    }
}
