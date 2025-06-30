package com.example.wuyeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

import com.example.wuyeapp.databinding.ActivityMainBinding;
import com.example.wuyeapp.model.user.OwnerInfo;
import com.example.wuyeapp.session.SessionManager;
import com.example.wuyeapp.ui.auth.LoginActivity;
import com.example.wuyeapp.utils.LogUtil;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity 
        implements NavigationView.OnNavigationItemSelectedListener {
    
    private ActivityMainBinding binding;
    private SessionManager sessionManager;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 设置 Toolbar
        setSupportActionBar(binding.toolbar);

        // 初始化 SessionManager
        sessionManager = SessionManager.getInstance(this);

        // 检查用户是否已登录
        if (!sessionManager.isLoggedIn()) {
            // 未登录，跳转到登录页面
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 设置抽屉布局
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(this);
        
        // 获取并显示用户信息
        OwnerInfo ownerInfo = sessionManager.getOwnerInfo();
        if (ownerInfo != null) {
            updateNavigationHeader(ownerInfo);
        }
    }

    private void updateNavigationHeader(OwnerInfo ownerInfo) {
        // 更新导航头部的用户信息
        NavigationView navigationView = binding.navView;
        if (navigationView.getHeaderView(0) != null) {
            // 获取头部视图中的文本视图
            android.widget.TextView tvUsername = navigationView.getHeaderView(0).findViewById(R.id.tv_username);
            android.widget.TextView tvEmail = navigationView.getHeaderView(0).findViewById(R.id.tv_email);
            
            // 设置用户信息
            if (tvUsername != null) {
                tvUsername.setText(ownerInfo.getName());
            }
            if (tvEmail != null) {
                tvEmail.setText(ownerInfo.getEmail());
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // 处理导航项的点击
        int id = item.getItemId();
        
        if (id == R.id.nav_logout) {
            // 执行登出操作
            logout();
            return true;
        }
        
        // 处理其他导航项...
        binding.drawerLayout.closeDrawers();
        return true;
    }

    private void logout() {
        // 清除会话
        sessionManager.logout();
        
        // 跳转到登录页面
        Intent intent = new Intent(this, LoginActivity.class);
        // 清除任务栈中的其他Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 