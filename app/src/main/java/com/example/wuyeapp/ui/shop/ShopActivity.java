package com.example.wuyeapp.ui.shop;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.view.Gravity;
import android.view.View;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.widget.ImageView;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.example.wuyeapp.R;
import com.example.wuyeapp.model.shop.Product;
import com.example.wuyeapp.model.shop.Shop;
import com.example.wuyeapp.model.shop.ShopAuthResponse;
import com.example.wuyeapp.network.api.ShopApiService;
import com.example.wuyeapp.network.api.ShopAuthApiService;
import com.example.wuyeapp.network.client.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import com.example.wuyeapp.utils.LogUtil;
import com.example.wuyeapp.model.shop.ShopListResponse;
import com.example.wuyeapp.model.shop.ProductListResponse;
import java.io.IOException;
import com.example.wuyeapp.session.SessionManager;
import com.example.wuyeapp.ui.auth.LoginActivity;
import com.example.wuyeapp.model.shop.Category;
import com.example.wuyeapp.model.ApiResponse;
import android.app.AlertDialog;

public class ShopActivity extends AppCompatActivity {
    private static final String TAG = "ShopActivity";
    private String currentShopName = "默认商铺";
    private int currentShopId = -1;
    private String currentCategory = "全部";
    private List<String> categories = new ArrayList<>();
    private List<Shop> shops = new ArrayList<>();
    private List<Product> allProducts = new ArrayList<>();
    private LinearLayout productListLayout;
    private LinearLayout categoryListLayout;
    private TextView shopTitle;
    private ShopApiService shopApiService;
    private SessionManager sessionManager;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = SessionManager.getInstance(this);
        shopApiService = RetrofitClient.getInstance().getShopApiService();

        // 检查商城登录状态
        if (!checkShopAuthentication()) {
            return;
        }

