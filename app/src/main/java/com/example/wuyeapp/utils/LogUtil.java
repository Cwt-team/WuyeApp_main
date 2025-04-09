package com.example.wuyeapp.utils;

import android.util.Log;

public class LogUtil {
    private static final String DEFAULT_TAG = "WuyeApp";
    private static boolean isDebug = true;  // 可以通过BuildConfig.DEBUG来控制

    public static void d(String msg) {
        if (isDebug) {
            Log.d(DEFAULT_TAG, msg);
        }
    }

    public static void i(String msg) {
        if (isDebug) {
            Log.i(DEFAULT_TAG, msg);
        }
    }

    public static void w(String msg) {
        if (isDebug) {
            Log.w(DEFAULT_TAG, msg);
        }
    }

    public static void e(String msg) {
        if (isDebug) {
            Log.e(DEFAULT_TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(tag, msg);
        }
    }

    // ... 其他日志级别的重载方法
} 