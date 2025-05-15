package com.example.wuyeapp.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.databinding.ActivityHouseBindingBinding;
import com.example.wuyeapp.model.base.BaseResponse;
import com.example.wuyeapp.model.community.Community;
import com.example.wuyeapp.model.community.CommunitiesResponse;
import com.example.wuyeapp.network.client.RetrofitClient;
import com.example.wuyeapp.session.SessionManager;
import com.example.wuyeapp.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HouseBindingActivity extends AppCompatActivity {

    private static final String TAG = "HouseBindingActivity";
    private ActivityHouseBindingBinding binding;
    private SessionManager sessionManager;
    private List<Community> communities = new ArrayList<>();
    private int selectedCommunityId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHouseBindingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        sessionManager = SessionManager.getInstance(this);
        
        // 设置返回按钮
        binding.ivBack.setOnClickListener(v -> finish());
        
        // 加载社区列表
        loadCommunities();
        
        // 设置社区下拉选择监听
        binding.spinnerCommunity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position <= communities.size()) {
                    selectedCommunityId = communities.get(position - 1).getId();
                } else {
                    selectedCommunityId = -1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCommunityId = -1;
            }
        });
        
        // 设置提交按钮点击事件
        binding.btnSubmitApplication.setOnClickListener(v -> {
            if (validateInputs()) {
                submitApplication();
            }
        });
    }
    
    // 加载社区列表
    private void loadCommunities() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance().getApiService()
                .getCommunities()
                .enqueue(new Callback<CommunitiesResponse>() {
                    @Override
                    public void onResponse(Call<CommunitiesResponse> call, Response<CommunitiesResponse> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            communities = response.body().getCommunities();
                            setupCommunitySpinner();
                        } else {
                            Toast.makeText(HouseBindingActivity.this, "获取社区列表失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CommunitiesResponse> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(HouseBindingActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    // 设置社区下拉选择器
    private void setupCommunitySpinner() {
        List<String> communityNames = new ArrayList<>();
        communityNames.add("请选择社区");
        
        for (Community community : communities) {
            communityNames.add(community.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, 
                android.R.layout.simple_spinner_item, 
                communityNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCommunity.setAdapter(adapter);
    }
    
    // 验证输入
    private boolean validateInputs() {
        boolean isValid = true;
        
        if (selectedCommunityId == -1) {
            Toast.makeText(this, "请选择社区", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        String buildingName = binding.etBuilding.getText().toString().trim();
        if (TextUtils.isEmpty(buildingName)) {
            binding.tilBuilding.setError("请输入楼栋号");
            isValid = false;
        } else {
            binding.tilBuilding.setError(null);
        }
        
        String unitName = binding.etUnit.getText().toString().trim();
        if (TextUtils.isEmpty(unitName)) {
            binding.tilUnit.setError("请输入单元号");
            isValid = false;
        } else {
            binding.tilUnit.setError(null);
        }
        
        String houseNumber = binding.etHouseNumber.getText().toString().trim();
        if (TextUtils.isEmpty(houseNumber)) {
            binding.tilHouseNumber.setError("请输入房号");
            isValid = false;
        } else {
            binding.tilHouseNumber.setError(null);
        }
        
        String idCard = binding.etIdCard.getText().toString().trim();
        if (TextUtils.isEmpty(idCard)) {
            binding.tilIdCard.setError("请输入身份证号");
            isValid = false;
        } else if (!idCard.matches("^\\d{17}[\\dX]$")) {
            binding.tilIdCard.setError("请输入正确的身份证号码");
            isValid = false;
        } else {
            binding.tilIdCard.setError(null);
        }
        
        return isValid;
    }
    
    // 提交申请
    private void submitApplication() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmitApplication.setEnabled(false);
        
        long ownerId = sessionManager.getOwnerInfo().getId();
        String buildingName = binding.etBuilding.getText().toString().trim();
        String unitName = binding.etUnit.getText().toString().trim();
        String houseNumber = binding.etHouseNumber.getText().toString().trim();
        String idCard = binding.etIdCard.getText().toString().trim();
        
        RetrofitClient.getInstance().getApiService()
                .submitHousingApplication(
                        ownerId, 
                        selectedCommunityId,
                        buildingName,
                        unitName,
                        houseNumber,
                        idCard
                )
                .enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSubmitApplication.setEnabled(true);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse baseResponse = response.body();
                            if (baseResponse.isSuccess()) {
                                LogUtil.i(TAG + " 申请提交成功");
                                Toast.makeText(HouseBindingActivity.this, baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                finish(); // 返回上一个界面
                            } else {
                                LogUtil.w(TAG + " 申请提交失败: " + baseResponse.getMessage());
                                Toast.makeText(HouseBindingActivity.this, baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            LogUtil.w(TAG + " 申请提交失败: 服务器响应错误");
                            Toast.makeText(HouseBindingActivity.this, "服务器响应错误", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSubmitApplication.setEnabled(true);
                        
                        LogUtil.e(TAG + " 申请提交请求失败: " + t.getMessage());
                        Toast.makeText(HouseBindingActivity.this, "网络连接失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
} 