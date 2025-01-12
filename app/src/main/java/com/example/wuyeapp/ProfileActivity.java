package com.example.wuyeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
            Toast.makeText(this, "切换业主", Toast.LENGTH_SHORT).show();
            // TODO: 实现切换业主功能
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