package com.example.wuyeapp.model.base;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * 基础响应类，用于通用API响应
 */
public class BaseResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int code;

    // 用于接收后端返回的额外字段
    @SerializedName("houseExists")
    private Boolean houseExists;
    @SerializedName("houseId")
    private Integer houseId;

    public BaseResponse() {
    }

    public BaseResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public BaseResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Boolean getHouseExists() {
        return houseExists;
    }

    public void setHouseExists(Boolean houseExists) {
        this.houseExists = houseExists;
    }

    public Integer getHouseId() {
        return houseId;
    }

    public void setHouseId(Integer houseId) {
        this.houseId = houseId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
} 