package com.example.wuyeapp.ui.shop;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

public class OrderListActivity extends AppCompatActivity {
    static class Order {
        String product;
        String shop;
        double price;
        String status;
        Order(String product, String shop, double price, String status) {
            this.product = product;
            this.shop = shop;
            this.price = price;
            this.status = status;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        TextView title = new TextView(this);
        title.setText("订单列表");
        title.setTextSize(22);
        root.addView(title);
        List<Order> orders = getOrders();
        for (Order o : orders) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(0, 0, 0, 32);
            TextView p = new TextView(this);
            p.setText("商品：" + o.product);
            TextView s = new TextView(this);
            s.setText("商铺：" + o.shop);
            TextView price = new TextView(this);
            price.setText("价格：￥" + o.price);
            TextView st = new TextView(this);
            st.setText("状态：" + o.status);
            item.addView(p);
            item.addView(s);
            item.addView(price);
            item.addView(st);
            root.addView(item);
        }
        setContentView(root);
    }
    private List<Order> getOrders() {
        List<Order> list = new ArrayList<>();
        list.add(new Order("新鲜苹果", "水果店A", 5.8, "已完成"));
        list.add(new Order("有机牛奶", "便利店B", 12.5, "待支付"));
        list.add(new Order("蛋糕", "烘焙坊D", 28.0, "配送中"));
        return list;
    }
} 