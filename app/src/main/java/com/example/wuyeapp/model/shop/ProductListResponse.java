package com.example.wuyeapp.model.shop;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 商品列表响应模型，用于解析后端返回的包含商品列表和总数的JSON对象。
 * 后端响应示例：
 * {
 *   "items": [
 *     {
 *       "id": 1,
 *       "name": "商品名称",
 *       // ... 其他商品字段
 *     }
 *   ],
 *   "total": 10
 * }
 */
public class ProductListResponse {

    @SerializedName("items")
    private List<Product> items;

    @SerializedName("total")
    private int total;

    public List<Product> getItems() {
        return items;
    }

    public void setItems(List<Product> items) {
        this.items = items;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "ProductListResponse{" +
               "items=" + (items != null ? items.size() : "null") + " items" +
               ", total=" + total +
               '}';
    }
}
