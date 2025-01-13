package com.example.wuyeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wuyeapp.databinding.ActivityInviteVisitorBinding;

public class InviteVisitorActivity extends AppCompatActivity {

    private ActivityInviteVisitorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInviteVisitorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 返回按钮
        binding.btnBack.setOnClickListener(v -> finish());

        // 获取密码按钮
        binding.btnGetCode.setOnClickListener(v -> {
            Toast.makeText(this, "生成访客密码", Toast.LENGTH_SHORT).show();
            // TODO: 实现生成密码的逻辑
        });

        // 删除按钮
        binding.btnDelete.setOnClickListener(v -> {
            Toast.makeText(this, "删除密码", Toast.LENGTH_SHORT).show();
            // TODO: 实现删除密码的逻辑
        });

        // 分享按钮
        binding.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, 
                "访客密码: *#218455\n" +
                "有效期: 2025-01-08 00:00 至 2025-01-15 00:00");
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "分享访客密码"));
        });
    }
} 