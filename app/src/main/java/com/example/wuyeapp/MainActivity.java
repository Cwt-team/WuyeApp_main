package com.example.wuyeapp;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;

import com.example.wuyeapp.databinding.ActivityMainBinding;
import com.example.wuyeapp.databinding.ItemLivingServiceBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ImageAdapter imageAdapter;
    private ServiceAdapter serviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. 设置头部信息
        binding.appName.setText("你的应用名称"); // 替换为实际的应用名称
        binding.address.setText("当前用户地址");   // 替换为用户的实际地址
        binding.onlineStatus.setText("在线");   // 可以动态设置
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
            Toast.makeText(this, "点击了我的", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到个人中心页面
        });
    }

    private void setupQuickActions() {
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
                            Toast.makeText(this, "点击了用户通", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Toast.makeText(this, "点击了监控", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Toast.makeText(this, "点击了邀请访客", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            Toast.makeText(this, "点击了呼叫电梯", Toast.LENGTH_SHORT).show();
                            break;
                        case 4:
                            Toast.makeText(this, "点击了扫码开门", Toast.LENGTH_SHORT).show();
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
