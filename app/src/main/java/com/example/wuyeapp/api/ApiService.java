package com.example.wuyeapp.api;

import com.example.wuyeapp.model.LoginResponse;
import com.example.wuyeapp.model.OwnerInfo;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // 登录API
    @FormUrlEncoded
    @POST("api/mobile/login")
    Call<LoginResponse> login(
            @Field("username") String username,
            @Field("password") String password
    );
    
    // 获取业主信息
    @GET("api/owners/{phone}")
    Call<OwnerInfo> getOwnerByPhone(@Path("phone") String phone);
    
    // 测试连接API
    @GET("api/ping")
    Call<Void> testConnection();
}
