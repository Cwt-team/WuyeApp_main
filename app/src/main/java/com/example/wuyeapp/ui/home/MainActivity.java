package com.example.wuyeapp.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;
import androidx.appcompat.app.AlertDialog;

import com.example.wuyeapp.R;
import com.example.wuyeapp.model.maintenance.Maintenance;
import com.example.wuyeapp.session.SessionManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;

import com.example.wuyeapp.databinding.ActivityMainBinding;
import com.example.wuyeapp.databinding.ItemLivingServiceBinding;
import com.example.wuyeapp.session.SessionManager;
import com.example.wuyeapp.ui.profile.ProfileActivity;
import com.example.wuyeapp.ui.visitor.DialPadActivity;
import com.example.wuyeapp.ui.visitor.InviteVisitorActivity;
import com.example.wuyeapp.ui.visitor.ScanQrActivity;
import com.example.wuyeapp.utils.LogUtil;
import com.example.wuyeapp.ui.maintenance.MaintenanceActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private ImageAdapter imageAdapter;
    private ServiceAdapter serviceAdapter;
    private static final int REQUEST_CODE_MORE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i(TAG + " onCreate");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. 设置头部信息
        binding.appName.setText("我是帅哥111"); // 替换为实际的应用名称
        binding.address.setText("新兴江1号");   // 替换为用户的实际地址
        binding.onlineStatus.setText("隐身");   // 可以动态设置
        binding.onlineStatus.setTextColor(ContextCompat.getColor(this, R.color.green)); // 确保 R.color.green 存在
        binding.headerImage.setImageResource(R.drawable.ic_header_image); // 替换为你的头像资源

        // 2. 设置 ViewPager2 用于图片轮播
        List<Integer> imageList = new ArrayList<>();
        // 使用你自己的图片资源替换
        imageList.add(R.drawable.pic1);
        imageList.add(R.drawable.pic2);

        imageAdapter = new ImageAdapter(this, imageList);
        binding.imageViewpager.setAdapter(imageAdapter);

        // 3. 设置图片指示器
        setupImageIndicators(imageList.size());
        binding.imageViewpager.registerOnPageChangeCallback(new OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateImageIndicators(position, imageList.size());
            }
        });

        // 4. 设置快捷功能按钮的点击事件
        setupQuickActions();

        // 5. 设置通知区域的点击事件
        binding.notificationLayout.setOnClickListener(v -> {
            // 处理点击通知区域的事件，例如跳转到通知列表
            Toast.makeText(this, "跳转到通知列表", Toast.LENGTH_SHORT).show();
        });

        // 6. 设置生活服务 RecyclerView
        List<Service> servicesList = new ArrayList<>();
        servicesList.add(new Service("家政服务平台、保姆月嫂", R.drawable.pic3));
        servicesList.add(new Service("快递服务，查快递", R.drawable.pic4));
        servicesList.add(new Service("本地生活，享生活", R.drawable.pic5));
        servicesList.add(new Service("更多服务", R.drawable.ic_quick_action_more));

        serviceAdapter = new ServiceAdapter(servicesList);
        binding.livingServicesRecyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.livingServicesRecyclerview.setAdapter(serviceAdapter);

        // 7. 设置底部导航栏按钮的点击事件 (示例)
        // 假设你的底部导航栏中有一些按钮，你需要获取它们的引用并设置点击事件
        // 例如：
        // Button button1 = findViewById(R.id.bottom_navigation_button1); // 假设你在 bottom_navigation.xml 中有这些按钮
        // button1.setOnClickListener(v -> { /* 处理按钮1的点击事件 */ });

        binding.navHome.setOnClickListener(v -> {
            // 已经在首页,无需处理
        });

        binding.navUnlock.setOnClickListener(v -> {
            Toast.makeText(this, "点击了开锁", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到开锁页面
        });

        binding.navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
        
        // 设置报事报修按钮的点击事件
        binding.btnRepair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MaintenanceActivity.class);
                startActivity(intent);
            }
        });


    }

    private void setupQuickActions() {
        LogUtil.d(TAG + " 设置快捷操作");
        // 获取 GridLayout 中的所有子 LinearLayout (每个代表一个快捷功能)
        for (int i = 0; i < binding.quickActionsGrid.getChildCount(); i++) {
            View child = binding.quickActionsGrid.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout quickActionLayout = (LinearLayout) child;
                int finalI = i;
                quickActionLayout.setOnClickListener(v -> {
                    // 根据点击的索引 finalI 执行相应的操作
                    switch (finalI) {
                        case 0:
                            // 跳转到户户通界面
                            Intent intent1 = new Intent(this, DialPadActivity.class);
                            startActivity(intent1);
                            break;
                        case 1:
                            Toast.makeText(this, "点击了监控", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Intent intent2 = new Intent(this, InviteVisitorActivity.class);
                            startActivity(intent2);
                            break;
                        case 3:
                            Toast.makeText(this, "点击了呼叫电梯", Toast.LENGTH_SHORT).show();
                            break;
                            case 4:
                            Intent intent = new Intent(this, ScanQrActivity.class);
                            startActivity(intent);
                            break;
                        case 5:
                            Toast.makeText(this, "点击了社区通知", Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(this, "点击了报警记录", Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(this, "点击了更多", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                });
            }
        }

        // 修改"更多"按钮的点击事件
        binding.btnMore.setOnClickListener(v -> {
            Intent intent = new Intent(this, MoreActivity.class);
            startActivityForResult(intent, REQUEST_CODE_MORE);
        });
    }

    private void setupImageIndicators(int count) {
        binding.imageIndicator.removeAllViews();
        int inactiveColor = ContextCompat.getColor(this, R.color.gray); // 在 colors.xml 中定义
        int activeColor = ContextCompat.getColor(this, R.color.green);   // 在 colors.xml 中定义

        for (int i = 0; i < count; i++) {
            ImageView indicator = new ImageView(this);
            indicator.setImageResource(R.drawable.indicator_inactive); // 在 drawable 中定义
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            params.gravity = Gravity.CENTER_VERTICAL;
            indicator.setLayoutParams(params);
            indicator.setColorFilter(inactiveColor);
            binding.imageIndicator.addView(indicator);
        }

        // 设置第一个指示器为激活状态
        if (count > 0) {
            ((ImageView) binding.imageIndicator.getChildAt(0)).setImageResource(R.drawable.indicator_active); // 在 drawable 中定义
            ((ImageView) binding.imageIndicator.getChildAt(0)).setColorFilter(activeColor);
        }
    }

    private void updateImageIndicators(int position, int count) {
        int inactiveColor = ContextCompat.getColor(this, R.color.gray);
        int activeColor = ContextCompat.getColor(this, R.color.green);

        for (int i = 0; i < binding.imageIndicator.getChildCount(); i++) {
            ImageView indicator = (ImageView) binding.imageIndicator.getChildAt(i);
            indicator.setImageResource(i == position ? R.drawable.indicator_active : R.drawable.indicator_inactive);
            indicator.setColorFilter(i == position ? activeColor : inactiveColor);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MORE && resultCode == Activity.RESULT_OK) {
            ArrayList<String> selectedFunctions = data.getStringArrayListExtra(MoreActivity.SELECTED_FUNCTIONS);
            updateQuickActions(selectedFunctions);
        }
    }
    
    private void updateQuickActions(ArrayList<String> selectedFunctions) {
        binding.quickActionsGrid.removeAllViews();
        binding.quickActionsGrid.setColumnCount(4);
        
        // 添加选中的功能
        for (String function : selectedFunctions) {
            addQuickActionView(function);
        }
        
        // 始终添加"更多"按钮
        View moreActionView = LayoutInflater.from(this).inflate(R.layout.item_quick_action, null);
        ImageView moreIcon = moreActionView.findViewById(R.id.function_icon);
        TextView moreName = moreActionView.findViewById(R.id.function_name);
        
        moreIcon.setImageResource(R.drawable.ic_quick_action_more);
        moreName.setText("更多");
        
        androidx.gridlayout.widget.GridLayout.LayoutParams params = 
            new androidx.gridlayout.widget.GridLayout.LayoutParams();
        params.width = 0;
        params.height = androidx.gridlayout.widget.GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = androidx.gridlayout.widget.GridLayout.spec(
            androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f);
        params.rowSpec = androidx.gridlayout.widget.GridLayout.spec(
            androidx.gridlayout.widget.GridLayout.UNDEFINED);
        
        moreActionView.setLayoutParams(params);
        moreActionView.setOnClickListener(v -> startMoreActivity());
        
        binding.quickActionsGrid.addView(moreActionView);
    }
    
    private void startMoreActivity() {
        Intent intent = new Intent(this, MoreActivity.class);
        startActivityForResult(intent, REQUEST_CODE_MORE);
    }

    private void addQuickActionView(String function) {
        View quickActionView = LayoutInflater.from(this).inflate(R.layout.item_quick_action, null);
        ImageView icon = quickActionView.findViewById(R.id.function_icon);
        TextView name = quickActionView.findViewById(R.id.function_name);
        
        name.setText(function);
        icon.setImageResource(getFunctionIcon(function));
        
        androidx.gridlayout.widget.GridLayout.LayoutParams params = 
            new androidx.gridlayout.widget.GridLayout.LayoutParams();
        params.width = 0;
        params.height = androidx.gridlayout.widget.GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = androidx.gridlayout.widget.GridLayout.spec(
            androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f);
        params.rowSpec = androidx.gridlayout.widget.GridLayout.spec(
            androidx.gridlayout.widget.GridLayout.UNDEFINED);
        
        quickActionView.setLayoutParams(params);
        
        // 添加点击事件
        quickActionView.setOnClickListener(v -> {
            if (function.equals("更多")) {
                Intent intent = new Intent(this, MoreActivity.class);
                startActivityForResult(intent, REQUEST_CODE_MORE);
            } else {
                handleQuickActionClick(function);
            }
        });
        
        binding.quickActionsGrid.addView(quickActionView);
    }

    private int getFunctionIcon(String functionName) {
        switch (functionName) {
            case "户户通":
                return R.drawable.ic_huhuotong;
            case "监控":
                return R.drawable.ic_jiankong;
            case "邀请访客":
                return R.drawable.ic_yaoqingfangke;
            case "呼叫电梯":
                return R.drawable.ic_hujiaodianti;
            case "扫码开门":
                return R.drawable.ic_saomamenkai;
            case "社区通知":
                return R.drawable.ic_shequtongzhi;
            case "报警记录":
                return R.drawable.ic_baojingjilu;
            case "更多":
                return R.drawable.ic_quick_action_more;
            case "呼叫记录":
                return R.drawable.ic_hujiaorecord;
            case "报事报修":
                return R.drawable.ic_repair;
            case "社区评价":
                return R.drawable.ic_evaluate;
            case "投诉建议":
                return R.drawable.ic_suggest;
            default:
                return R.drawable.ic_quick_action_more;
        }
    }

    private void handleQuickActionClick(String functionName) {
        LogUtil.i(TAG + " 点击功能: " + functionName);
        Intent intent;
        switch (functionName) {
            case "户户通":
                intent = new Intent(this, DialPadActivity.class);
                startActivity(intent);
                break;
            case "监控":
                Toast.makeText(this, "启动监控", Toast.LENGTH_SHORT).show();
                break;
            case "邀请访客":
                intent = new Intent(this, InviteVisitorActivity.class);
                startActivity(intent);
                break;
            case "呼叫电梯":
                Toast.makeText(this, "启动呼叫电梯", Toast.LENGTH_SHORT).show();
                break;
            case "扫码开门":
                intent = new Intent(this, ScanQrActivity.class);
                startActivity(intent);
                break;
            case "社区通知":
                Toast.makeText(this, "启动社区通知", Toast.LENGTH_SHORT).show();
                break;
            case "报警记录":
                Toast.makeText(this, "启动报警记录", Toast.LENGTH_SHORT).show();
                break;
            case "报事报修":
                intent = new Intent(this, MaintenanceActivity.class);
                startActivity(intent);
                break;
            default:
                Toast.makeText(this, "功能开发中...", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.i(TAG + " onResume");
        loadAndUpdateQuickActions();
    }

    private void loadAndUpdateQuickActions() {
        SharedPreferences prefs = getSharedPreferences("QuickActions", MODE_PRIVATE);
        ArrayList<String> selectedFunctions = new ArrayList<>();
        int count = prefs.getInt("count", 0);
        
        if (count == 0) {
            // 如果没有保存的状态，使用默认的快捷功能
            selectedFunctions.add("户户通");
            selectedFunctions.add("监控");
            selectedFunctions.add("邀请访客");
            selectedFunctions.add("呼叫电梯");
            selectedFunctions.add("扫码开门");
            selectedFunctions.add("社区通知");
            selectedFunctions.add("报警记录");
        } else {
            // 加载保存的快捷功能
            for (int i = 0; i < count; i++) {
                selectedFunctions.add(prefs.getString("function_" + i, ""));
            }
        }
        
        updateQuickActions(selectedFunctions);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.i(TAG + " onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG + " onDestroy");
    }
}

class Service {
    private String name;
    private int imageResId;

    public Service(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }
}

class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final MainActivity context;
    private final List<Integer> imageList;

    public ImageAdapter(MainActivity context, List<Integer> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // 或其他合适的缩放类型
        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.imageView.setImageResource(imageList.get(position));
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ImageViewHolder(@NonNull ImageView itemView) {
            super(itemView);
            imageView = itemView;
        }
    }
}

class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {
    private final List<Service> serviceList;

    public ServiceAdapter(List<Service> serviceList) {
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLivingServiceBinding binding = ItemLivingServiceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ServiceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);
        holder.binding.serviceTitle.setText(service.getName());
        holder.binding.serviceImage.setImageResource(service.getImageResId());
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        public ItemLivingServiceBinding binding;

        public ServiceViewHolder(@NonNull ItemLivingServiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
