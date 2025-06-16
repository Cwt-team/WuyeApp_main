package com.example.wuyeapp.ui.shop;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ProductDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        String name = getIntent().getStringExtra("name");
        String shop = getIntent().getStringExtra("shop");
        double price = getIntent().getDoubleExtra("price", 0);
        String category = getIntent().getStringExtra("category");
        TextView title = new TextView(this);
        title.setText("商品详情");
        title.setTextSize(22);
        root.addView(title);
        TextView nameTv = new TextView(this);
        nameTv.setText("名称：" + name);
        nameTv.setTextSize(18);
        root.addView(nameTv);
        TextView shopTv = new TextView(this);
        shopTv.setText("商铺：" + shop);
        root.addView(shopTv);
        TextView priceTv = new TextView(this);
        priceTv.setText("价格：￥" + price);
        root.addView(priceTv);
        TextView catTv = new TextView(this);
        catTv.setText("分类：" + category);
        root.addView(catTv);
        Button orderBtn = new Button(this);
        orderBtn.setText("下单");
        orderBtn.setOnClickListener(v -> Toast.makeText(this, "下单成功：" + name, Toast.LENGTH_SHORT).show());
        root.addView(orderBtn);
        setContentView(root);
    }
} 