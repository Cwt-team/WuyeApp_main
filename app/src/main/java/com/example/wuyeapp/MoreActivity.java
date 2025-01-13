package com.example.wuyeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.wuyeapp.databinding.ActivityMoreBinding;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;

public class MoreActivity extends AppCompatActivity {
    
    public static final String SELECTED_FUNCTIONS = "selected_functions";
    
    private ActivityMoreBinding binding;
    private boolean isManageMode = false;
    private MoreFunctionAdapter smartDoorAdapter;
    private MoreFunctionAdapter smartLifeAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        initView();
        initData();
        initListeners();
    }
    
    private void initView() {
        // 设置RecyclerView网格布局
        binding.smartDoorGrid.setLayoutManager(new GridLayoutManager(this, 4));
        binding.smartLifeGrid.setLayoutManager(new GridLayoutManager(this, 4));
        
        // 初始化适配器
        smartDoorAdapter = new MoreFunctionAdapter(this, true);
        smartLifeAdapter = new MoreFunctionAdapter(this, false);
        
        binding.smartDoorGrid.setAdapter(smartDoorAdapter);
        binding.smartLifeGrid.setAdapter(smartLifeAdapter);
    }
    
    private void initData() {
        // 初始化智能门禁功能列表
        List<FunctionItem> smartDoorItems = new ArrayList<>();
        smartDoorItems.add(new FunctionItem("户户通", R.drawable.ic_huhuotong, true));
        smartDoorItems.add(new FunctionItem("监视", R.drawable.ic_monitor, true));
        smartDoorItems.add(new FunctionItem("邀请访客", R.drawable.ic_yaoqingfangke, true));
        smartDoorItems.add(new FunctionItem("呼叫记录", R.drawable.ic_call_history, false));
        smartDoorItems.add(new FunctionItem("呼叫电梯", R.drawable.ic_hujiaodianti, true));
        smartDoorItems.add(new FunctionItem("扫码开门", R.drawable.ic_scan, true));
        
        // 初始化智慧生活功能列表
        List<FunctionItem> smartLifeItems = new ArrayList<>();
        smartLifeItems.add(new FunctionItem("社区通知", R.drawable.ic_shequtongzhi, true));
        smartLifeItems.add(new FunctionItem("报事报修", R.drawable.ic_repair, false));
        smartLifeItems.add(new FunctionItem("社区评价", R.drawable.ic_evaluate, false));
        smartLifeItems.add(new FunctionItem("投诉建议", R.drawable.ic_suggest, false));
        smartLifeItems.add(new FunctionItem("报警记录", R.drawable.ic_baojingjilu, true));
        
        smartDoorAdapter.setItems(smartDoorItems);
        smartLifeAdapter.setItems(smartLifeItems);
    }
    
    private void initListeners() {
        binding.btnBack.setOnClickListener(v -> {
            saveAndFinish();
        });
        
        binding.btnManage.setOnClickListener(v -> {
            isManageMode = !isManageMode;
            binding.btnManage.setText(isManageMode ? "完成" : "管理");
            smartDoorAdapter.setManageMode(isManageMode);
            smartLifeAdapter.setManageMode(isManageMode);
            
            if (!isManageMode) {
                checkAndResetDefaultApps();
                saveAndFinish();
            }
        });
    }
    
    private void checkAndResetDefaultApps() {
        int selectedCount = smartDoorAdapter.getSelectedCount() + 
                          smartLifeAdapter.getSelectedCount();
        
        if (selectedCount == 0) {
            // 重置前7个应用为选中状态
            smartDoorAdapter.resetDefaultSelection();
            smartLifeAdapter.resetDefaultSelection();
            Toast.makeText(this, "已重置默认应用", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveAndFinish() {
        // 收集所有选中的功能
        ArrayList<String> selectedFunctions = new ArrayList<>();
        for (FunctionItem item : smartDoorAdapter.getItems()) {
            if (item.isSelected()) {
                selectedFunctions.add(item.getName());
            }
        }
        for (FunctionItem item : smartLifeAdapter.getItems()) {
            if (item.isSelected()) {
                selectedFunctions.add(item.getName());
            }
        }
        
        // 将选中的功能返回给MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra(SELECTED_FUNCTIONS, selectedFunctions);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
} 