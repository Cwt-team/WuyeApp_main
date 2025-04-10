package com.example.wuyeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.api.RetrofitClient;
import com.example.wuyeapp.api.ApiService;
import com.example.wuyeapp.databinding.ActivityProfileBinding;
import com.example.wuyeapp.model.OwnerDetailResponse;
import com.example.wuyeapp.model.OwnerInfo;
import com.example.wuyeapp.session.SessionManager;
import com.example.wuyeapp.utils.LogUtil;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private ActivityProfileBinding binding;
    private ApiService apiService;
    private OwnerDetailResponse.OwnerDetailData ownerDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化API服务
        apiService = RetrofitClient.getInstance().getApiService();

        // 获取当前登录的用户信息
        OwnerInfo currentOwner = SessionManager.getInstance(this).getOwnerInfo();
        if (currentOwner != null && currentOwner.getId() != 0) {
            // 显示基本用户信息（从Session中获取）
            binding.phoneNumber.setText(currentOwner.getPhoneNumber());
            
            // 从API获取详细信息
            fetchOwnerDetail(currentOwner.getId());
        } else {
            LogUtil.e(TAG + " 当前登录用户信息无效");
            Toast.makeText(this, "登录信息已失效，请重新登录", Toast.LENGTH_SHORT).show();
            // 跳转到登录页面
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        // 设置底部导航栏点击事件
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // 结束当前活动
        });

        binding.navUnlock.setOnClickListener(v -> {
            Toast.makeText(this, "点击了开锁", Toast.LENGTH_SHORT).show();
            // TODO: 实现开锁功能
        });

        binding.navProfile.setOnClickListener(v -> {
            // 已经在"我的"页面，无需处理
        });

        // 设置功能按钮点击事件
        binding.switchOwner.setOnClickListener(v -> {
            // 退出登录的功能
            new AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 清除登录会话
                    SessionManager.getInstance(this).logout();
                    
                    // 跳转到登录页面
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("取消", null)
                .show();
        });

        binding.faceRecord.setOnClickListener(v -> {
            Intent intent = new Intent(this, FaceRecordActivity.class);
            intent.putExtra("ownerId", currentOwner.getId());
            startActivity(intent);
        });

        binding.settings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("ownerId", currentOwner.getId());
            startActivity(intent);
        });
        
        binding.personalInfo.setOnClickListener(v -> {
            if (ownerDetail != null) {
                Intent intent = new Intent(this, PersonalInfoActivity.class);
                intent.putExtra("ownerId", currentOwner.getId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "正在加载个人信息，请稍后再试", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // 从API获取业主详细信息
    private void fetchOwnerDetail(long ownerId) {
        LogUtil.d(TAG + " 正在获取业主详细信息，ID: " + ownerId);
        binding.loadingProgress.setVisibility(View.VISIBLE);
        
        apiService.getOwnerDetail(ownerId).enqueue(new Callback<OwnerDetailResponse>() {
            @Override
            public void onResponse(Call<OwnerDetailResponse> call, Response<OwnerDetailResponse> response) {
                binding.loadingProgress.setVisibility(View.GONE);
                
                if (response.isSuccessful()) {
                    LogUtil.d(TAG + " API响应成功");
                    if (response.body() != null) {
                        LogUtil.d(TAG + " 响应体不为空");
                        
                        if (response.body().isSuccess()) {
                            LogUtil.d(TAG + " 业务处理成功");
                            ownerDetail = response.body().getData();
                            if (ownerDetail != null) {
                                LogUtil.d(TAG + " 成功获取业主详情数据");
                                updateUI(ownerDetail);
                            } else {
                                LogUtil.e(TAG + " 业主详情数据为空");
                                Toast.makeText(ProfileActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            LogUtil.e(TAG + " 业务处理失败: " + response.body().getMessage());
                            Toast.makeText(ProfileActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        LogUtil.e(TAG + " 响应体为空");
                        Toast.makeText(ProfileActivity.this, "服务器响应异常", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    LogUtil.e(TAG + " API响应失败: " + response.code());
                    Toast.makeText(ProfileActivity.this, "网络请求失败: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OwnerDetailResponse> call, Throwable t) {
                binding.loadingProgress.setVisibility(View.GONE);
                LogUtil.e(TAG + " 网络请求失败: " + t.getMessage());
                Toast.makeText(ProfileActivity.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // 使用业主详细信息更新UI
    private void updateUI(OwnerDetailResponse.OwnerDetailData ownerDetail) {
        if (ownerDetail != null) {
            // 更新地址信息
            binding.address.setText(ownerDetail.getHouseInfo().getFullName());
            
            // 更新其他UI元素
            // 根据面部识别状态更新人脸录制按钮的状态
            if (ownerDetail.getFaceStatus() == 1) {
                binding.faceStatusIcon.setVisibility(View.VISIBLE);
            } else {
                binding.faceStatusIcon.setVisibility(View.GONE);
            }
        }
    }
} 