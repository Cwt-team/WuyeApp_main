package com.example.wuyeapp.api;

import com.example.wuyeapp.model.LoginResponse;
import com.example.wuyeapp.model.OwnerInfo;
import com.example.wuyeapp.model.OwnerDetailResponse;
import com.example.wuyeapp.model.OwnerUpdateRequest;
import com.example.wuyeapp.model.FaceUploadResponse;
import com.example.wuyeapp.model.BaseResponse;
import com.example.wuyeapp.model.MaintenanceRequest;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

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

    // 获取业主详细信息
    @GET("api/mobile/owners/{id}")
    Call<OwnerDetailResponse> getOwnerDetail(@Path("id") long id);

    // 更新业主信息
    @PUT("api/mobile/owners/{id}")
    Call<BaseResponse> updateOwnerInfo(
        @Path("id") long id,
        @Body OwnerUpdateRequest updateRequest
    );

    // 上传人脸图像
    @Multipart
    @POST("api/mobile/owners/{id}/face")
    Call<FaceUploadResponse> uploadFace(
        @Path("id") long id,
        @Part MultipartBody.Part image
    );

    // 提交报修请求
    @POST("api/mobile/maintenance/request")
    Call<BaseResponse> submitMaintenanceRequest(@Body MaintenanceRequest request);
}
