package com.example.wuyeapp.utils;

import android.util.Log;

public class LogUtil {
    private static final String DEFAULT_TAG = "WuyeApp";
    // 临时使用一个静态变量来控制日志输出
    private static boolean isDebug = true;  // 在开发阶段设置为true

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

    public static void i(String tag, String msg) {
        if (isDebug) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (isDebug) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (isDebug) {
            Log.e(tag, msg, tr);
        }
    }

    public static void logToFile(String msg) {
        if (!isDebug) return;
        // 实现文件写入逻辑
    }

    public static void d(String tag, String format, Object... args) {
        if (isDebug) {
            Log.d(tag, String.format(format, args));
        }
    }

    public static void e(String msg, Throwable tr) {
        if (isDebug) {
            Log.e(DEFAULT_TAG, msg, tr);
        }
    }

    // ... 其他日志级别的重载方法
} 