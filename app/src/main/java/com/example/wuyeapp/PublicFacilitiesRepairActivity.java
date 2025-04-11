package com.example.wuyeapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.api.RetrofitClient;
import com.example.wuyeapp.model.BaseResponse;
import com.example.wuyeapp.model.MaintenanceRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublicFacilitiesRepairActivity extends AppCompatActivity {

    private static final String TAG = "PublicFacilitiesRepair";
    private EditText titleInput, descriptionInput, contactNameInput, contactPhoneInput;
    private TextView communityTextView;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_facilities_repair); // 确保布局文件名正确

        titleInput = findViewById(R.id.title_input);
        descriptionInput = findViewById(R.id.description_input);
        contactNameInput = findViewById(R.id.contact_name_input);
        contactPhoneInput = findViewById(R.id.contact_phone_input);
        communityTextView = findViewById(R.id.community_text_view);
        submitButton = findViewById(R.id.submit_button);

        // 设置社区选择逻辑
        communityTextView.setOnClickListener(v -> {
            // TODO: 实现社区选择逻辑
        });

        // 提交报修请求
        submitButton.setOnClickListener(v -> submitRepairRequest());
    }

    private void submitRepairRequest() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String contactName = contactNameInput.getText().toString().trim();
        String contactPhone = contactPhoneInput.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || contactName.isEmpty() || contactPhone.isEmpty()) {
            Toast.makeText(this, "请填写所有必填项", Toast.LENGTH_SHORT).show();
            return;
        }

        MaintenanceRequest request = new MaintenanceRequest(title, description, "public_facility", "normal");
        // TODO: 设置社区ID和其他信息

        RetrofitClient.getInstance().getApiService()
            .submitMaintenanceRequest(request)
            .enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
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
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    Log.e(TAG, "请求失败: " + t.getMessage());
                }
            });
    }
}
