package com.example.wuyeapp.ui.maintenance;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.R;
import com.example.wuyeapp.network.client.RetrofitClient;
import com.example.wuyeapp.databinding.ActivityMaintenanceDetailBinding;
import com.example.wuyeapp.model.base.BaseResponse;
import com.example.wuyeapp.model.maintenance.MaintenanceDetailResponse;
import com.example.wuyeapp.model.maintenance.MaintenanceRequest;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.widget.EditText;
import android.widget.RatingBar;

public class MaintenanceDetailActivity extends AppCompatActivity {
    private static final String TAG = "MaintenanceDetail";
    private ActivityMaintenanceDetailBinding binding;
    private long maintenanceId;
    private MaintenanceRequest maintenance;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMaintenanceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 获取传入的报修ID
        maintenanceId = getIntent().getLongExtra("maintenanceId", -1);
        if (maintenanceId == -1) {
            Toast.makeText(this, "报修信息不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 设置返回按钮
        binding.btnBack.setOnClickListener(v -> finish());
        
        // 设置取消报修按钮
        binding.btnCancel.setOnClickListener(v -> showCancelConfirmDialog());
        
        // 设置评价按钮
        binding.btnEvaluate.setOnClickListener(v -> showEvaluationDialog());
        
        // 加载报修详情
        loadMaintenanceDetail();
    }
    
    private void loadMaintenanceDetail() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        RetrofitClient.getInstance().getApiService()
            .getMaintenanceDetail(maintenanceId)
            .enqueue(new Callback<MaintenanceDetailResponse>() {
                @Override
                public void onResponse(Call<MaintenanceDetailResponse> call, 
                                      Response<MaintenanceDetailResponse> response) {
                    binding.progressBar.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null && 
                        response.body().isSuccess()) {
                        maintenance = response.body().getData();
                        displayMaintenanceDetail();
                    } else {
                        Toast.makeText(MaintenanceDetailActivity.this, 
                                     "获取报修详情失败", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<MaintenanceDetailResponse> call, Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "获取报修详情失败: " + t.getMessage());
                    Toast.makeText(MaintenanceDetailActivity.this, 
                                 "网络错误，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void displayMaintenanceDetail() {
        if (maintenance == null) return;
        
        // 设置基本信息
        binding.tvTitle.setText(maintenance.getTitle());
        binding.tvDescription.setText(maintenance.getDescription());
        binding.tvStatus.setText(getStatusText(maintenance.getStatus()));
        binding.tvStatus.setTextColor(getStatusColor(maintenance.getStatus()));
        binding.tvType.setText(getTypeText(maintenance.getType()));
        binding.tvPriority.setText(getPriorityText(maintenance.getPriority()));
        
        // 设置时间信息
        if (maintenance.getReportTime() != null) {
            binding.tvReportTime.setText(dateFormat.format(maintenance.getReportTime()));
        }
        
        if (maintenance.getExpectedTime() != null) {
            binding.tvExpectedTime.setText(dateFormat.format(maintenance.getExpectedTime()));
        } else {
            binding.tvExpectedTime.setText("未指定");
        }
        
        // 设置处理信息
        if (maintenance.getStatus().equals("processing") || 
            maintenance.getStatus().equals("completed")) {
            binding.layoutProcessInfo.setVisibility(View.VISIBLE);
            binding.tvHandlerName.setText(maintenance.getHandlerName());
            binding.tvHandlerPhone.setText(maintenance.getHandlerPhone());
            
            if (maintenance.getProcessTime() != null) {
                binding.tvProcessTime.setText(dateFormat.format(maintenance.getProcessTime()));
            }
        } else {
            binding.layoutProcessInfo.setVisibility(View.GONE);
        }
        
        // 设置完成信息
        if (maintenance.getStatus().equals("completed")) {
            binding.layoutCompleteInfo.setVisibility(View.VISIBLE);
            
            if (maintenance.getCompleteTime() != null) {
                binding.tvCompleteTime.setText(dateFormat.format(maintenance.getCompleteTime()));
            }
        } else {
            binding.layoutCompleteInfo.setVisibility(View.GONE);
        }
        
        // 设置按钮状态
        updateButtonState();
    }
    
    private void updateButtonState() {
        if (maintenance == null) return;
        
        // 只有待处理和处理中的报修才能取消
        if ("pending".equals(maintenance.getStatus()) || 
            "assigned".equals(maintenance.getStatus())) {
            binding.btnCancel.setVisibility(View.VISIBLE);
        } else {
            binding.btnCancel.setVisibility(View.GONE);
        }
        
        // 只有已完成的报修才能评价
        if ("completed".equals(maintenance.getStatus()) && 
            (maintenance.getEvaluationScore() == null || maintenance.getEvaluationScore() == 0)) {
            binding.btnEvaluate.setVisibility(View.VISIBLE);
        } else {
            binding.btnEvaluate.setVisibility(View.GONE);
        }
        
        // 如果已经评价，显示评价内容
        if (maintenance.getEvaluationScore() != null && maintenance.getEvaluationScore() > 0) {
            binding.layoutEvaluation.setVisibility(View.VISIBLE);
            binding.tvEvaluationScore.setText(String.valueOf(maintenance.getEvaluationScore()));
            binding.tvEvaluationContent.setText(maintenance.getEvaluationContent());
            
            if (maintenance.getEvaluationTime() != null) {
                binding.tvEvaluationTime.setText(dateFormat.format(maintenance.getEvaluationTime()));
            }
        } else {
            binding.layoutEvaluation.setVisibility(View.GONE);
        }
    }
    
    private void showCancelConfirmDialog() {
        new AlertDialog.Builder(this)
            .setTitle("取消报修")
            .setMessage("确定要取消该报修请求吗？")
            .setPositiveButton("确定", (dialog, which) -> cancelMaintenanceRequest())
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void cancelMaintenanceRequest() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        RetrofitClient.getInstance().getApiService()
            .cancelMaintenanceRequest(maintenanceId)
            .enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    binding.progressBar.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null && 
                        response.body().isSuccess()) {
                        Toast.makeText(MaintenanceDetailActivity.this, 
                                     "报修已取消", Toast.LENGTH_SHORT).show();
                        // 重新加载报修详情
                        loadMaintenanceDetail();
                    } else {
                        String message = response.body() != null ? 
                                        response.body().getMessage() : "取消报修失败";
                        Toast.makeText(MaintenanceDetailActivity.this, 
                                     message, Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "取消报修失败: " + t.getMessage());
                    Toast.makeText(MaintenanceDetailActivity.this, 
                                 "网络错误，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void showEvaluationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_maintenance_evaluation, null);
        final RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
        final EditText etContent = dialogView.findViewById(R.id.et_evaluation_content);
        
        new AlertDialog.Builder(this)
            .setTitle("评价维修服务")
            .setView(dialogView)
            .setPositiveButton("提交", (dialog, which) -> {
                int score = (int) ratingBar.getRating();
                String content = etContent.getText().toString().trim();
                
                if (score == 0) {
                    Toast.makeText(this, "请选择评分", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                submitEvaluation(score, content);
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void submitEvaluation(int score, String content) {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        Map<String, Object> evaluationData = new HashMap<>();
        evaluationData.put("score", score);
        evaluationData.put("content", content);
        
        RetrofitClient.getInstance().getApiService()
            .evaluateMaintenanceRequest(maintenanceId, evaluationData)
            .enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    binding.progressBar.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null && 
                        response.body().isSuccess()) {
                        Toast.makeText(MaintenanceDetailActivity.this, 
                                     "评价提交成功", Toast.LENGTH_SHORT).show();
                        // 重新加载报修详情
                        loadMaintenanceDetail();
                    } else {
                        String message = response.body() != null ? 
                                        response.body().getMessage() : "评价提交失败";
                        Toast.makeText(MaintenanceDetailActivity.this, 
                                     message, Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "评价提交失败: " + t.getMessage());
                    Toast.makeText(MaintenanceDetailActivity.this, 
                                 "网络错误，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private String getStatusText(String status) {
        if (status == null) return "未知";
        
        switch (status) {
            case "pending": return "待处理";
            case "assigned": return "已分配";
            case "processing": return "处理中";
            case "completed": return "已完成";
            case "cancelled": return "已取消";
            case "rejected": return "已驳回";
            default: return "未知";
        }
    }
    
    private int getStatusColor(String status) {
        if (status == null) return getResources().getColor(R.color.gray);
        
        switch (status) {
            case "pending": return getResources().getColor(R.color.orange);
            case "assigned": return getResources().getColor(R.color.blue);
            case "processing": return getResources().getColor(R.color.blue);
            case "completed": return getResources().getColor(R.color.green);
            case "cancelled": return getResources().getColor(R.color.gray);
            case "rejected": return getResources().getColor(R.color.red);
            default: return getResources().getColor(R.color.gray);
        }
    }
    
    private String getTypeText(String type) {
        if (type == null) return "其他";
        
        switch (type) {
            case "water_electric": return "水电维修";
            case "decoration": return "装修维修";
            case "public_facility": return "公共设施";
            case "clean": return "保洁服务";
            case "security": return "安保服务";
            case "personal_residence": return "个人住所";
            default: return "其他";
        }
    }
    
    private String getPriorityText(String priority) {
        if (priority == null) return "普通";
        
        switch (priority) {
            case "low": return "低";
            case "normal": return "普通";
            case "high": return "高";
            case "urgent": return "紧急";
            default: return "普通";
        }
    }
} 