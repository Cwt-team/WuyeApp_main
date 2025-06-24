package com.example.wuyeapp.network.api;

import com.example.wuyeapp.model.shop.Shop;
import com.example.wuyeapp.model.shop.Product;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ShopApiService {

    /**
     * 获取所有商铺列表
     * GET /api/shop/
     * @return 包含所有商铺的列表
     */
    @GET("/api/shop/")
    Call<List<Shop>> getAllShops();

    /**
     * 获取单个商铺的详细信息
     * GET /api/shop/{shop_id}
     * @param shopId 商铺的ID
     * @return 单个商铺的详细信息
     */
    @GET("/api/shop/{shop_id}")
    Call<Shop> getShopDetails(@Path("shop_id") int shopId);

    // 假设后端也有获取某个商铺下所有商品的接口
    /**
     * 获取指定商铺的所有商品列表
     * GET /api/shop/{shop_id}/products
     * 注意：如果您的后端没有这个接口，请根据实际情况调整或删除
     * @param shopId 商铺的ID
     * @return 包含指定商铺所有商品的列表
     */
    @GET("/api/shop/{shop_id}/products")
    Call<List<Product>> getProductsByShopId(@Path("shop_id") int shopId);

    // 假设后端有获取所有商品的接口
    /**
     * 获取所有商品列表
     * GET /api/products/
     * 注意：如果您的后端没有这个接口，请根据实际情况调整或删除
     * @return 包含所有商品的列表
     */
    @GET("/api/products/")
    Call<List<Product>> getAllProducts();
}