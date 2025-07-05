package com.example.wuyeapp.model.user;

import com.google.gson.annotations.SerializedName;

public class Address {
    @SerializedName("id")
    private int id;
    @SerializedName("user_id")
    private long userId;
    @SerializedName("receiver_name")
    private String receiverName;
    @SerializedName("phone")
    private String phone;
    @SerializedName("province")
    private String province;
    @SerializedName("city")
    private String city;
    @SerializedName("district")
    private String district;
    @SerializedName("detail_address")
    private String detailAddress;
    @SerializedName("is_default")
    private boolean isDefault;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;

    // getter 和 setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getDetailAddress() { return detailAddress; }
    public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
} 