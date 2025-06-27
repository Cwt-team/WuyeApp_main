package com.example.wuyeapp.model.shop;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("shop_id")
    private int shopId; // 所属商铺的ID
    @SerializedName("price")
    private double price;
    @SerializedName("category_id")
    private int categoryId; // 商品分类的ID (现在是int类型)
    @SerializedName("image_url")
    private String imageUrl; // 商品图片URL
    @SerializedName("description")
    private String description; // 商品描述
    @SerializedName("stock")
    private int stock;
    @SerializedName("status")
    private int status;

    public Product(int id, String name, int shopId, double price, String category, String imageUrl, String description) {
        this.id = id;
        this.name = name;
        this.shopId = shopId;
        this.price = price;
        this.categoryId = Integer.parseInt(category);
        this.imageUrl = imageUrl;
        this.description = description;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getShopId() {
        return shopId;
    }

    public double getPrice() {
        return price;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public int getStock() {
        return stock;
    }

    public int getStatus() {
        return status;
    }

    // Setters (如果需要修改数据则添加，否则可省略)
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}