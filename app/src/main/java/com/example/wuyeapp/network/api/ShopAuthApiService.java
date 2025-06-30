package com.example.wuyeapp.network.api;

import com.example.wuyeapp.model.shop.ShopAuthResponse;
import com.example.wuyeapp.model.user.LoginRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ShopAuthApiService {
    /**
     * 商城系统登录
     * POST /api/auth/login
     * @param loginRequest 登录请求参数
     * @return 登录响应，包含token和用户信息
     */
    @POST("/api/auth/login")
    Call<ShopAuthResponse> login(@Body LoginRequest loginRequest);
    
    /**
     * 刷新商城系统token
     * POST /api/auth/refresh
     * @param token 当前的token，格式：Bearer {token}
     * @return 新的token和用户信息
     */
    @POST("/api/auth/refresh")
    Call<ShopAuthResponse> refreshToken(@Header("Authorization") String token);
} 