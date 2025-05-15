package com.example.wuyeapp.ui.call;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

/**
 * 透明Activity，用于唤醒屏幕并快速关闭
 * 仅用于确保在锁屏状态下能显示来电通知
 */
public class WakeupActivity extends Activity {
    private static final String TAG = "WakeupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i(TAG, "WakeupActivity已启动，准备唤醒屏幕");
        
        // 设置窗口标志，确保能在锁屏上显示
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        // Android 13+需要使用setShowWhenLocked而不是FLAG_SHOW_WHEN_LOCKED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        
        // 使用极短的延迟然后结束Activity，目的只是唤醒屏幕
        new Thread(() -> {
            try {
                Thread.sleep(1000); // 延迟1秒
            } catch (InterruptedException e) {
                Log.e(TAG, "WakeupActivity延迟被中断", e);
            } finally {
                Log.i(TAG, "WakeupActivity任务完成，准备关闭");
                runOnUiThread(this::finish);
            }
        }).start();
    }
    
    @Override
    protected void onDestroy() {
        Log.i(TAG, "WakeupActivity已销毁");
        super.onDestroy();
    }
} 