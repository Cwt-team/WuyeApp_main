// app/src/main/java/com/example/wuyeapp/model/shop/ShopListResponse.java
package com.example.wuyeapp.model.shop;

import java.util.List;
import com.google.gson.annotations.SerializedName; // 添加这个导入

public class ShopListResponse {
    @SerializedName("total")
    private int total;

    @SerializedName("current_page")
    private int currentPage;

    @SerializedName("page_size")
    private int pageSize;

    @SerializedName("items")
    private List<Shop> items;

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

    public List<Shop> getItems() {
        return items;
    }

    public void setItems(List<Shop> items) {
        this.items = items;
    }
}