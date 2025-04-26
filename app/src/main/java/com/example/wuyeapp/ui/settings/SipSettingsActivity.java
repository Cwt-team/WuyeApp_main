package com.example.wuyeapp.ui.settings;

import android.content.SharedPreferences;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.databinding.ActivitySipSettingsBinding;
import com.example.wuyeapp.sip.LinphoneSipManager;
import com.example.wuyeapp.sip.LinphoneCallback;
import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.Account;

public class SipSettingsActivity extends AppCompatActivity {

    private ActivitySipSettingsBinding binding;
    private SharedPreferences preferences;
    private static final String PREFS_NAME = "SipSettings";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_DOMAIN = "domain";
    private static final String KEY_PORT = "port";
    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            binding.tvTestResult.setText("SIP连接测试超时，请检查服务器地址和端口是否正确");
            binding.tvTestResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    };
    private static final String TAG = "SipSettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "SipSettingsActivity创建");
        binding = ActivitySipSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // 加载已保存的设置
        loadSettings();

        // 保存按钮点击事件
        binding.btnSave.setOnClickListener(v -> saveSettings(true)); // 保存并关闭
        
        // 测试按钮点击事件
        binding.btnTest.setOnClickListener(v -> {
            Log.i(TAG, "点击了测试连接按钮");
            testConnection();
        });
    }

    private void loadSettings() {
        binding.etUsername.setText(preferences.getString(KEY_USERNAME, ""));
        binding.etPassword.setText(preferences.getString(KEY_PASSWORD, ""));
        binding.etDomain.setText(preferences.getString(KEY_DOMAIN, "8.138.26.199"));
        binding.etPort.setText(preferences.getString(KEY_PORT, "5060"));
    }

    private void saveSettings(boolean finish) {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String domain = binding.etDomain.getText().toString().trim();
        String port = binding.etPort.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || domain.isEmpty()) {
            Toast.makeText(this, "用户名、密码和服务器地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 保存设置
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_DOMAIN, domain);
        editor.putString(KEY_PORT, port);
        editor.apply();

        Toast.makeText(this, "SIP设置已保存", Toast.LENGTH_SHORT).show();
        
        if (finish) {
            finish();
        }
    }
    
    private void testConnection() {
        Log.i(TAG, "====== 开始SIP连接测试 ======");
        
        // 检查网络状态
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        
        if (!isConnected) {
            Log.e(TAG, "网络连接不可用");
            binding.tvTestResult.setText("网络连接不可用，请检查网络设置");
            binding.tvTestResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return;
        }
        
        // 打印网络详情
        if (activeNetwork != null) {
            Log.i(TAG, "网络类型: " + activeNetwork.getTypeName());
            Log.i(TAG, "网络子类型: " + activeNetwork.getSubtypeName());
            Log.i(TAG, "网络详情: " + activeNetwork.getExtraInfo());
            Log.i(TAG, "网络是否连接: " + activeNetwork.isConnected());
            Log.i(TAG, "网络是否可用: " + activeNetwork.isAvailable());
        }
        
        // 显示测试中的提示
        binding.tvTestResult.setText("正在尝试连接SIP服务器...");
        binding.tvTestResult.setTextColor(getResources().getColor(android.R.color.black));
        binding.tvTestResult.setVisibility(View.VISIBLE);
        
        // 获取SIP账户信息
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String domain = binding.etDomain.getText().toString().trim();
        String port = binding.etPort.getText().toString().trim();
        
        // 验证输入
        if (username.isEmpty() || password.isEmpty() || domain.isEmpty()) {
            binding.tvTestResult.setText("请填写完整的SIP账户信息");
            binding.tvTestResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return;
        }
        
        // 如果端口不是默认的5060，则添加到域名后
        if (!port.equals("5060")) {
            domain = domain + ":" + port;
        }
        
        // 设置超时处理
        timeoutHandler = new Handler();
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "SIP连接测试超时");
                binding.tvTestResult.setText("SIP连接测试超时，服务器未响应");
                binding.tvTestResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                
                // 尝试获取更多诊断信息
                LinphoneSipManager sipManager = LinphoneSipManager.getInstance();
                if (sipManager != null && sipManager.getLinphoneService() != null) {
                    Core core = sipManager.getLinphoneService().getCore();
                    if (core != null) {
                        Account account = core.getDefaultAccount();
                        if (account != null) {
                            Log.e(TAG, "账户注册状态: " + account.getState());
                            Log.e(TAG, "账户错误码: " + account.getError());
                        }
                    }
                }
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 60000); // 60秒超时
        
        // 保存设置但不关闭页面
        saveSettings(false);
        
        // 初始化SipManager并直接进行注册
        final String finalDomain = domain;
        
        LinphoneSipManager sipManager = LinphoneSipManager.getInstance();
        Log.d(TAG, "设置LinphoneCallback");
        sipManager.setLinphoneCallback(createTestCallback());
        
        // 先初始化SipManager
        sipManager.init(SipSettingsActivity.this);
        
        // 延迟一秒后直接调用testRegistration进行注册测试
        new Handler().postDelayed(() -> {
            if (sipManager.getLinphoneService() != null) {
                Log.i(TAG, "直接测试SIP注册: " + username + "@" + finalDomain);
                sipManager.testRegistration(username, password, finalDomain, createTestCallback());
            } else {
                binding.tvTestResult.setText("SIP服务未启动，请稍后再试");
                binding.tvTestResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                timeoutHandler.removeCallbacks(timeoutRunnable);
            }
        }, 1000);
    }

    // 创建测试回调
    private LinphoneCallback createTestCallback() {
        return new LinphoneCallback() {
            @Override
            public void onRegistrationSuccess() {
                Log.i(TAG, "SIP注册成功回调");
                timeoutHandler.removeCallbacks(timeoutRunnable); // 取消超时
                
                runOnUiThread(() -> {
                    binding.tvTestResult.setText("SIP连接成功！账户已成功注册");
                    binding.tvTestResult.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    Toast.makeText(SipSettingsActivity.this, "SIP连接成功！", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onRegistrationFailed(String reason) {
                Log.e(TAG, "SIP注册失败回调: " + reason);
                timeoutHandler.removeCallbacks(timeoutRunnable); // 取消超时
                
                runOnUiThread(() -> {
                    binding.tvTestResult.setText("SIP连接失败: " + reason);
                    binding.tvTestResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    Toast.makeText(SipSettingsActivity.this, "SIP连接失败", Toast.LENGTH_SHORT).show();
                });
            }
            
            // 其他回调方法的实现
            @Override public void onIncomingCall(org.linphone.core.Call call, String caller) {
                Log.d(TAG, "收到来电回调: " + caller);
            }
            @Override public void onCallProgress() {
                Log.d(TAG, "通话进行中回调");
            }
            @Override public void onCallEstablished() {
                Log.d(TAG, "通话已建立回调");
            }
            @Override public void onCallEnded() {
                Log.d(TAG, "通话已结束回调");
            }
            @Override public void onCallFailed(String reason) {
                Log.d(TAG, "通话失败回调: " + reason);
            }
        };
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "SipSettingsActivity销毁");
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
        super.onDestroy();
    }
}
