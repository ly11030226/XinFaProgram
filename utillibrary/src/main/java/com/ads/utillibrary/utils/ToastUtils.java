package com.ads.utillibrary.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * ToastUtils
 */
public class ToastUtils {
    private static Toast toast = null; //Toast的对象！

    public static void showToast(Context mContext, String id) {
        if (toast == null) {
            toast = Toast.makeText(mContext.getApplicationContext(), id, Toast.LENGTH_SHORT);
        }
        else {
            toast.setText(id);
        }
        toast.show();
    }
}
