package com.example.wuyeapp.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wuyeapp.databinding.ActivityApplicationListBinding;
import com.example.wuyeapp.model.application.ApplicationListResponse;
import com.example.wuyeapp.model.application.HousingApplication;
import com.example.wuyeapp.network.client.RetrofitClient;
import com.example.wuyeapp.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApplicationListActivity extends AppCompatActivity {
    private ActivityApplicationListBinding binding;
    private ApplicationListAdapter adapter;
    private List<HousingApplication> applicationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityApplicationListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApplicationListAdapter(applicationList);
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefreshLayout.setOnRefreshListener(this::loadApplications);
        binding.btnBack.setOnClickListener(v -> finish());

        loadApplications();
    }

    private void loadApplications() {
        binding.swipeRefreshLayout.setRefreshing(true);
        long ownerId = SessionManager.getInstance(this).getOwnerInfo().getId();
        RetrofitClient.getInstance().getApiService().getApplications(ownerId)
            .enqueue(new Callback<ApplicationListResponse>() {
                @Override
                public void onResponse(@NonNull Call<ApplicationListResponse> call, @NonNull Response<ApplicationListResponse> response) {
                    binding.swipeRefreshLayout.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        applicationList.clear();
                        applicationList.addAll(response.body().getApplications());
                        adapter.notifyDataSetChanged();
                        binding.emptyView.setVisibility(applicationList.isEmpty() ? View.VISIBLE : View.GONE);
                    } else {
                        Toast.makeText(ApplicationListActivity.this, "获取申请记录失败", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ApplicationListResponse> call, @NonNull Throwable t) {
                    binding.swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(ApplicationListActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
} 