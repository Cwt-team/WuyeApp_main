package com.example.wuyeapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.databinding.ActivityLoginBinding;
import com.example.wuyeapp.model.user.LoginRequest;
import com.example.wuyeapp.model.user.LoginResponse;
import com.example.wuyeapp.session.SessionManager;
import com.example.wuyeapp.ui.home.MainActivity;
import com.example.wuyeapp.utils.LogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.util.Log;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.wuyeapp.model.user.OwnerInfo;
import com.example.wuyeapp.network.client.RetrofitClient;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private SessionManager sessionManager;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i(TAG + " onCreate");
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化
        sessionManager = SessionManager.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // 如果已经登录，直接进入主页面
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        // 设置登录按钮点击事件
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        // 设置注册按钮点击事件
        binding.tvRegister.setOnClickListener(v -> {
            // 跳转到注册页面
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 将数据库测试按钮改为API连接测试按钮
        binding.btnTestDb.setText("测试API连接");
        binding.btnTestDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示加载对话框
                ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage("正在测试API连接...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                
                // 测试API连接
                Call<Void> call = RetrofitClient.getInstance().getApiService().testConnection();
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        progressDialog.dismiss();
                        
                        if (response.isSuccessful()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setTitle("API连接测试结果")
                                   .setMessage("API连接成功！可以正常访问服务器。")
                                   .setPositiveButton("确定", null)
                                   .create()
                                   .show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setTitle("API连接测试结果")
                                   .setMessage("API连接失败，服务器返回状态码: " + response.code())
                                   .setPositiveButton("确定", null)
                                   .create()
                                   .show();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setTitle("API连接测试结果")
                               .setMessage("API连接失败: " + t.getMessage())
                               .setPositiveButton("确定", null)
                               .create()
                               .show();
                    }
                });
            }
        });

        // 暂不登录按钮点击事件
        binding.btnSkipLogin.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG + " onDestroy");
        executorService.shutdown();
    }

    private void attemptLogin() {
        String username = binding.etPhone.getText().toString().trim();  // 这里的输入框可以输入手机号或账号
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入账号/手机号和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);

        // 调用登录API
        RetrofitClient.getApiService().login(username, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Log.i(TAG, "登录接口响应: " + response.toString());
                if (response.body() != null) {
                    Log.i(TAG, "登录接口响应body: " + response.body().toString());
                } else {
                    Log.w(TAG, "登录接口响应body为null");
                }
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess()) {
                        // 保存用户信息和token
                        sessionManager.createLoginSession(
                            loginResponse.getOwnerInfo(), 
                            loginResponse.getToken()
                        );
                        
                        // 修复：token判空，避免NullPointerException
                        if (loginResponse.getToken() == null || loginResponse.getToken().isEmpty()) {
                            Toast.makeText(LoginActivity.this, "登录异常，请重试", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // 设置RetrofitClient的认证token
                        RetrofitClient.getInstance().setAuthToken(loginResponse.getToken());
                        
                        // 跳转到主页面
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, 
                                     loginResponse.getMessage(), 
                                     Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, 
                                 "登录失败，请稍后重试", 
                                 Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "登录接口请求失败", t);
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                LogUtil.e(TAG, "Login failed", t);
                Toast.makeText(LoginActivity.this, 
                             "网络连接失败，请检查网络设置", 
                             Toast.LENGTH_SHORT).show();
            }
        });
    }
} 