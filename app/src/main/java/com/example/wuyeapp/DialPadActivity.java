package com.example.wuyeapp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wuyeapp.databinding.ActivityDialPadBinding;

public class DialPadActivity extends AppCompatActivity {
    
    private ActivityDialPadBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDialPadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 返回按钮点击事件
        binding.btnBack.setOnClickListener(v -> finish());
        
        // 拨号按钮点击事件
        binding.btnCall.setOnClickListener(v -> {
            Toast.makeText(this, "拨号功能待实现", Toast.LENGTH_SHORT).show();
        });

        // 取消按钮点击事件
        binding.btnCancel.setOnClickListener(v -> finish());
    }
} 