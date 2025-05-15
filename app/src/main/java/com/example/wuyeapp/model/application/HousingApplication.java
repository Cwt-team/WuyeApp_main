package com.example.wuyeapp.model.application;

/**
 * 房屋绑定申请模型类
 */
public class HousingApplication {
    private long id;
    private int communityId;
    private String communityName;
    private String buildingName;
    private String unitName;
    private String houseNumber;
    private String status;
    private String applicationTime;
    private String callbackMessage;
    
    public HousingApplication() {
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public int getCommunityId() {
        return communityId;
    }
    
    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }
    
    public String getCommunityName() {
        return communityName;
    }
    
    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }
    
    public String getBuildingName() {
        return buildingName;
    }
    
    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }
    
    public String getUnitName() {
        return unitName;
    }
    
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }
    
    public String getHouseNumber() {
        return houseNumber;
    }
    
    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getApplicationTime() {
        return applicationTime;
    }
    
    public void setApplicationTime(String applicationTime) {
        this.applicationTime = applicationTime;
    }
    
    public String getCallbackMessage() {
        return callbackMessage;
    }
    
    public void setCallbackMessage(String callbackMessage) {
        this.callbackMessage = callbackMessage;
    }
}