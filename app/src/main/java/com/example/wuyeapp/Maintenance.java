package com.example.wuyeapp;

import android.content.Intent; // 添加这一行
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.api.RetrofitClient;
import com.example.wuyeapp.model.BaseResponse;
import com.example.wuyeapp.model.MaintenanceRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Maintenance extends AppCompatActivity {

    private static final String TAG = "Maintenance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintence); // 确保布局文件名正确

        // 示例：提交报修请求
        submitMaintenanceRequest(new MaintenanceRequest("水管漏水", "厨房水管漏水需要维修", "water_electric", "high"));

        // 添加公共设施按钮的点击事件
        findViewById(R.id.btn_public_facilities).setOnClickListener(v -> {
            Intent intent = new Intent(Maintenance.this, PublicFacilitiesRepairActivity.class);
            startActivity(intent);
        });

        // 添加个人住所按钮的点击事件
        findViewById(R.id.btn_personal_residence).setOnClickListener(v -> {
            Intent intent = new Intent(Maintenance.this, PersonalResidenceRepairActivity.class);
            startActivity(intent);
        });
    }

    private void submitMaintenanceRequest(MaintenanceRequest request) {
        RetrofitClient.getInstance().getApiService()
            .submitMaintenanceRequest(request)
            .enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        BaseResponse baseResponse = response.body();
                        if (baseResponse.isSuccess()) {
                            Log.i(TAG, "报修请求提交成功: " + baseResponse.getMessage());
                        } else {
                            Log.w(TAG, "报修请求提交失败: " + baseResponse.getMessage());
                        }
                    } else {
                        Log.e(TAG, "响应失败: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    Log.e(TAG, "请求失败: " + t.getMessage());
                }
            });
    }
}