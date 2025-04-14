package com.example.wuyeapp.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.R;
import com.example.wuyeapp.network.client.RetrofitClient;
import com.example.wuyeapp.network.api.ApiService;
import com.example.wuyeapp.databinding.ActivityPersonalInfoBinding;
import com.example.wuyeapp.model.base.BaseResponse;
import com.example.wuyeapp.model.user.OwnerDetailResponse;
import com.example.wuyeapp.model.user.OwnerUpdateRequest;
import com.example.wuyeapp.utils.LogUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalInfoActivity extends AppCompatActivity {

    private static final String TAG = "PersonalInfoActivity";
    private ActivityPersonalInfoBinding binding;
    private ApiService apiService;
    private long ownerId;
    private OwnerDetailResponse.OwnerDetailData ownerDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonalInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化API服务
        apiService = RetrofitClient.getInstance().getApiService();
        
        // 获取传递的业主ID
        ownerId = getIntent().getLongExtra("ownerId", 0);
        if (ownerId == 0) {
            Toast.makeText(this, "业主ID错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 设置返回按钮
        binding.btnBack.setOnClickListener(v -> finish());
        
        // 设置保存按钮
        binding.btnSave.setOnClickListener(v -> saveChanges());
        
        // 设置修改密码按钮
        binding.changePassword.setOnClickListener(v -> showChangePasswordDialog());
        
        // 获取业主详情
        fetchOwnerDetail();
    }
    
    // 从API获取业主详细信息
    private void fetchOwnerDetail() {
        LogUtil.d(TAG + " 正在获取业主详细信息，ID: " + ownerId);
        binding.loadingProgress.setVisibility(View.VISIBLE);
        
        apiService.getOwnerDetail(ownerId).enqueue(new Callback<OwnerDetailResponse>() {
            @Override
            public void onResponse(Call<OwnerDetailResponse> call, Response<OwnerDetailResponse> response) {
                binding.loadingProgress.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ownerDetail = response.body().getData();
                    updateUI(ownerDetail);
                } else {
                    String errorMsg = "获取业主信息失败";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    LogUtil.e(TAG + " " + errorMsg);
                    Toast.makeText(PersonalInfoActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OwnerDetailResponse> call, Throwable t) {
                binding.loadingProgress.setVisibility(View.GONE);
                LogUtil.e(TAG + " 网络请求失败: " + t.getMessage());
                Toast.makeText(PersonalInfoActivity.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // 使用业主详细信息更新UI
    private void updateUI(OwnerDetailResponse.OwnerDetailData ownerDetail) {
        if (ownerDetail != null) {
            // 更新非可编辑信息
            binding.tvName.setText(ownerDetail.getName());
            binding.tvPhone.setText(ownerDetail.getPhoneNumber());
            binding.tvGender.setText("M".equals(ownerDetail.getGender()) ? "男" : "女");
            binding.tvIdCard.setText(maskIdCard(ownerDetail.getIdCard()));
            binding.tvCommunity.setText(ownerDetail.getCommunityInfo().getName());
            
            // 使用详细的房屋信息
            StringBuilder houseBuilder = new StringBuilder();
            if (ownerDetail.getHouseInfo().getDistrictNumber() != null) {
                houseBuilder.append(ownerDetail.getHouseInfo().getDistrictNumber()).append("区 ");
            }
            
            if (ownerDetail.getHouseInfo().getBuildingNumber() != null) {
                houseBuilder.append(ownerDetail.getHouseInfo().getBuildingNumber()).append("栋 ");
            }
            
            if (ownerDetail.getHouseInfo().getUnitNumber() != null) {
                houseBuilder.append(ownerDetail.getHouseInfo().getUnitNumber()).append("单元 ");
            }
            
            if (ownerDetail.getHouseInfo().getRoomNumber() != null) {
                houseBuilder.append(ownerDetail.getHouseInfo().getRoomNumber()).append("室");
            }
            
            binding.tvHouse.setText(houseBuilder.toString());
            
            // 更新可编辑信息
            binding.etEmail.setText(ownerDetail.getEmail());
            binding.etCity.setText(ownerDetail.getCity());
            binding.etAddress.setText(ownerDetail.getAddress());
        }
    }
    
    // 掩盖身份证号码
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 15) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }
    
    // 保存更改
    private void saveChanges() {
        if (ownerDetail == null) {
            Toast.makeText(this, "信息尚未加载完成", Toast.LENGTH_SHORT).show();
            return;
        }
        
        OwnerUpdateRequest request = new OwnerUpdateRequest();
        request.setEmail(binding.etEmail.getText().toString().trim());
        request.setCity(binding.etCity.getText().toString().trim());
        request.setAddress(binding.etAddress.getText().toString().trim());
        
        binding.loadingProgress.setVisibility(View.VISIBLE);
        apiService.updateOwnerInfo(ownerId, request).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                binding.loadingProgress.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(PersonalInfoActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "保存失败";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(PersonalInfoActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                binding.loadingProgress.setVisibility(View.GONE);
                Toast.makeText(PersonalInfoActivity.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // 显示修改密码对话框
    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("修改密码")
                .setView(dialogView)
                .setPositiveButton("确定", null) // 手动处理点击事件
                .setNegativeButton("取消", null)
                .create();
        
        dialog.show();
        
        // 手动处理确定按钮，避免对话框自动关闭
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String oldPassword = ((android.widget.EditText) dialogView.findViewById(R.id.et_old_password)).getText().toString();
            String newPassword = ((android.widget.EditText) dialogView.findViewById(R.id.et_new_password)).getText().toString();
            String confirmPassword = ((android.widget.EditText) dialogView.findViewById(R.id.et_confirm_password)).getText().toString();
            
            // 验证输入
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "请填写所有密码字段", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "新密码与确认密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 创建请求对象
            OwnerUpdateRequest request = new OwnerUpdateRequest();
            request.setOldPassword(oldPassword);
            request.setNewPassword(newPassword);
            
            // 发送请求
            binding.loadingProgress.setVisibility(View.VISIBLE);
            apiService.updateOwnerInfo(ownerId, request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    binding.loadingProgress.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(PersonalInfoActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        String errorMsg = "密码修改失败";
                        if (response.body() != null) {
                            errorMsg = response.body().getMessage();
                        }
                        Toast.makeText(PersonalInfoActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    binding.loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(PersonalInfoActivity.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
