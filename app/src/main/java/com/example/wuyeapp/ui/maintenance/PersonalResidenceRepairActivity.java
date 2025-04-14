package com.example.wuyeapp.ui.maintenance;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.R;
import com.example.wuyeapp.network.client.RetrofitClient;
import com.example.wuyeapp.model.base.BaseResponse;
import com.example.wuyeapp.model.maintenance.MaintenanceRequest;
import com.example.wuyeapp.model.user.OwnerInfo;
import com.example.wuyeapp.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalResidenceRepairActivity extends AppCompatActivity {

    private static final String TAG = "PersonalResidenceRepair";
    private EditText titleInput, descriptionInput, contactNameInput, contactPhoneInput;
    private TextView communityTextView;
    private Button submitButton;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_residence_repair); // 确保布局文件名正确

        titleInput = findViewById(R.id.title_input);
        descriptionInput = findViewById(R.id.description_input);
        contactNameInput = findViewById(R.id.contact_name_input);
        contactPhoneInput = findViewById(R.id.contact_phone_input);
        communityTextView = findViewById(R.id.community_text_view);
        submitButton = findViewById(R.id.submit_button);

        sessionManager = SessionManager.getInstance(this);

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

        OwnerInfo owner = sessionManager.getOwnerInfo();
        MaintenanceRequest request = new MaintenanceRequest(
            title, 
            description, 
            "personal_residence", 
            "normal",
            owner.getCommunityId(),
            owner.getHouseId(),
            owner.getName(),
            owner.getPhoneNumber()
        );

        RetrofitClient.getInstance().getApiService()
            .submitMaintenanceRequest(request)
            .enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        BaseResponse baseResponse = response.body();
                        if (baseResponse.isSuccess()) {
                            Toast.makeText(PersonalResidenceRepairActivity.this, "报修请求提交成功", Toast.LENGTH_SHORT).show();
                            finish(); // 返回上一个活动
                        } else {
                            Toast.makeText(PersonalResidenceRepairActivity.this, baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
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