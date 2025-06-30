package com.example.wuyeapp.ui.auth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.wuyeapp.R;
import com.example.wuyeapp.session.SessionManager;
import com.example.wuyeapp.sip.LinphoneSipManager;
import com.example.wuyeapp.ui.home.MainActivity;
import com.example.wuyeapp.ui.auth.LoginActivity;
import com.example.wuyeapp.utils.LogUtil;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DISPLAY_TIME = 1500; // 1.5秒
    private static final String TAG = "SplashActivity";
    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 101; // 修改为更通用的请求码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i(TAG + " onCreate");
        setContentView(R.layout.activity_splash);

        // 检查并请求所需权限
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            // 权限已全部授予，进行Linphone初始化和后续操作
            initializeLinphoneAndProceed();
        } else {
            // 请求权限
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                LogUtil.i(TAG + " 所有所需权限已授予.");
                initializeLinphoneAndProceed();
            } else {
                LogUtil.w(TAG + " 部分或全部所需权限被拒绝.");
                Toast.makeText(this, "需要麦克风、电话和相机权限才能使用全部功能。", Toast.LENGTH_LONG).show();
                // 即使权限被拒绝，我们也尝试进行Linphone初始化和后续操作，但SIP功能可能会受限。
                // 实际应用中，这里可能需要更精细的错误处理或引导用户开启权限。
                initializeLinphoneAndProceed();
            }
        }
    }

    private void initializeLinphoneAndProceed() {
        LogUtil.i(TAG + " 初始化LinphoneSipManager...");
        // 在权限（希望）已授予后初始化 LinphoneSipManager
        // 这会启动 LinphoneService
        LinphoneSipManager.getInstance().init(this);

        // 然后，在启动画面显示时间结束后，进行原始的逻辑跳转
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