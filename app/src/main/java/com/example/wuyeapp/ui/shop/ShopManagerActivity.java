package com.example.wuyeapp.ui.shop;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class ShopManagerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        TextView title = new TextView(this);
        title.setText("商铺管理");
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        root.addView(title);
        TextView shop = new TextView(this);
        shop.setText("当前商铺：水果店A");
        shop.setTextSize(18);
        root.addView(shop);
        Button addBtn = new Button(this);
        addBtn.setText("添加商品");
        addBtn.setOnClickListener(v -> Toast.makeText(this, "添加商品功能开发中", Toast.LENGTH_SHORT).show());
        root.addView(addBtn);
        setContentView(root);
    }
} 