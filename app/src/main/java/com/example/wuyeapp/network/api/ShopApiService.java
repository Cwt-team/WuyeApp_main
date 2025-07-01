package com.example.wuyeapp.network.api;

import com.example.wuyeapp.model.ApiResponse;
import com.example.wuyeapp.model.shop.Product;
import com.example.wuyeapp.model.shop.ProductListResponse;
import com.example.wuyeapp.model.shop.Category;
import com.example.wuyeapp.model.shop.ShopListResponse; 
import com.example.wuyeapp.model.shop.Shop; 
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ShopApiService {
    /**
     * 获取商品列表
     * GET /api/mobile/products
     */
    @GET("/api/mobile/products")
    Call<ApiResponse<ProductListResponse>> getProducts(
        @Query("page") Integer page,
        @Query("size") Integer size,
        @Query("search") String search,
        @Query("category_id") Integer categoryId,
        @Query("shop_id") Integer shopId
    );

    /**
     * 获取分类列表
     * GET /api/mobile/categories
     */
    @GET("/api/mobile/categories")
    Call<ApiResponse<List<Category>>> getCategories(
        @Query("shop_id") Integer shopId
    );

    /**
     * 获取商品详情
     * GET /api/mobile/products/{productId}
     */
    @GET("/api/mobile/products/{product_id}")
    Call<ApiResponse<Product>> getProductDetail(
        @Path("product_id") int productId
    );

    /**
     * 获取商铺列表
     * GET /api/mobile/shops
     */
    @GET("/api/mobile/shops")
    Call<ApiResponse<ShopListResponse>> getShops(
        @Query("page") Integer page,
        @Query("size") Integer size,
        @Query("search") String search
    );
}