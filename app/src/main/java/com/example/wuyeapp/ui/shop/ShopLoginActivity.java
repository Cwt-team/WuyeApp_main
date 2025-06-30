package com.example.wuyeapp.ui.shop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.databinding.ActivityShopLoginBinding;
import com.example.wuyeapp.model.shop.ShopAuthResponse;
import com.example.wuyeapp.model.user.LoginRequest;
import com.example.wuyeapp.network.api.ShopAuthApiService;
import com.example.wuyeapp.network.client.RetrofitClient;
import com.example.wuyeapp.session.SessionManager;
import com.example.wuyeapp.utils.LogUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopLoginActivity extends AppCompatActivity {
    private static final String TAG = "ShopLoginActivity";
    private ActivityShopLoginBinding binding;
    private ShopAuthApiService shopAuthApiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        shopAuthApiService = RetrofitClient.getInstance().getShopAuthApiService();
        sessionManager = SessionManager.getInstance(this);

        // 设置登录按钮点击事件
        binding.btnLogin.setOnClickListener(v -> performShopLogin());
    }

    private void performShopLogin() {
        String loginId = binding.etLoginId.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // 输入验证
        if (loginId.isEmpty()) {
            binding.etLoginId.setError("请输入账号");
            return;
        }
        if (password.isEmpty()) {
            binding.etPassword.setError("请输入密码");
            return;
        }

        // 显示加载进度
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);

        // 创建登录请求
        LoginRequest request = new LoginRequest(loginId, password);

        // 发起登录请求
        shopAuthApiService.login(request).enqueue(new Callback<ShopAuthResponse>() {
            @Override
            public void onResponse(Call<ShopAuthResponse> call, Response<ShopAuthResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ShopAuthResponse authResponse = response.body();
                    // 保存登录会话
                    sessionManager.createShopLoginSession(authResponse);
                    
                    // 设置RetrofitClient的认证token
                    RetrofitClient.getInstance().setAuthToken(authResponse.getToken());
                    
                    LogUtil.i(TAG, "商城登录成功：" + authResponse.getDisplayName());
                    
                    // 跳转到商城主页
                    Intent intent = new Intent(ShopLoginActivity.this, ShopActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMessage = "登录失败：" + (response.code() == 401 ? "账号或密码错误" : "请稍后重试");
                    Toast.makeText(ShopLoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    LogUtil.e(TAG, "登录失败：HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ShopAuthResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                
                String errorMessage = "网络连接失败，请检查网络设置";
                Toast.makeText(ShopLoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                LogUtil.e(TAG, "登录请求失败", t);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
} 