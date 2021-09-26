package com.szty.h5xinfakey;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/**
 * @author Ly
 */
public class Tools {
    private static final String TAG = "Tools";
    /**
     * 跳转到系统设置界面
     *
     * @param activity
     */
    public static void jumpSystemSet(Activity activity) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(uri);
        activity.startActivity(intent);
    }

}
