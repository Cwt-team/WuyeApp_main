package com.example.wuyeapp.model;

public class LoginResponse {
    private boolean success;
    private String message;
    private OwnerInfo ownerInfo;
    
    // 构造函数
    public LoginResponse() {
    }
    
    public LoginResponse(boolean success, String message, OwnerInfo ownerInfo) {
        this.success = success;
        this.message = message;
        this.ownerInfo = ownerInfo;
    }
    
    // getter和setter方法
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public OwnerInfo getOwnerInfo() {
        return ownerInfo;
    }
    
    public void setOwnerInfo(OwnerInfo ownerInfo) {
        this.ownerInfo = ownerInfo;
    }
}
