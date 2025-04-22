package com.example.wuyeapp.ui.visitor;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.wuyeapp.databinding.ActivityDialPadBinding;
import com.example.wuyeapp.sip.SipCallback;
import com.example.wuyeapp.sip.SipCall;
import com.example.wuyeapp.sip.LinphoneSipManager;
import com.example.wuyeapp.ui.settings.SipSettingsActivity;
import com.example.wuyeapp.ui.call.CallActivity;

import android.content.SharedPreferences;

public class DialPadActivity extends AppCompatActivity implements SipCallback {
    
    private ActivityDialPadBinding binding;
    private StringBuilder dialNumber = new StringBuilder();
    
    // 请求权限的请求码
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDialPadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 请求必要的权限
        requestPermissions();
        
        // 设置SIP回调
        LinphoneSipManager.getInstance().setSipCallback(this);
        
        // 返回按钮点击事件
        binding.btnBack.setOnClickListener(v -> finish());
        
        // 拨号按钮点击事件
        binding.btnCall.setOnClickListener(v -> {
            if (dialNumber.length() > 0) {
                String number = dialNumber.toString();
                // 启动CallActivity
                Intent intent = new Intent(this, CallActivity.class);
                intent.setAction("MAKE_CALL");
                intent.putExtra("number", number);
                startActivity(intent);
            } else {
                Toast.makeText(this, "请输入号码", Toast.LENGTH_SHORT).show();
            }
        });

        // 取消/删除按钮点击事件
        binding.btnCancel.setOnClickListener(v -> {
            if (dialNumber.length() > 0) {
                dialNumber.deleteCharAt(dialNumber.length() - 1);
                updateDialNumber();
            }
        });
        
        // 设置数字按钮点击事件
        setNumberButtonListeners();
    }
    
    private void setNumberButtonListeners() {
        binding.btn0.setOnClickListener(v -> appendDialNumber("0"));
        binding.btn1.setOnClickListener(v -> appendDialNumber("1"));
        binding.btn2.setOnClickListener(v -> appendDialNumber("2"));
        binding.btn3.setOnClickListener(v -> appendDialNumber("3"));
        binding.btn4.setOnClickListener(v -> appendDialNumber("4"));
        binding.btn5.setOnClickListener(v -> appendDialNumber("5"));
        binding.btn6.setOnClickListener(v -> appendDialNumber("6"));
        binding.btn7.setOnClickListener(v -> appendDialNumber("7"));
        binding.btn8.setOnClickListener(v -> appendDialNumber("8"));
        binding.btn9.setOnClickListener(v -> appendDialNumber("9"));
        binding.btnStar.setOnClickListener(v -> appendDialNumber("*"));
        binding.btnHash.setOnClickListener(v -> appendDialNumber("#"));
    }
    
    private void appendDialNumber(String digit) {
        dialNumber.append(digit);
        updateDialNumber();
    }
    
    private void updateDialNumber() {
        // 在UI中显示拨号号码
        // 注意：您需要在布局文件中添加一个TextView来显示拨号号码
        // binding.tvDialNumber.setText(dialNumber.toString());
    }
    
    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.USE_SIP
        };
        
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }
        
        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (!allGranted) {
                Toast.makeText(this, "SIP功能需要相关权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    // SipCallback 接口实现
    @Override
    public void onRegistrationSuccess() {
        runOnUiThread(() -> Toast.makeText(this, "SIP账户注册成功", Toast.LENGTH_SHORT).show());
    }
    
    @Override
    public void onRegistrationFailed(String reason) {
        runOnUiThread(() -> Toast.makeText(this, "SIP账户注册失败: " + reason, Toast.LENGTH_SHORT).show());
    }
    
    @Override
    public void onIncomingCall(SipCall call, String caller) {
        runOnUiThread(() -> {
            // 显示来电界面，在实际应用中，您应该创建一个来电界面
            // 这里简单处理，显示一个Toast，并自动接听
            Toast.makeText(this, "来电: " + caller, Toast.LENGTH_LONG).show();
            call.answer();
        });
    }
    
    @Override
    public void onCallFailed(String reason) {
        runOnUiThread(() -> Toast.makeText(this, "通话失败: " + reason, Toast.LENGTH_SHORT).show());
    }
    
    @Override
    public void onCallEstablished() {
        runOnUiThread(() -> Toast.makeText(this, "通话已建立", Toast.LENGTH_SHORT).show());
    }
    
    @Override
    public void onCallEnded() {
        runOnUiThread(() -> Toast.makeText(this, "通话已结束", Toast.LENGTH_SHORT).show());
    }

    // 添加导航到SIP设置的方法
    private void navigateToSipSettings() {
        Intent intent = new Intent(this, SipSettingsActivity.class);
        startActivity(intent);
    }
} 