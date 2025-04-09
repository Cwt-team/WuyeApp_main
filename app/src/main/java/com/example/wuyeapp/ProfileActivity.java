package com.example.wuyeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.databinding.ActivityProfileBinding;
import com.example.wuyeapp.model.OwnerInfo;
import com.example.wuyeapp.session.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 获取当前登录的用户信息
        OwnerInfo currentOwner = SessionManager.getInstance(this).getOwnerInfo();
        if (currentOwner != null) {
            // 显示用户电话号码
            binding.phoneNumber.setText(currentOwner.getPhoneNumber());
            
            // 显示用户地址 (假设你有这个信息)
            // binding.address.setText("某某小区-某单元-某室");
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
            Toast.makeText(this, "人脸录制", Toast.LENGTH_SHORT).show();
            // TODO: 实现人脸录制功能
        });

        binding.settings.setOnClickListener(v -> {
            Toast.makeText(this, "设置", Toast.LENGTH_SHORT).show();
            // TODO: 实现设置功能
        });
    }
} 