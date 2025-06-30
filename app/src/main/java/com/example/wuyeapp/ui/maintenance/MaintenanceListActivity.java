package com.example.wuyeapp.ui.maintenance;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.wuyeapp.adapter.MaintenanceAdapter;
import com.example.wuyeapp.network.client.RetrofitClient;
import com.example.wuyeapp.databinding.ActivityMaintenanceListBinding;
import com.example.wuyeapp.model.maintenance.MaintenanceListResponse;
import com.example.wuyeapp.model.maintenance.MaintenanceRequest;
import com.example.wuyeapp.model.user.OwnerInfo;
import com.example.wuyeapp.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaintenanceListActivity extends AppCompatActivity {
    private static final String TAG = "MaintenanceList";
    private ActivityMaintenanceListBinding binding;
    private MaintenanceAdapter adapter;
    private List<MaintenanceRequest> maintenanceList = new ArrayList<>();
    private int currentPage = 1;
    private final int PAGE_SIZE = 10;
    private String currentStatus = "all"; // 默认获取所有状态
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMaintenanceListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 设置RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MaintenanceAdapter(this, maintenanceList);
        binding.recyclerView.setAdapter(adapter);
        
        // 设置状态过滤
        setupStatusFilter();
        
        // 加载数据
        loadMaintenanceList();
        
        // 设置下拉刷新
        binding.swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        
        // 设置返回按钮
        binding.btnBack.setOnClickListener(v -> finish());
    }
    
    private void setupStatusFilter() {
        binding.chipAll.setOnClickListener(v -> {
            currentStatus = "all";
            refreshData();
        });
        
        binding.chipPending.setOnClickListener(v -> {
            currentStatus = "pending";
            refreshData();
        });
        
        binding.chipProcessing.setOnClickListener(v -> {
            currentStatus = "processing";
            refreshData();
        });
        
        binding.chipCompleted.setOnClickListener(v -> {
            currentStatus = "completed";
            refreshData();
        });
    }
    
    private void refreshData() {
        currentPage = 1;
        maintenanceList.clear();
        adapter.notifyDataSetChanged();
        loadMaintenanceList();
    }
    
    private void loadMaintenanceList() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // 获取当前登录用户ID
        OwnerInfo owner = SessionManager.getInstance(this).getOwnerInfo();
        if (owner == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 添加认证token到请求头
        // 这应该从SessionManager中获取，确保身份验证
        String token = SessionManager.getInstance(this).getAuthToken();
        
        // 使用拦截器添加认证头
        RetrofitClient.getInstance().setAuthToken(token);
        
        RetrofitClient.getInstance().getApiService()
            .getMaintenanceList(owner.getId(), currentPage, PAGE_SIZE, 
                                "all".equals(currentStatus) ? null : currentStatus)
            .enqueue(new Callback<MaintenanceListResponse>() {
                @Override
                public void onResponse(Call<MaintenanceListResponse> call, 
                                      Response<MaintenanceListResponse> response) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    
                    if (response.isSuccessful() && response.body() != null && 
                        response.body().isSuccess()) {
                        MaintenanceListResponse.MaintenanceListData data = response.body().getData();
                        if (data != null && data.getItems() != null) {
                            maintenanceList.addAll(data.getItems());
                            adapter.notifyDataSetChanged();
                            
                            // 显示空视图或列表
                            if (maintenanceList.isEmpty()) {
                                binding.emptyView.setVisibility(View.VISIBLE);
                                binding.recyclerView.setVisibility(View.GONE);
                            } else {
                                binding.emptyView.setVisibility(View.GONE);
                                binding.recyclerView.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        Toast.makeText(MaintenanceListActivity.this, 
                                      "获取维修列表失败", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<MaintenanceListResponse> call, Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    Log.e(TAG, "获取维修列表失败: " + t.getMessage());
                    Toast.makeText(MaintenanceListActivity.this, 
                                 "网络错误，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            });
    }
} 