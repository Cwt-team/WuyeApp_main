package com.example.wuyeapp.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.databinding.ActivitySipSettingsBinding;
import com.example.wuyeapp.sip.SipService;

public class SipSettingsActivity extends AppCompatActivity {

    private ActivitySipSettingsBinding binding;
    private SharedPreferences preferences;
    private static final String PREFS_NAME = "SipSettings";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_DOMAIN = "domain";
    private static final String KEY_PORT = "port";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySipSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // 加载已保存的设置
        loadSettings();

        // 保存按钮点击事件
        binding.btnSave.setOnClickListener(v -> saveSettings());
        
        // 测试按钮点击事件
        binding.btnTest.setOnClickListener(v -> testConnection());
    }

    private void loadSettings() {
        binding.etUsername.setText(preferences.getString(KEY_USERNAME, ""));
        binding.etPassword.setText(preferences.getString(KEY_PASSWORD, ""));
        binding.etDomain.setText(preferences.getString(KEY_DOMAIN, "8.138.26.199"));
        binding.etPort.setText(preferences.getString(KEY_PORT, "5060"));
    }

    private void saveSettings() {
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
        finish();
    }
    
    private void testConnection() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String domain = binding.etDomain.getText().toString().trim();
        String port = binding.etPort.getText().toString().trim();
        
        if (username.isEmpty() || password.isEmpty() || domain.isEmpty()) {
            Toast.makeText(this, "请先填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 这里应该实际测试SIP连接
        // 在实际应用中，您应该启动一个测试服务或使用SipService进行测试
        // 简单起见，我们这里只显示一个Toast
        Toast.makeText(this, "正在测试SIP连接...", Toast.LENGTH_SHORT).show();
        
        // 示例: 如何调用SipService进行测试连接
        // Intent intent = new Intent(this, SipService.class);
        // intent.setAction("TEST_CONNECTION");
        // intent.putExtra("username", username);
        // intent.putExtra("password", password);
        // intent.putExtra("domain", domain);
        // intent.putExtra("port", port);
        // startService(intent);
    }
}
