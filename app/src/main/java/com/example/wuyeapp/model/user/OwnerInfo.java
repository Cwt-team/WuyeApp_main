package com.example.wuyeapp.model.user;

import com.google.gson.annotations.SerializedName;

public class OwnerInfo {
    @SerializedName("id")
    private long id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("phoneNumber")
    private String phone;
    
    @SerializedName("account")
    private String account;
    
    @SerializedName("communityId")
    private String communityId;
    
    @SerializedName("houseId")
    private String houseId;
    
    private String email;
    private String avatar;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public String getPhoneNumber() {
        return getPhone();
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }

    public String getHouseId() {
        return houseId;
    }

    public void setHouseId(String houseId) {
        this.houseId = houseId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
