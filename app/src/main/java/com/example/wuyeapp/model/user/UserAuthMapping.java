package com.example.wuyeapp.model.user;

public class UserAuthMapping {
    private int id;
    private int mallUserId;
    private long personalInfoId;
    private String createdAt;
    private String updatedAt;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMallUserId() {
        return mallUserId;
    }

    public void setMallUserId(int mallUserId) {
        this.mallUserId = mallUserId;
    }

    public long getPersonalInfoId() {
        return personalInfoId;
    }

    public void setPersonalInfoId(long personalInfoId) {
        this.personalInfoId = personalInfoId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
} 