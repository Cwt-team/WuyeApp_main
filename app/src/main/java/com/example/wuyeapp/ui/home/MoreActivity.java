package com.example.wuyeapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.wuyeapp.R;
import com.example.wuyeapp.adapter.MoreFunctionAdapter;
import com.example.wuyeapp.common.FunctionItem;
import com.example.wuyeapp.databinding.ActivityMoreBinding;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;
import com.example.wuyeapp.utils.LogUtil;

public class MoreActivity extends AppCompatActivity {
    
    private static final String TAG = "MoreActivity";
    
    public static final String SELECTED_FUNCTIONS = "selected_functions";
    
    private ActivityMoreBinding binding;
    private boolean isManageMode = false;
    private MoreFunctionAdapter smartDoorAdapter;
    private MoreFunctionAdapter smartLifeAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i(TAG + " onCreate");
        binding = ActivityMoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        initView();
        initData();
        
        // 返回按钮点击事件
        binding.btnBack.setOnClickListener(v -> {
            saveAndFinish();
        });
        
        // 管理按钮点击事件
        binding.btnManage.setOnClickListener(v -> {
            isManageMode = !isManageMode;
            binding.btnManage.setText(isManageMode ? "完成" : "管理");
            
            // 显示/隐藏提示文字
            binding.tvSmartDoorHint.setVisibility(isManageMode ? View.VISIBLE : View.GONE);
            binding.tvSmartLifeHint.setVisibility(isManageMode ? View.VISIBLE : View.GONE);
            
            // 设置adapter管理模式
            smartDoorAdapter.setManageMode(isManageMode);
            smartLifeAdapter.setManageMode(isManageMode);
        });
    }
    
    private void initView() {
        // 设置RecyclerView网格布局
        binding.smartDoorGrid.setLayoutManager(new GridLayoutManager(this, 4));
        binding.smartLifeGrid.setLayoutManager(new GridLayoutManager(this, 4));
        
        // 初始化适配器
        smartDoorAdapter = new MoreFunctionAdapter(this, false);
        smartLifeAdapter = new MoreFunctionAdapter(this, false);
        
        binding.smartDoorGrid.setAdapter(smartDoorAdapter);
        binding.smartLifeGrid.setAdapter(smartLifeAdapter);
    }
    
    private void initData() {
        // 获取保存的选中状态
        SharedPreferences prefs = getSharedPreferences("QuickActions", MODE_PRIVATE);
        Set<String> savedFunctions = new HashSet<>();
        int count = prefs.getInt("count", 0);
        for (int i = 0; i < count; i++) {
            savedFunctions.add(prefs.getString("function_" + i, ""));
        }
        
        // 初始化智能门禁功能列表
        List<FunctionItem> smartDoorItems = getSmartDoorItems();
        // 根据保存的状态设置选中状态
        for (FunctionItem item : smartDoorItems) {
            item.setSelected(savedFunctions.contains(item.getName()));
            item.setHomeApp(item.isSelected());
        }
        
        // 初始化智慧生活功能列表
        List<FunctionItem> smartLifeItems = getSmartLifeItems();
        // 根据保存的状态设置选中状态
        for (FunctionItem item : smartLifeItems) {
            item.setSelected(savedFunctions.contains(item.getName()));
            item.setHomeApp(item.isSelected());
        }
        
        smartDoorAdapter.setItems(smartDoorItems);
        smartLifeAdapter.setItems(smartLifeItems);
    }
    
    private void saveAndFinish() {
        LogUtil.d(TAG + " 保存并退出");
        ArrayList<String> selectedFunctions = new ArrayList<>();
        
        // 收集所有选中的功能
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
        
        // 确保选中的功能数量不超过7个（为"更多"按钮预留一个位置）
        if (selectedFunctions.size() > 7) {
            selectedFunctions = new ArrayList<>(selectedFunctions.subList(0, 7));
        }
        
        // 保存选中状态到SharedPreferences
        saveSelectedFunctions(selectedFunctions);
        
        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra(SELECTED_FUNCTIONS, selectedFunctions);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        
        LogUtil.i(TAG + " 已选择的功能: " + selectedFunctions.toString());
    }

    private void saveSelectedFunctions(ArrayList<String> selectedFunctions) {
        SharedPreferences prefs = getSharedPreferences("QuickActions", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // 清除之前的所有选中状态
        editor.clear();
        
        // 保存新的选中状态
        editor.putInt("count", selectedFunctions.size());
        for (int i = 0; i < selectedFunctions.size(); i++) {
            editor.putString("function_" + i, selectedFunctions.get(i));
        }
        
        editor.apply();
    }

    private List<FunctionItem> getSmartDoorItems() {
        List<FunctionItem> items = new ArrayList<>();
        items.add(new FunctionItem("户户通", R.drawable.ic_huhuotong, true));
        items.add(new FunctionItem("监控", R.drawable.ic_jiankong, true));
        items.add(new FunctionItem("邀请访客", R.drawable.ic_yaoqingfangke, true));
        items.add(new FunctionItem("呼叫电梯", R.drawable.ic_hujiaodianti, true));
        items.add(new FunctionItem("扫码开门", R.drawable.ic_saomamenkai, true));
        items.add(new FunctionItem("呼叫记录", R.drawable.ic_hujiaorecord, false));
        return items;
    }

    private List<FunctionItem> getSmartLifeItems() {
        List<FunctionItem> items = new ArrayList<>();
        items.add(new FunctionItem("社区通知", R.drawable.ic_shequtongzhi, true));
        items.add(new FunctionItem("报警记录", R.drawable.ic_baojingjilu, true));
        items.add(new FunctionItem("报事报修", R.drawable.ic_repair, false));
        items.add(new FunctionItem("社区评价", R.drawable.ic_evaluate, false));
        items.add(new FunctionItem("投诉建议", R.drawable.ic_suggest, false));
        return items;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG + " onDestroy");
    }
} 