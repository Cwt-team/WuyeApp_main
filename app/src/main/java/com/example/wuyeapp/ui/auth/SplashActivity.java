package com.example.wuyeapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.R;
import com.example.wuyeapp.session.SessionManager;
import com.example.wuyeapp.ui.home.MainActivity;
import com.example.wuyeapp.utils.LogUtil;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DISPLAY_TIME = 1500; // 1.5秒
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i(TAG + " onCreate");
        setContentView(R.layout.activity_splash);

        // 延迟1.5秒后检查登录状态并跳转
        new Handler(Looper.getMainLooper()).postDelayed(this::checkLoginStatus, SPLASH_DISPLAY_TIME);
    }

    private void checkLoginStatus() {
        LogUtil.d(TAG + " 检查登录状态");
        // 获取SessionManager实例
        SessionManager sessionManager = SessionManager.getInstance(this);
        boolean isLoggedIn = sessionManager.isLoggedIn();
        LogUtil.i(TAG + " 登录状态: " + (isLoggedIn ? "已登录" : "未登录"));

        // 根据登录状态跳转到不同的页面
        Intent intent;
        if (isLoggedIn) {
            // 已登录，跳转到主页
            intent = new Intent(this, MainActivity.class);
        } else {
            // 未登录，跳转到登录页
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish(); // 结束SplashActivity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG + " onDestroy");
    }
} 