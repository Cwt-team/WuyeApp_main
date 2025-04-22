package com.example.wuyeapp.utils;

import android.util.Log;

/**
 * SIP相关日志工具类
 */
public class SipLogger {
    private static final String TAG = "SIP_TEST";
    private static boolean DEBUG = true;
    
    public static void d(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }
    
    public static void i(String message) {
        if (DEBUG) {
            Log.i(TAG, message);
        }
    }
    
    public static void e(String message) {
        if (DEBUG) {
            Log.e(TAG, message);
        }
    }
    
    public static void e(String message, Throwable t) {
        if (DEBUG) {
            Log.e(TAG, message, t);
        }
    }
} 