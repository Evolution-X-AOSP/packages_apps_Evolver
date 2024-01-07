package com.evolution.settings.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.settings.R;

public class ClockStyle extends RelativeLayout {

    private static final int[] CLOCK_VIEW_IDS = {
            R.id.keyguard_clock_style_default,
            R.id.keyguard_clock_style_oos,
            R.id.keyguard_clock_style_ios,
            R.id.keyguard_clock_style_cos,
            R.id.keyguard_clock_style_custom,
            R.id.keyguard_clock_style_custom1,
            R.id.keyguard_clock_style_custom2,
            R.id.keyguard_clock_style_custom3
    };

    private static final int DEFAULT_STYLE = 0; //Disabled
    private static final String CLOCK_STYLE_KEY = "clock_style";

    private Context mContext;
    private View[] clockViews;

    public ClockStyle(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        clockViews = new View[CLOCK_VIEW_IDS.length];
        for (int i = 0; i < CLOCK_VIEW_IDS.length; i++) {
            if (CLOCK_VIEW_IDS[i] != 0) {
                clockViews[i] = findViewById(CLOCK_VIEW_IDS[i]);
            } else {
                clockViews[i] = null;
            }
        }
        new MyContentObserver(new Handler()).observe();
        updateClockView();
    }

    private void updateClockView() {
        if (clockViews != null) {
            int clockStyle = Settings.System.getInt(mContext.getContentResolver(), CLOCK_STYLE_KEY, DEFAULT_STYLE);
            for (int i = 0; i < clockViews.length; i++) {
                if (clockViews[i] != null) {
                    clockViews[i].setVisibility(i == clockStyle ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    class MyContentObserver extends ContentObserver {
        public MyContentObserver(Handler h) {
            super(h);
        }

        public void observe() {
            ContentResolver cr = mContext.getContentResolver();
                    cr.registerContentObserver(Settings.System.getUriFor("clock_style"), false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateClockView();
        }
    }
}
