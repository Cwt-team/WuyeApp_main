package com.example.wuyeapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.databinding.ActivityRegisterBinding;
import com.example.wuyeapp.model.base.BaseResponse;
import com.example.wuyeapp.network.client.RetrofitClient;
import com.example.wuyeapp.utils.LogUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i(TAG + " onCreate");
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 设置返回登录按钮点击事件
        binding.tvBackToLogin.setOnClickListener(v -> {
            finish();
        });

        // 设置注册按钮点击事件
        binding.btnRegister.setOnClickListener(v -> {
            // 获取用户输入
            String account = binding.etAccount.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
            String name = binding.etName.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();

            // 验证输入
            if (TextUtils.isEmpty(account)) {
                binding.tilAccount.setError("请输入账号");
                return;
            } else {
                binding.tilAccount.setError(null);
            }

            if (TextUtils.isEmpty(password)) {
                binding.tilPassword.setError("请输入密码");
                return;
            } else {
                binding.tilPassword.setError(null);
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                binding.tilConfirmPassword.setError("请再次输入密码");
                return;
            } else {
                binding.tilConfirmPassword.setError(null);
            }

            if (!password.equals(confirmPassword)) {
                binding.tilConfirmPassword.setError("两次输入的密码不一致");
                return;
            } else {
                binding.tilConfirmPassword.setError(null);
            }

            if (TextUtils.isEmpty(name)) {
                binding.tilName.setError("请输入姓名");
                return;
            } else {
                binding.tilName.setError(null);
            }

            if (TextUtils.isEmpty(phone)) {
                binding.tilPhone.setError("请输入手机号码");
                return;
            } else {
                binding.tilPhone.setError(null);
            }

            // 验证手机号格式
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                binding.tilPhone.setError("请输入正确的手机号码");
                return;
            } else {
                binding.tilPhone.setError(null);
            }

            // 显示加载进度
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnRegister.setEnabled(false);

            // 调用注册API
            RetrofitClient.getInstance().getApiService()
                    .register(account, password, name, phone)
                    .enqueue(new Callback<BaseResponse>() {
                        @Override
                        public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                            // 隐藏进度条
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnRegister.setEnabled(true);
                            
                            if (response.isSuccessful() && response.body() != null) {
                                BaseResponse registerResponse = response.body();
                                if (registerResponse.isSuccess()) {
                                    LogUtil.i(TAG + " 注册成功: " + account);
                                    
                                    // 显示成功提示
                                    Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                                    
                                    // 返回登录页面
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    LogUtil.w(TAG + " 注册失败: " + registerResponse.getMessage());
                                    // 注册失败
                                    Toast.makeText(RegisterActivity.this, registerResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                LogUtil.w(TAG + " 注册失败: 服务器响应错误");
                                Toast.makeText(RegisterActivity.this, "服务器响应错误", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<BaseResponse> call, Throwable t) {
                            // 隐藏进度条
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnRegister.setEnabled(true);
                            
                            LogUtil.e(TAG + " 注册请求失败: " + t.getMessage());
                            Toast.makeText(RegisterActivity.this, "网络连接失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG + " onDestroy");
    }
} 