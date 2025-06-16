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

public class ShopActivity extends AppCompatActivity {
    static class Product {
        String name;
        String shop;
        double price;
        String category;
        int imageResId;
        Product(String name, String shop, double price, String category, int imageResId) {
            this.name = name;
            this.shop = shop;
            this.price = price;
            this.category = category;
            this.imageResId = imageResId;
        }
    }

    static class Shop {
        String name;
        Shop(String name) { this.name = name; }
    }

    private String currentShop = "水果店A";
    private String currentCategory = "全部";
    private final List<String> categories = new ArrayList<String>() {{
        add("全部"); add("水果"); add("饮品"); add("日用品"); add("烘焙");
    }};
    private final List<Shop> shops = new ArrayList<Shop>() {{
        add(new Shop("水果店A")); add(new Shop("便利店B")); add(new Shop("日用品C")); add(new Shop("烘焙坊D"));
    }};
    private List<Product> allProducts;
    private LinearLayout productListLayout;
    private LinearLayout categoryListLayout;
    private TextView shopTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allProducts = getProducts();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, 0, 0, 0);
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
        shopTitle.setText("当前商铺：" + currentShop);
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
        // 主体区域
        LinearLayout mainArea = new LinearLayout(this);
        mainArea.setOrientation(LinearLayout.HORIZONTAL);
        // 左侧分类栏
        categoryListLayout = new LinearLayout(this);
        categoryListLayout.setOrientation(LinearLayout.VERTICAL);
        categoryListLayout.setPadding(8, 8, 8, 8);
        categoryListLayout.setBackgroundColor(0xFFF7F7F7);
        updateCategoryList();
        // 右侧商品列表
        ScrollView productScroll = new ScrollView(this);
        productListLayout = new LinearLayout(this);
        productListLayout.setOrientation(LinearLayout.VERTICAL);
        productListLayout.setPadding(24, 16, 24, 16);
        productScroll.addView(productListLayout);
        updateProductList();
        // 左右分栏布局
        mainArea.addView(categoryListLayout, new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.MATCH_PARENT));
        mainArea.addView(productScroll, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        root.addView(mainArea, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);
    }

    private void updateCategoryList() {
        categoryListLayout.removeAllViews();
        for (String cat : categories) {
            Button btn = new Button(this);
            btn.setText(cat);
            btn.setAllCaps(false);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(32);
            if (cat.equals(currentCategory)) {
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
                currentCategory = cat;
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
            if ((!currentCategory.equals("全部") && !p.category.equals(currentCategory)) || !p.shop.equals(currentShop)) continue;
            // 商品卡片外层
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
            // 商品图片
            ImageView img = new ImageView(this);
            if (p.imageResId != 0) {
                img.setImageResource(p.imageResId);
            } else {
                img.setImageResource(android.R.drawable.ic_menu_gallery); // 用系统自带图标模拟
            }
            LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(160, 160);
            imgLp.setMargins(0, 0, 32, 0);
            img.setLayoutParams(imgLp);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            card.addView(img);
            // 右侧信息区
            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            // 商品名
            TextView name = new TextView(this);
            name.setText(p.name);
            name.setTextSize(20);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            name.setPadding(0, 0, 0, 8);
            info.addView(name);
            // 价格
            TextView price = new TextView(this);
            price.setText("￥" + p.price);
            price.setTextSize(18);
            price.setTextColor(0xFFFF9800);
            price.setPadding(0, 0, 0, 16);
            info.addView(price);
            // 购买按钮
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
                // 缩放动画
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(buy, "scaleX", 1f, 0.92f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(buy, "scaleY", 1f, 0.92f, 1f);
                AnimatorSet set = new AnimatorSet();
                set.playTogether(scaleX, scaleY);
                set.setDuration(180);
                set.setInterpolator(new AccelerateDecelerateInterpolator());
                set.start();
                Toast.makeText(this, "已下单：" + p.name, Toast.LENGTH_SHORT).show();
            });
            info.addView(buy);
            card.addView(info);
            card.setOnClickListener(v -> showProductDetail(p));
            productListLayout.addView(card);
        }
    }

    private void showShopSwitchDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("切换商铺");
        String[] shopNames = new String[shops.size()];
        for (int i = 0; i < shops.size(); i++) shopNames[i] = shops.get(i).name;
        builder.setItems(shopNames, (dialog, which) -> {
            currentShop = shopNames[which];
            shopTitle.setText("当前商铺：" + currentShop);
            updateProductList();
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showProductDetail(Product p) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("name", p.name);
        intent.putExtra("shop", p.shop);
        intent.putExtra("price", p.price);
        intent.putExtra("category", p.category);
        startActivity(intent);
    }

    private List<Product> getProducts() {
        List<Product> list = new ArrayList<>();
        int[] imgs;
        try {
            imgs = new int[]{R.drawable.pic1, R.drawable.pic2, R.drawable.pic3, R.drawable.pic4, R.drawable.pic5};
        } catch (Throwable e) {
            // R 不存在时用0占位
            imgs = new int[]{0,0,0,0,0};
        }
        list.add(new Product("新鲜苹果", "水果店A", 5.8, "水果", imgs[0]));
        list.add(new Product("香蕉", "水果店A", 3.2, "水果", imgs[1]));
        list.add(new Product("有机牛奶", "便利店B", 12.5, "饮品", imgs[2]));
        list.add(new Product("矿泉水", "便利店B", 2.0, "饮品", imgs[3]));
        list.add(new Product("洗衣液", "日用品C", 18.0, "日用品", imgs[4]));
        list.add(new Product("面包", "烘焙坊D", 8.0, "烘焙", imgs[0]));
        list.add(new Product("蛋糕", "烘焙坊D", 28.0, "烘焙", imgs[1]));
        return list;
    }
} 