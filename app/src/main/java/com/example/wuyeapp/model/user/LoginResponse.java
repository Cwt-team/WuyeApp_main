package com.example.wuyeapp.model.user;

import com.example.wuyeapp.model.base.BaseResponse;
import com.google.gson.annotations.SerializedName;

public class LoginResponse extends BaseResponse {
    @SerializedName("ownerInfo")
    private OwnerInfo ownerInfo;
    
    @SerializedName("token")
    private String token;
    
    @SerializedName("userId")
    private long userId;
    

    

    
    public OwnerInfo getOwnerInfo() {
        return ownerInfo;
    }
    
    public void setOwnerInfo(OwnerInfo ownerInfo) {
        this.ownerInfo = ownerInfo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }




}
