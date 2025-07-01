package com.example.wuyeapp.network.client;

import com.example.wuyeapp.network.api.ApiService;
import com.example.wuyeapp.network.api.ShopApiService;
import com.example.wuyeapp.network.api.ShopAuthApiService;
import com.example.wuyeapp.session.UnifiedAuthManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClientFactory {
    private static final String MAIN_BASE_URL = "http://10.0.2.2:5000"; // 替换为实际的主程序API地址
    private static final String SHOP_BASE_URL = "http://10.0.2.2:5100"; // 替换为实际的商城API地址

    private OkHttpClient.Builder httpClientBuilder;
    private UnifiedAuthManager authManager;
    private AuthInterceptor authInterceptor;

    public ApiClientFactory(UnifiedAuthManager authManager) {
        this.authManager = authManager;
        this.httpClientBuilder = new OkHttpClient.Builder();
        if (authManager != null) {
            this.authInterceptor = new AuthInterceptor(authManager);
            this.httpClientBuilder.addInterceptor(this.authInterceptor);
        }
    }

    public void setAuthManager(UnifiedAuthManager authManager) {
        this.authManager = authManager;
        if (this.authInterceptor != null) {
            this.httpClientBuilder.interceptors().remove(this.authInterceptor);
        }
        if (authManager != null) {
            this.authInterceptor = new AuthInterceptor(authManager);
            this.httpClientBuilder.addInterceptor(this.authInterceptor);
        }
    }

    private OkHttpClient getHttpClient() {
        return httpClientBuilder.build();
    }

    public ApiService createMainApiService() {
        return new Retrofit.Builder()
                .baseUrl(MAIN_BASE_URL)
                .client(getHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
    }

    public ShopApiService createShopApiService() {
        return new Retrofit.Builder()
                .baseUrl(SHOP_BASE_URL)
                .client(getHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ShopApiService.class);
    }

    public ShopAuthApiService createShopAuthApiService() {
        return new Retrofit.Builder()
                .baseUrl(SHOP_BASE_URL)
                .client(getHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ShopAuthApiService.class);
    }
} 