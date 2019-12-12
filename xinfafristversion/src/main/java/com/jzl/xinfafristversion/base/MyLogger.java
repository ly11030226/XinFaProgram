package com.jzl.xinfafristversion.base;

import android.util.Log;

import com.jzl.xinfafristversion.BuildConfig;


public class MyLogger {

    public static void d(String tag, String log) {
        if (BuildConfig.LOG_DEBUG) {
            Log.d(tag, log + "");
        }
    }

    public static void w(String tag, String log) {
        if (BuildConfig.LOG_DEBUG) {
            Log.w(tag, log + "");
        }
    }

    public static void e(String tag, String log, Throwable t) {
        if (BuildConfig.LOG_DEBUG) {
            Log.e(tag, log + "", t);
        }
    }

    public static void e(String tag, String log) {
        if (BuildConfig.LOG_DEBUG) {
            Log.e(tag, log + "");
        }
    }

    public static void i(String tag, String log) {
        if (BuildConfig.LOG_DEBUG) {
            Log.i(tag, log + "");
        }

    }
}
