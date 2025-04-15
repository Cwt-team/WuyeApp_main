package com.example.wuyeapp.ui.maintenance;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.network.client.RetrofitClient;
import com.example.wuyeapp.databinding.ActivityPublicFacilitiesRepairBinding;
import com.example.wuyeapp.model.base.BaseResponse;
import com.example.wuyeapp.model.maintenance.MaintenanceRequest;
import com.example.wuyeapp.model.user.OwnerInfo;
import com.example.wuyeapp.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublicFacilitiesRepairActivity extends AppCompatActivity {

    private static final String TAG = "PublicFacilitiesRepair";
    private ActivityPublicFacilitiesRepairBinding binding;
    private int selectedCommunityId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPublicFacilitiesRepairBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 获取当前登录用户的信息
        OwnerInfo owner = SessionManager.getInstance(this).getOwnerInfo();
        if (owner != null) {
            binding.contactNameInput.setText(owner.getName());
            binding.contactPhoneInput.setText(owner.getPhoneNumber());
            selectedCommunityId = owner.getCommunityId();
            
            // 设置社区名称
            if (selectedCommunityId > 0) {
                // 这里可以调用API获取社区名称，或者从本地缓存获取
                binding.communityTextView.setText("您的社区"); // 临时占位
            }
        }

        // 设置社区选择逻辑
        binding.communityTextView.setOnClickListener(v -> {
            // 可以显示一个社区选择对话框
            showCommunitySelectionDialog();
        });

        // 提交报修请求
        binding.submitButton.setOnClickListener(v -> submitRepairRequest());
        
        // 设置返回按钮
        binding.btnBack.setOnClickListener(v -> finish());
    }
    
    private void showCommunitySelectionDialog() {
        // 这里实现社区选择对话框
        // 可以调用API获取社区列表，然后显示一个对话框供用户选择
        Toast.makeText(this, "社区选择功能正在开发中", Toast.LENGTH_SHORT).show();
    }

    private void submitRepairRequest() {
        String title = binding.titleInput.getText().toString().trim();
        String description = binding.descriptionInput.getText().toString().trim();
        String contactName = binding.contactNameInput.getText().toString().trim();
        String contactPhone = binding.contactPhoneInput.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || contactName.isEmpty() || contactPhone.isEmpty()) {
            Toast.makeText(this, "请填写所有必填项", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedCommunityId <= 0) {
            Toast.makeText(this, "请选择有效的社区", Toast.LENGTH_SHORT).show();
            return;
        }

        // 添加日志以便调试
        Log.d(TAG, "提交公共设施报修请求: communityId=" + selectedCommunityId);

        // 显示加载进度
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.submitButton.setEnabled(false);

        // 创建完整的请求对象
        MaintenanceRequest request = new MaintenanceRequest(
            title, 
            description, 
            "public_facility", 
            "normal",
            selectedCommunityId,
            null, // 公共设施报修无需房屋ID
            contactName,
            contactPhone
        );

        RetrofitClient.getInstance().getApiService()
            .submitMaintenanceRequest(request)
            .enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.submitButton.setEnabled(true);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        BaseResponse baseResponse = response.body();
                        if (baseResponse.isSuccess()) {
                            Toast.makeText(PublicFacilitiesRepairActivity.this, "报修请求提交成功", Toast.LENGTH_SHORT).show();
                            finish(); // 返回上一个活动
                        } else {
                            Toast.makeText(PublicFacilitiesRepairActivity.this, baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "响应失败: " + response.code() + " " + response.message());
                        Toast.makeText(PublicFacilitiesRepairActivity.this, "提交失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.submitButton.setEnabled(true);
                    Log.e(TAG, "请求失败: " + t.getMessage());
                    Toast.makeText(PublicFacilitiesRepairActivity.this, "网络错误，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
