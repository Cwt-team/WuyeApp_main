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
import com.example.wuyeapp.network.api.ShopApiService;
import com.example.wuyeapp.network.client.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import com.example.wuyeapp.utils.LogUtil;
import com.example.wuyeapp.model.shop.ShopListResponse;

public class ShopActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shopApiService = RetrofitClient.getShopApiService();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, 0, 0, 0);
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(24, 24, 24, 24);
        topBar.setBackgroundColor(0xFFFFFFFF);
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
        switchShopBtn.setBackground(btnBg);
        switchShopBtn.setPadding(32,8,32,8);
        switchShopBtn.setOnClickListener(v -> showShopSwitchDialog());
        Button orderBtn = new Button(this);
        orderBtn.setText("订单");
        orderBtn.setTextColor(0xFFFFFFFF);
        orderBtn.setBackground(btnBg);
        orderBtn.setPadding(32,8,32,8);
        orderBtn.setOnClickListener(v -> startActivity(new Intent(this, OrderListActivity.class)));
        topBar.addView(shopTitle);
        topBar.addView(switchShopBtn);
        topBar.addView(orderBtn);
        root.addView(topBar);
        LinearLayout mainArea = new LinearLayout(this);
        mainArea.setOrientation(LinearLayout.HORIZONTAL);
        categoryListLayout = new LinearLayout(this);
        categoryListLayout.setOrientation(LinearLayout.VERTICAL);
        categoryListLayout.setPadding(8, 8, 8, 8);
        categoryListLayout.setBackgroundColor(0xFFF7F7F7);
        updateCategoryList();
        ScrollView productScroll = new ScrollView(this);
        productListLayout = new LinearLayout(this);
        productListLayout.setOrientation(LinearLayout.VERTICAL);
        productListLayout.setPadding(24, 16, 24, 16);
        productScroll.addView(productListLayout);
        updateProductList();
        mainArea.addView(categoryListLayout, new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.MATCH_PARENT));
        mainArea.addView(productScroll, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        root.addView(mainArea, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);
        fetchShopsAndProducts();
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
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 24);
            btn.setLayoutParams(lp);
            categoryListLayout.addView(btn);
        }
    }

    private void updateProductList() {
        productListLayout.removeAllViews();
        for (Product p : allProducts) {
            String productCategoryIdString = String.valueOf(p.getCategoryId());
            if ((!currentCategory.equals("全部") && !productCategoryIdString.equals(currentCategory)) || (currentShopId != -1 && p.getShopId() != currentShopId)) continue;
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setPadding(24, 24, 24, 24);
            GradientDrawable cardBg = new GradientDrawable();
            cardBg.setCornerRadius(36);
            cardBg.setColor(0xFFFFFFFF);
            card.setBackground(cardBg);
            card.setElevation(12f);
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLp.setMargins(0, 0, 0, 36);
            card.setLayoutParams(cardLp);
            ImageView img = new ImageView(this);
            if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
                img.setImageResource(android.R.drawable.ic_menu_gallery);
            } else {
                img.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(160, 160);
            imgLp.setMargins(0, 0, 32, 0);
            img.setLayoutParams(imgLp);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            card.addView(img);
            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            TextView name = new TextView(this);
            name.setText(p.getName());
            name.setTextSize(20);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            name.setPadding(0, 0, 0, 8);
            info.addView(name);
            TextView price = new TextView(this);
            price.setText("￥" + p.getPrice());
            price.setTextSize(18);
            price.setTextColor(0xFFFF9800);
            price.setPadding(0, 0, 0, 16);
            info.addView(price);
            Button buy = new Button(this);
            buy.setText("购买");
            GradientDrawable buyBg = new GradientDrawable();
            buyBg.setCornerRadius(32);
            buyBg.setColor(0xFF009688);
            buy.setBackground(buyBg);
            buy.setTextColor(0xFFFFFFFF);
            buy.setTextSize(16);
            buy.setPadding(48, 16, 48, 16);
            buy.setOnClickListener(v -> {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(buy, "scaleX", 1f, 0.92f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(buy, "scaleY", 1f, 0.92f, 1f);
                AnimatorSet set = new AnimatorSet();
                set.playTogether(scaleX, scaleY);
                set.setDuration(180);
                set.setInterpolator(new AccelerateDecelerateInterpolator());
                set.start();
                Toast.makeText(this, "已下单：" + p.getName(), Toast.LENGTH_SHORT).show();
            });
            info.addView(buy);
            card.addView(info);
            card.setOnClickListener(v -> showProductDetail(p));
            productListLayout.addView(card);
        }
    }

    private void showShopSwitchDialog() {
        if (shops.isEmpty()) {
            Toast.makeText(this, "商铺列表正在加载，请稍候。", Toast.LENGTH_SHORT).show();
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("切换商铺");
        String[] shopNames = new String[shops.size()];
        for (int i = 0; i < shops.size(); i++) {
            shopNames[i] = shops.get(i).getName();
        }

        builder.setItems(shopNames, (dialog, which) -> {
            currentShopName = shopNames[which];
            currentShopId = shops.get(which).getId();
            shopTitle.setText("当前商铺：" + currentShopName);
            updateProductList();
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

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

    private void fetchShopsAndProducts() {
        Toast.makeText(this, "正在加载商铺和商品信息...", Toast.LENGTH_SHORT).show();

        shopApiService.getAllShops().enqueue(new Callback<ShopListResponse>() {
            @Override
            public void onResponse(Call<ShopListResponse> call, Response<ShopListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ShopListResponse shopListResponse = response.body();
                    shops = shopListResponse.getItems();

                    if (!shops.isEmpty()) {
                        currentShopId = shops.get(0).getId();
                        currentShopName = shops.get(0).getName();
                        shopTitle.setText("当前商铺：" + currentShopName);
                        fetchProducts();
                    } else {
                        Toast.makeText(ShopActivity.this, "没有商铺信息。", Toast.LENGTH_SHORT).show();
                        LogUtil.w("获取到商铺列表但为空。");
                        updateProductList();
                    }
                } else {
                    Toast.makeText(ShopActivity.this, "获取商铺列表失败：" + response.code(), Toast.LENGTH_SHORT).show();
                    LogUtil.e("获取商铺列表失败，HTTP状态码: " + response.code() + ", 错误信息: " + response.message());
                    allProducts.clear();
                    updateProductList();
                }
            }

            @Override
            public void onFailure(Call<ShopListResponse> call, Throwable t) {
                Toast.makeText(ShopActivity.this, "网络连接或服务器错误：商铺", Toast.LENGTH_SHORT).show();
                LogUtil.e("获取商铺列表请求失败：", t);
                allProducts.clear();
                updateProductList();
            }
        });
    }

    private void fetchProducts() {
        if (currentShopId == -1) {
            LogUtil.w("没有有效的商铺ID，无法获取商品列表。");
            allProducts.clear();
            updateProductList();
            Toast.makeText(this, "没有选择商铺，无法加载商品。", Toast.LENGTH_SHORT).show();
            return;
        }

        LogUtil.i("正在获取商铺 (ID: " + currentShopId + ") 的商品信息...");

        shopApiService.getProductsByShopIdWithQuery(currentShopId).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allProducts = response.body();
                    LogUtil.i("成功获取到商品列表，数量: " + allProducts.size());

                    extractCategoriesFromProducts(allProducts);
                    updateCategoryList();
                    updateProductList();
                    if (allProducts.isEmpty()) {
                        Toast.makeText(ShopActivity.this, "该商铺没有商品信息。", Toast.LENGTH_SHORT).show();
                        LogUtil.w("该商铺 (ID: " + currentShopId + ") 没有商品信息。");
                    }
                } else {
                    Toast.makeText(ShopActivity.this, "获取商品列表失败：" + response.code(), Toast.LENGTH_SHORT).show();
                    LogUtil.e("获取商品列表失败，HTTP状态码: " + response.code() + ", 错误信息: " + response.message());
                    allProducts.clear();
                    updateProductList();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(ShopActivity.this, "网络连接或服务器错误：商品", Toast.LENGTH_SHORT).show();
                LogUtil.e("获取商品列表请求失败：", t);
                allProducts.clear();
                updateProductList();
            }
        });
    }

    private void extractCategoriesFromProducts(List<Product> products) {
        categories.clear();
        categories.add("全部");
        for (Product product : products) {
            String categoryIdString = String.valueOf(product.getCategoryId());
            if (categoryIdString != null && !categoryIdString.trim().isEmpty()) {
                if (!categories.contains(categoryIdString)) {
                    categories.add(categoryIdString);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
} 