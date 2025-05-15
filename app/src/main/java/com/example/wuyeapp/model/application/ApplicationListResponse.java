package com.example.wuyeapp.model.application;

import com.example.wuyeapp.model.base.BaseResponse;

import java.util.List;

/**
 * 房屋绑定申请列表响应类
 */
public class ApplicationListResponse extends BaseResponse {
    private List<HousingApplication> applications;
    
    public ApplicationListResponse() {
        super();
    }
    
    public List<HousingApplication> getApplications() {
        return applications;
    }
    
    public void setApplications(List<HousingApplication> applications) {
        this.applications = applications;
    }
} 