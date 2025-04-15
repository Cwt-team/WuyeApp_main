package com.example.wuyeapp.ui.maintenance;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.databinding.ActivityMaintenceBinding;

public class MaintenanceActivity extends AppCompatActivity {

    private static final String TAG = "MaintenanceActivity";
    private ActivityMaintenceBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMaintenceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 添加公共设施按钮的点击事件
        binding.btnPublicFacilities.setOnClickListener(v -> {
            Intent intent = new Intent(MaintenanceActivity.this, PublicFacilitiesRepairActivity.class);
            startActivity(intent);
        });

        // 添加个人住所按钮的点击事件
        binding.btnPersonalResidence.setOnClickListener(v -> {
            Intent intent = new Intent(MaintenanceActivity.this, PersonalResidenceRepairActivity.class);
            startActivity(intent);
        });
        
        // 添加历史记录按钮的点击事件
        binding.btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MaintenanceActivity.this, MaintenanceListActivity.class);
            startActivity(intent);
        });
        
        // 设置返回按钮
        binding.btnBack.setOnClickListener(v -> finish());
    }
} 