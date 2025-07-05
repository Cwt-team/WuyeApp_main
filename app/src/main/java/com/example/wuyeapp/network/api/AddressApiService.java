package com.example.wuyeapp.network.api;

import com.example.wuyeapp.model.base.BaseResponse;
import com.example.wuyeapp.model.user.Address;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AddressApiService {
    // 获取地址列表
    @GET("/api/mobile/addresses")
    Call<BaseResponse<List<Address>>> getAddressList(@Query("user_id") long userId);

    // 新增地址
    @POST("/api/mobile/addresses")
    Call<BaseResponse<Address>> addAddress(@Body Address address);

    // 更新地址
    @PUT("/api/mobile/addresses/{id}")
    Call<BaseResponse<Address>> updateAddress(@Path("id") int id, @Body Address address);

    // 删除地址
    @DELETE("/api/mobile/addresses/{id}")
    Call<BaseResponse<Void>> deleteAddress(@Path("id") int id);

    // 设置默认地址
    @PUT("/api/mobile/addresses/{id}/default")
    Call<BaseResponse<Address>> setDefaultAddress(@Path("id") int id);
} 