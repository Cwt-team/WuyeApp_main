// app/src/main/java/com/example/wuyeapp/model/shop/ShopListResponse.java
package com.example.wuyeapp.model.shop;

import java.util.List;
import com.google.gson.annotations.SerializedName; // 添加这个导入

public class ShopListResponse {
    @SerializedName("items") // 确保字段名与JSON键匹配
    private List<Shop> items;

    @SerializedName("total") // 确保字段名与JSON键匹配
    private int total;

    public List<Shop> getItems() {
        return items;
    }

    public void setItems(List<Shop> items) {
        this.items = items;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}