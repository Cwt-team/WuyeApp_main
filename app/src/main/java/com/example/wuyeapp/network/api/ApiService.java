package com.example.wuyeapp.network.api;

import com.example.wuyeapp.model.user.LoginRequest;
import com.example.wuyeapp.model.user.LoginResponse;
import com.example.wuyeapp.model.user.OwnerInfo;
import com.example.wuyeapp.model.user.OwnerDetailResponse;
import com.example.wuyeapp.model.user.OwnerUpdateRequest;
import com.example.wuyeapp.model.user.FaceUploadResponse;
import com.example.wuyeapp.model.base.BaseResponse;
import com.example.wuyeapp.model.maintenance.MaintenanceRequest;
import com.example.wuyeapp.model.maintenance.MaintenanceListResponse;
import com.example.wuyeapp.model.maintenance.MaintenanceDetailResponse;
import com.example.wuyeapp.model.community.CommunitiesResponse;
import com.example.wuyeapp.model.application.ApplicationListResponse;
import com.example.wuyeapp.model.building.BuildingListResponse;
import com.example.wuyeapp.model.unit.UnitListResponse;
import com.example.wuyeapp.model.room.RoomListResponse;

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
import retrofit2.http.Query;

import java.util.Map;

public interface ApiService {
    // 登录API（账号或手机号+密码）
    @FormUrlEncoded
    @POST("api/mobile/login")
    Call<LoginResponse> login(
            @Field("username") String username,  // 改为username以匹配后端
            @Field("password") String password
    );
    
    // 注册API
    @FormUrlEncoded
    @POST("api/mobile/register")
    Call<BaseResponse> register(
            @Field("username") String username,
            @Field("password") String password,
            @Field("name") String name,
            @Field("phone") String phoneNumber
    );
    
    // 获取业主信息
    @GET("api/owners/{phone}")
    Call<OwnerInfo> getOwnerByPhone(@Path("phone") String phone);
    
    // 测试连接API
    @GET("api/test")
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

    // 获取报修列表
    @GET("api/mobile/maintenance/list")
    Call<MaintenanceListResponse> getMaintenanceList(
        @Query("ownerId") long ownerId,
        @Query("page") int page,
        @Query("size") int size,
        @Query("status") String status
    );

    // 获取报修详情
    @GET("api/mobile/maintenance/{id}")
    Call<MaintenanceDetailResponse> getMaintenanceDetail(@Path("id") long id);

    // 取消报修
    @POST("api/mobile/maintenance/{id}/cancel")
    Call<BaseResponse> cancelMaintenanceRequest(@Path("id") long id);

    // 评价报修
    @POST("api/mobile/maintenance/{id}/evaluate")
    Call<BaseResponse> evaluateMaintenanceRequest(
        @Path("id") long id,
        @Body Map<String, Object> evaluationData
    );
    
    // 获取社区列表
    @GET("api/mobile/communities")
    Call<CommunitiesResponse> getCommunities();
    
    // 提交房屋绑定申请
    @FormUrlEncoded
    @POST("api/mobile/housing-application")
    Call<BaseResponse> submitHousingApplication(
        @Field("ownerId") long ownerId,
        @Field("communityId") int communityId,
        @Field("buildingName") String buildingName,
        @Field("unitName") String unitName,
        @Field("houseNumber") String houseNumber,
        @Field("idCard") String idCard
    );
    
    // 获取用户房屋绑定申请记录
    @GET("api/mobile/housing-applications/{ownerId}")
    Call<ApplicationListResponse> getApplications(@Path("ownerId") long ownerId);
    
    // 上传申请证明材料
    @Multipart
    @POST("api/mobile/housing-application/{id}/proof")
    Call<BaseResponse> uploadApplicationProof(
        @Path("id") long applicationId,
        @Part MultipartBody.Part image
    );

    // 获取小区下所有楼栋
    @GET("api/mobile/buildings")
    Call<BuildingListResponse> getBuildings(@Query("communityId") int communityId);

    // 获取楼栋下所有单元
    @GET("api/mobile/units")
    Call<UnitListResponse> getUnits(@Query("buildingId") int buildingId);

    // 获取单元下所有房间
    @GET("api/mobile/rooms")
    Call<RoomListResponse> getRooms(@Query("unitId") int unitId);

    @POST("api/mobile/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
}
