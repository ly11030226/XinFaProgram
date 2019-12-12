package com.ads.xinfa;

import android.view.View;

public abstract class DoubleClickListener implements View.OnClickListener {
    private static final int TIME_DURATION = 1000;
    private static long preTime;

    @Override
    public void onClick(View v) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - preTime < TIME_DURATION) {
            doubleClick(v);
        }
        preTime = currentTime;
    }

    public abstract void doubleClick(View v);

}
