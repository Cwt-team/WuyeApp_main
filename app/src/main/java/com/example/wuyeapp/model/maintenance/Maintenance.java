package com.example.wuyeapp.model.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.network.client.RetrofitClient;
import com.example.wuyeapp.databinding.ActivityMaintenceBinding;
import com.example.wuyeapp.model.base.BaseResponese;
import com.example.wuyeapp.model.maintenance.MaintenanceRequest;
import com.example.wuyeapp.ui.maintenance.MaintenanceListActivity;
import com.example.wuyeapp.ui.maintenance.PersonalResidenceRepairActivity;
import com.example.wuyeapp.ui.maintenance.PublicFacilitiesRepairActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Maintenance extends AppCompatActivity {

    private static final String TAG = "Maintenance";
    private ActivityMaintenceBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMaintenceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 添加公共设施按钮的点击事件
        binding.btnPublicFacilities.setOnClickListener(v -> {
            Intent intent = new Intent(Maintenance.this, PublicFacilitiesRepairActivity.class);
            startActivity(intent);
        });

        // 添加个人住所按钮的点击事件
        binding.btnPersonalResidence.setOnClickListener(v -> {
            Intent intent = new Intent(Maintenance.this, PersonalResidenceRepairActivity.class);
            startActivity(intent);
        });
        
        // 添加历史记录按钮的点击事件
        binding.btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(Maintenance.this, MaintenanceListActivity.class);
            startActivity(intent);
        });
        
        // 设置返回按钮
        binding.btnBack.setOnClickListener(v -> finish());
    }
}