        setupUI();
        fetchShopsAndProducts();
    }

    private boolean checkShopAuthentication() {
        if (!sessionManager.isShopLoggedIn()) {
            LogUtil.i(TAG, "用户未登录商城系统或登录已过期，跳转到登录界面");
            redirectToShopLogin();
            return false;
        }

        if (sessionManager.isShopTokenNearExpiry()) {
            LogUtil.i(TAG, "商城token即将过期，尝试刷新");
            refreshShopToken();
        }

        return true;
    }

    private void redirectToShopLogin() {
        Intent intent = new Intent(this, ShopLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void refreshShopToken() {
        String currentToken = sessionManager.getShopToken();
        RetrofitClient.getInstance().getShopAuthApiService()
            .refreshToken("Bearer " + currentToken)
            .enqueue(new Callback<ShopAuthResponse>() {
                @Override
                public void onResponse(Call<ShopAuthResponse> call, Response<ShopAuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        sessionManager.createShopLoginSession(response.body());
                        RetrofitClient.getInstance().setAuthToken(response.body().getToken());
                        LogUtil.i(TAG, "商城token刷新成功");
                    } else {
                        LogUtil.e(TAG, "商城token刷新失败，需要重新登录");
                        redirectToShopLogin();
                    }
                }

                @Override
                public void onFailure(Call<ShopAuthResponse> call, Throwable t) {
                    LogUtil.e(TAG, "商城token刷新请求失败", t);
                    redirectToShopLogin();
                }
            });
    }

    private void setupUI() {
        // 初始化成员变量
        productListLayout = new LinearLayout(this);
        emptyView = new LinearLayout(this);
        
        // 创建根布局
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, 0, 0, 0);

        // 设置空视图
        TextView emptyText = new TextView(this);
        emptyText.setText("暂无商品");
        emptyText.setTextSize(18);
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setTextColor(0xFF666666);
        ((LinearLayout) emptyView).setGravity(Gravity.CENTER);
        ((LinearLayout) emptyView).addView(emptyText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        emptyView.setVisibility(View.GONE);

        // 顶部栏
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(24, 24, 24, 24);
        topBar.setBackgroundColor(0xFFFFFFFF);

        // 圆角主色按钮样式
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(0xFF009688);
        btnBg.setCornerRadius(32);

        shopTitle = new TextView(this);
        shopTitle.setText("当前商铺：" + currentShopName);
        shopTitle.setTextSize(20);
        shopTitle.setTypeface(Typeface.DEFAULT_BOLD);
        shopTitle.setPadding(0,0,24,0);

        Button switchShopBtn = new Button(this);
        switchShopBtn.setText("切换商铺");
        switchShopBtn.setTextColor(0xFFFFFFFF);
        switchShopBtn.setBackground(btnBg.getConstantState().newDrawable());
        switchShopBtn.setPadding(32,8,32,8);
        switchShopBtn.setOnClickListener(v -> showShopSwitchDialog());

        Button orderBtn = new Button(this);
        orderBtn.setText("订单");
        orderBtn.setTextColor(0xFFFFFFFF);
        orderBtn.setBackground(btnBg.getConstantState().newDrawable());
        orderBtn.setPadding(32,8,32,8);
        orderBtn.setOnClickListener(v -> startActivity(new Intent(this, OrderListActivity.class)));

        topBar.addView(shopTitle);
        topBar.addView(switchShopBtn);
        topBar.addView(orderBtn);
        root.addView(topBar);

        // 主体区域
        LinearLayout mainArea = new LinearLayout(this);
        mainArea.setOrientation(LinearLayout.HORIZONTAL);

        // 左侧分类栏
        categoryListLayout = new LinearLayout(this);
        categoryListLayout.setOrientation(LinearLayout.VERTICAL);
        categoryListLayout.setPadding(8, 8, 8, 8);
        categoryListLayout.setBackgroundColor(0xFFF7F7F7);

        // 右侧商品列表
        ScrollView productScroll = new ScrollView(this);
        productListLayout.setOrientation(LinearLayout.VERTICAL);
        productListLayout.setPadding(24, 16, 24, 16);
        productScroll.addView(productListLayout);

        // 左右分栏布局
        mainArea.addView(categoryListLayout, new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.MATCH_PARENT));
        mainArea.addView(productScroll, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        // 添加主体区域到根布局
        root.addView(mainArea, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        
        // 添加空视图到根布局
        root.addView(emptyView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        // 设置内容视图
        setContentView(root);

        // 初始化分类列表
        updateCategoryList();
    }

    private void updateCategoryList() {
        categoryListLayout.removeAllViews();
        for (String cat : categories) {
            Button btn = new Button(this);
            btn.setText(cat != null ? cat : "未知分类");
            btn.setAllCaps(false);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(32);
            if (cat != null && cat.equals(currentCategory)) {
                bg.setColor(0xFF009688);
                btn.setTextColor(0xFFFFFFFF);
            } else {
                bg.setColor(0xFFFFFFFF);
                btn.setTextColor(0xFF333333);
            }
            btn.setBackground(bg);
            btn.setTextSize(16);
            btn.setPadding(0, 24, 0, 24);
            btn.setOnClickListener(v -> {
                currentCategory = (cat != null) ? cat : "全部";
                updateCategoryList();
                updateProductList();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 24);
            btn.setLayoutParams(lp);
            categoryListLayout.addView(btn);
        }
    }

    private void updateProductList() {
        productListLayout.removeAllViews();
        
        if (allProducts == null || allProducts.isEmpty()) {
            LogUtil.d(TAG, "updateProductList: allProducts 为空或没有商品，不进行更新");
            showEmptyView();
            return;
        }

        LogUtil.d(TAG, "updateProductList: 开始更新商品列表");
        LogUtil.d(TAG, "当前筛选条件 - 分类: " + currentCategory);
        LogUtil.d(TAG, "当前筛选条件 - 商铺ID: " + currentShopId);
        LogUtil.d(TAG, "商品总数: " + allProducts.size());

        int displayedCount = 0;
        for (Product product : allProducts) {
            String categoryIdString = String.valueOf(product.getCategoryId());
            
            LogUtil.d(TAG, "处理商品: " + product.getName());
            LogUtil.d(TAG, "商品信息 - ID: " + product.getId());
            LogUtil.d(TAG, "商品信息 - 分类ID: " + product.getCategoryId());
            LogUtil.d(TAG, "商品信息 - 商铺ID: " + product.getShopId());

            // 检查是否需要显示该商品
            boolean shouldShow = true;
            if (!currentCategory.equals("全部") && !categoryIdString.equals(currentCategory)) {
                LogUtil.d(TAG, "商品 " + product.getName() + " 因分类不匹配而被过滤 (期望: " + currentCategory + ", 实际: " + categoryIdString + ")");
                shouldShow = false;
            }
            if (currentShopId != -1 && product.getShopId() != currentShopId) {
                LogUtil.d(TAG, "商品 " + product.getName() + " 因商铺不匹配而被过滤 (期望: " + currentShopId + ", 实际: " + product.getShopId() + ")");
                shouldShow = false;
            }

            if (shouldShow) {
                // 添加商品视图
                addProductView(product);
                displayedCount++;
            }
        }

        LogUtil.d(TAG, "updateProductList: 实际显示商品数量: " + displayedCount);
        
        if (displayedCount == 0) {
            LogUtil.d(TAG, "updateProductList: 筛选后没有商品可显示");
            showEmptyView();
        } else {
            hideEmptyView();
        }
    }

    private void showShopSwitchDialog() {
        if (shops == null || shops.isEmpty()) {
            Toast.makeText(this, "暂无可用商铺", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] shopNames = new String[shops.size()];
        for (int i = 0; i < shops.size(); i++) {
            Shop shop = shops.get(i);
            shopNames[i] = shop.getName() + " (" + shop.getAddress() + ")";
        }

        new AlertDialog.Builder(this)
            .setTitle("选择商铺")
            .setSingleChoiceItems(shopNames, getCurrentShopIndex(), (dialog, which) -> {
                Shop selectedShop = shops.get(which);
                currentShopId = selectedShop.getId();
                currentShopName = selectedShop.getName();
                shopTitle.setText("当前商铺：" + currentShopName);
                
                // 切换商铺后重新获取商品
                fetchProducts();
                dialog.dismiss();
                
                // 提示用户
                Toast.makeText(this, "已切换到: " + currentShopName, Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private int getCurrentShopIndex() {
        for (int i = 0; i < shops.size(); i++) {
            if (shops.get(i).getId() == currentShopId) {
                return i;
            }
        }
        return 0;
    }

    private void fetchShopsAndProducts() {
        Toast.makeText(this, "正在加载商铺和商品信息...", Toast.LENGTH_SHORT).show();

        // 获取商铺列表
        shopApiService.getShops(1, 50, null).enqueue(new Callback<ApiResponse<ShopListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ShopListResponse>> call, 
                                 Response<ApiResponse<ShopListResponse>> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    String errorMsg = "获取商铺列表失败";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    LogUtil.e(TAG, errorMsg);
                    showError(errorMsg);
                    return;
                }

                ShopListResponse shopListResponse = response.body().getData();
                if (shopListResponse == null || shopListResponse.getItems() == null) {
                    LogUtil.e(TAG, "商铺列表数据为空");
                    showError("获取商铺列表失败: 数据为空");
                    return;
                }

                shops = shopListResponse.getItems();
                LogUtil.i(TAG, "成功获取到商铺列表，数量: " + shops.size());

                if (!shops.isEmpty()) {
                    // 默认选择第一个商铺
                    currentShopId = shops.get(0).getId();
                    currentShopName = shops.get(0).getName();
                    shopTitle.setText("当前商铺：" + currentShopName);

                    
                    // 获取选中商铺的商品
                    fetchProducts();
                } else {
                    LogUtil.w(TAG, "没有可用的商铺");
                    Toast.makeText(ShopActivity.this, "暂无商铺信息", Toast.LENGTH_SHORT).show();
                    showEmptyView();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ShopListResponse>> call, Throwable t) {
                String errorMsg = "网络连接失败，无法获取商铺列表: " + t.getMessage();
                LogUtil.e(TAG, errorMsg, t);
                showError(errorMsg);
            }
        });
    }

    // 设置商铺切换器


    private void showProductDetail(Product p) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("productId", p.getId());
        intent.putExtra("productName", p.getName());
        intent.putExtra("productShopId", p.getShopId());
        intent.putExtra("productPrice", p.getPrice());
        intent.putExtra("productCategoryId", p.getCategoryId());
        intent.putExtra("productImageUrl", p.getImageUrl());
        intent.putExtra("productDescription", p.getDescription());
        startActivity(intent);
    }

    private void fetchProducts() {
        if (currentShopId == -1) {
            LogUtil.w(TAG, "没有选择商铺，无法获取商品列表");
            return;
        }

        LogUtil.d(TAG, "正在获取商铺ID: " + currentShopId + " 的商品列表");
        
        shopApiService.getProducts(1, 50, null, null, currentShopId)
            .enqueue(new Callback<ApiResponse<ProductListResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<ProductListResponse>> call, 
                                    Response<ApiResponse<ProductListResponse>> response) {
                    if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                        String errorMsg = "获取商品列表失败";
                        if (response.body() != null) {
                            errorMsg = response.body().getMessage();
                        }
                        LogUtil.e(TAG, errorMsg);
                        showError(errorMsg);
                        return;
                    }

                    ProductListResponse productResponse = response.body().getData();
                    if (productResponse == null || productResponse.getItems() == null) {
                        LogUtil.w(TAG, "商铺 " + currentShopId + " 暂无商品");
                        allProducts.clear();
                        showEmptyView();
                        return;
                    }

                    allProducts = productResponse.getItems();
                    LogUtil.i(TAG, "成功获取到商品列表，数量: " + allProducts.size());

                    if (!allProducts.isEmpty()) {
                        hideEmptyView();
                        updateProductList();
                        // 获取分类信息
                        fetchCategories();
                    } else {
                        showEmptyView();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<ProductListResponse>> call, Throwable t) {
                    String errorMsg = "网络连接失败，无法获取商品列表: " + t.getMessage();
                    LogUtil.e(TAG, errorMsg, t);
                    showError(errorMsg);
                }
            });
    }

    private void fetchCategories() {
        shopApiService.getCategories(currentShopId)
            .enqueue(new Callback<ApiResponse<List<Category>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Category>>> call,
                                    Response<ApiResponse<List<Category>>> response) {
                    if (response.isSuccessful() && response.body() != null && 
                        response.body().isSuccess()) {
                        List<Category> categoryList = response.body().getData();
                        categories.clear();
                        categories.add("全部");
                        for (Category category : categoryList) {
                            categories.add(category.getName());
                        }
                        updateCategoryList();
                    } else {
                        LogUtil.e(TAG, "获取分类列表失败: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                    LogUtil.e(TAG, "获取分类列表失败", t);
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkShopAuthentication()) {
            return;
        }
    }

    private void showEmptyView() {
        LogUtil.d(TAG, "显示空状态视图");
        emptyView.setVisibility(View.VISIBLE);
        productListLayout.setVisibility(View.GONE);
    }

    private void hideEmptyView() {
        LogUtil.d(TAG, "隐藏空状态视图");
        emptyView.setVisibility(View.GONE);
        productListLayout.setVisibility(View.VISIBLE);
    }

    private void addProductView(Product product) {
        LogUtil.d(TAG, "添加商品视图: " + product.getName());
        
        // 创建商品卡片布局
        LinearLayout productCard = new LinearLayout(this);
        productCard.setOrientation(LinearLayout.HORIZONTAL);
        productCard.setPadding(16, 16, 16, 16);
        productCard.setBackgroundColor(0xFFFFFFFF);
        
        // 设置卡片阴影和圆角
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(0xFFFFFFFF);
        shape.setCornerRadius(8);
        productCard.setBackground(shape);
        productCard.setElevation(4);
        

        
        // 商品信息容器
        LinearLayout infoContainer = new LinearLayout(this);
        infoContainer.setOrientation(LinearLayout.VERTICAL);
        infoContainer.setPadding(16, 0, 0, 0);
        
        // 商品名称
        TextView nameText = new TextView(this);
        nameText.setText(product.getName());
        nameText.setTextSize(18);
        nameText.setTypeface(Typeface.DEFAULT_BOLD);
        infoContainer.addView(nameText);
        
        // 商品价格
        TextView priceText = new TextView(this);
        priceText.setText("¥" + String.format("%.2f", product.getPrice()));
        priceText.setTextSize(16);
        priceText.setTextColor(0xFFE91E63);
        infoContainer.addView(priceText);
        
        // 商品描述
        TextView descText = new TextView(this);
        descText.setText(product.getDescription());
        descText.setTextSize(14);
        descText.setTextColor(0xFF666666);
        descText.setMaxLines(2);
        infoContainer.addView(descText);
        
        productCard.addView(infoContainer, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        // 添加点击事件
        productCard.setOnClickListener(v -> showProductDetail(product));
        
        // 添加到商品列表布局
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        productListLayout.addView(productCard, cardParams);
        
        // 添加动画效果
        productCard.setAlpha(0f);
        productCard.animate()
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(ShopActivity.this, message, Toast.LENGTH_SHORT).show();
            // 可以在这里添加错误UI显示
        });
    }
} 