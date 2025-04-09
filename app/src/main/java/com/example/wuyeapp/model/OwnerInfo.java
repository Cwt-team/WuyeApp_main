package com.example.wuyeapp.model;

public class OwnerInfo {
    private long id;
    private int communityId;
    private int houseId;
    private String name;
    private String gender;
    private String phoneNumber;
    private String idCard;
    private String email;
    private String city;
    private String address;
    private String ownerType;
    private String faceImage;
    private int faceStatus;
    private String account;
    private String password;
    private String wxOpenid;
    private String updatedAt;
    
    // 默认构造函数
    public OwnerInfo() {
    }
    
    // 构造函数 - 用于登录返回最基本信息
    public OwnerInfo(long id, String name, String phoneNumber, String account) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.account = account;
    }
    
    // Getters and Setters
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
    
    public int getHouseId() {
        return houseId;
    }
    
    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getIdCard() {
        return idCard;
    }
    
    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getOwnerType() {
        return ownerType;
    }
    
    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }
    
    public String getFaceImage() {
        return faceImage;
    }
    
    public void setFaceImage(String faceImage) {
        this.faceImage = faceImage;
    }
    
    public int getFaceStatus() {
        return faceStatus;
    }
    
    public void setFaceStatus(int faceStatus) {
        this.faceStatus = faceStatus;
    }
    
    public String getAccount() {
        return account;
    }
    
    public void setAccount(String account) {
        this.account = account;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getWxOpenid() {
        return wxOpenid;
    }
    
    public void setWxOpenid(String wxOpenid) {
        this.wxOpenid = wxOpenid;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
