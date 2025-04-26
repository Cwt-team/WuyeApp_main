package com.example.wuyeapp.ui.visitor;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.wuyeapp.databinding.ActivityDialPadBinding;
import com.example.wuyeapp.sip.LinphoneCallback;
import com.example.wuyeapp.sip.LinphoneSipManager;
import com.example.wuyeapp.sip.LinphoneService;
import com.example.wuyeapp.ui.call.CallActivity;
import com.example.wuyeapp.ui.settings.SipSettingsActivity;

import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.RegistrationState;
import org.linphone.core.Account;

import java.util.ArrayList;
import java.util.List;

public class DialPadActivity extends AppCompatActivity implements LinphoneCallback {
    
    private static final String TAG = "DialPadActivity";
    private ActivityDialPadBinding binding;
    private StringBuilder dialNumber = new StringBuilder();
    private LinphoneService linphoneService;
    private boolean isBound = false;
    
    // 请求权限的请求码
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LinphoneService.LocalBinder binder = (LinphoneService.LocalBinder) service;
            linphoneService = binder.getService();
            isBound = true;
            
            // 设置回调
            linphoneService.setLinphoneCallback(DialPadActivity.this);
            
            // 检查注册状态
            checkRegistrationStatus();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            linphoneService = null;
            isBound = false;
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDialPadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 先初始化界面
        initializeUI();
        
        // 设置Linphone回调
        LinphoneSipManager.getInstance().setLinphoneCallback(this);
        
        // 请求必要的权限
        requestPermissions();
        
        // 绑定Linphone服务
        Intent intent = new Intent(this, LinphoneService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    
    private void initializeUI() {
        // 返回按钮点击事件
        binding.btnBack.setOnClickListener(v -> finish());
        
        // 拨号按钮点击事件
        binding.btnCall.setOnClickListener(v -> {
            if (dialNumber.length() > 0) {
                String number = dialNumber.toString();
                
                // 检查SIP注册状态
                if (!isSipRegistered()) {
                    Toast.makeText(this, "SIP账户未注册，请先在设置中配置SIP账户", Toast.LENGTH_LONG).show();
                    navigateToSipSettings();
                    return;
                }
                
                // 启动CallActivity
                Intent callIntent = new Intent(this, CallActivity.class);
                callIntent.setAction("MAKE_CALL");
                callIntent.putExtra("number", number);
                startActivity(callIntent);
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
        
        // 长按删除按钮清空号码
        binding.btnCancel.setOnLongClickListener(v -> {
            dialNumber.setLength(0);
            updateDialNumber();
            return true;
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
        
        // 长按0键输入+号
        binding.btn0.setOnLongClickListener(v -> {
            appendDialNumber("+");
            return true;
        });
    }
    
    private void appendDialNumber(String digit) {
        dialNumber.append(digit);
        updateDialNumber();
    }
    
    private void updateDialNumber() {
        // 在UI中显示拨号号码
        binding.tvDialNumber.setText(dialNumber.toString());
    }
    
    private boolean isSipRegistered() {
        if (linphoneService != null) {
            Core core = linphoneService.getCore();
            if (core != null) {
                Account account = core.getDefaultAccount();
                if (account != null) {
                    return account.getState() == RegistrationState.Ok;
                }
            }
        }
        
        return false;
    }
    
    private void checkRegistrationStatus() {
        if (!isSipRegistered()) {
            // 检查是否已配置SIP账户
            SharedPreferences preferences = getSharedPreferences("SipSettings", Context.MODE_PRIVATE);
            String username = preferences.getString("username", "");
            String password = preferences.getString("password", "");
            String domain = preferences.getString("domain", "");
            
            if (username.isEmpty() || password.isEmpty() || domain.isEmpty()) {
                Toast.makeText(this, "请先配置SIP账户", Toast.LENGTH_LONG).show();
                navigateToSipSettings();
            } else {
                Toast.makeText(this, "SIP账户未注册，正在尝试重新注册...", Toast.LENGTH_SHORT).show();
                // SIP账户已配置但未注册，尝试重新注册
                LinphoneSipManager.getInstance().loadSettingsAndRegister();
            }
        } else {
            Log.d(TAG, "SIP账户已注册");
        }
    }
    
    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE
        };
        
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            // 只请求尚未授予的权限
            ActivityCompat.requestPermissions(this, 
                    permissionsToRequest.toArray(new String[0]), 
                    PERMISSIONS_REQUEST_CODE);
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
                // 权限被拒绝，提示用户但不关闭Activity
                Toast.makeText(this, "部分SIP功能可能无法正常使用，请授予相关权限", Toast.LENGTH_LONG).show();
                // 不再调用finish()
            }
        }
    }
    
    // 导航到SIP设置的方法
    private void navigateToSipSettings() {
        Intent intent = new Intent(this, SipSettingsActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // 重新检查注册状态
        if (isBound && linphoneService != null) {
            checkRegistrationStatus();
        }
    }
    
    @Override
    protected void onDestroy() {
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        super.onDestroy();
    }

    // LinphoneCallback 接口实现
    @Override
    public void onRegistrationSuccess() {
        runOnUiThread(() -> {
            Toast.makeText(this, "SIP账户注册成功", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SIP账户注册成功");
        });
    }
    
    @Override
    public void onRegistrationFailed(String reason) {
        runOnUiThread(() -> {
            Toast.makeText(this, "SIP账户注册失败: " + reason, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "SIP账户注册失败: " + reason);
        });
    }
    
    @Override
    public void onIncomingCall(Call call, String caller) {
        runOnUiThread(() -> {
            // 显示来电界面
            Intent intent = new Intent(this, CallActivity.class);
            intent.setAction("ANSWER_CALL");
            intent.putExtra("caller", caller);
            startActivity(intent);
        });
    }
    
    @Override
    public void onCallProgress() {
        // 通话进行中，不需要在拨号界面处理
    }
    
    @Override
    public void onCallEstablished() {
        // 通话已建立，不需要在拨号界面处理
    }
    
    @Override
    public void onCallEnded() {
        // 通话已结束，不需要在拨号界面处理
    }
    
    @Override
    public void onCallFailed(String reason) {
        runOnUiThread(() -> {
            Toast.makeText(this, "通话失败: " + reason, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "通话失败: " + reason);
        });
    }
} 