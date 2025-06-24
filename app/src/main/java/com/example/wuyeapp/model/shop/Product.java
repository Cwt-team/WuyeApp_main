package com.example.wuyeapp.model.shop;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("shopId")
    private int shopId; // 所属商铺的ID
    @SerializedName("price")
    private double price;
    @SerializedName("category")
    private String category;
    @SerializedName("imageUrl")
    private String imageUrl; // 商品图片URL
    @SerializedName("description")
    private String description; // 商品描述

    public Product(int id, String name, int shopId, double price, String category, String imageUrl, String description) {
        this.id = id;
        this.name = name;
        this.shopId = shopId;
        this.price = price;
        this.category = category;
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

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
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

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}