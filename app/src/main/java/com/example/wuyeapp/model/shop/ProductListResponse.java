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

    @SerializedName("total")
    private int total;

    @SerializedName("current_page")
    private int currentPage;

    @SerializedName("page_size")
    private int pageSize;

    @SerializedName("items")
    private List<Product> items;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<Product> getItems() {
        return items;
    }

    public void setItems(List<Product> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ProductListResponse{" +
                "total=" + total +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", items=" + (items != null ? items.size() : "null") + " items" +
                '}';
    }
}
