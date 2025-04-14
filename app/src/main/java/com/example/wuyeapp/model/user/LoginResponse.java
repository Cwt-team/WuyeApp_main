package com.example.wuyeapp.model.user;

import com.example.wuyeapp.model.base.BaseResponse;

public class LoginResponse extends BaseResponse {
    private OwnerInfo ownerInfo;
    
    // 构造函数
    public LoginResponse() {
        super();
    }
    
    public LoginResponse(boolean success, String message, OwnerInfo ownerInfo) {
        super(success, message);
        this.ownerInfo = ownerInfo;
    }
    
    // 只需要保留特有的字段的getter和setter
    public OwnerInfo getOwnerInfo() {
        return ownerInfo;
    }
    
    public void setOwnerInfo(OwnerInfo ownerInfo) {
        this.ownerInfo = ownerInfo;
        setData(ownerInfo); // 同时设置父类的data字段
    }
}
