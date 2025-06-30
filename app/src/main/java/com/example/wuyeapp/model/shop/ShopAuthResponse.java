package com.example.wuyeapp.model.shop;

import com.google.gson.annotations.SerializedName;

public class ShopAuthResponse {
    @SerializedName("token")
    private String token;
    
    @SerializedName("login_id")
    private String loginId;
    
    @SerializedName("display_name")
    private String displayName;
    
    @SerializedName("user_role")
    private String userRole;
    
    @SerializedName("shop_id")
    private Integer shopId;
    
    @SerializedName("shop_manager_id")
    private Integer shopManagerId;
    
    @SerializedName("avatar_url")
    private String avatarUrl;

    // Getters
    public String getToken() {
        return token;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUserRole() {
        return userRole;
    }

    public Integer getShopId() {
        return shopId;
    }

    public Integer getShopManagerId() {
        return shopManagerId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    // Setters
    public void setToken(String token) {
        this.token = token;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    public void setShopManagerId(Integer shopManagerId) {
        this.shopManagerId = shopManagerId;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
} 