package com.example.wuyeapp.model.maintenance;

import com.example.wuyeapp.model.base.BaseResponse;

public class MaintenanceDetailResponse extends BaseResponse {
    private MaintenanceRequest data;
    
    public MaintenanceRequest getData() {
        return data;
    }
    
    public void setData(MaintenanceRequest data) {
        this.data = data;
    }
} 