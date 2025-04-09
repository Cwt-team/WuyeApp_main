package com.example.wuyeapp.api;

import android.util.Log;

import com.example.wuyeapp.utils.LogUtil;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    private static final String BASE_URL = "http://10.0.2.2:5000/"; // 与您后端服务匹配的URL
    private static RetrofitClient instance;
    private Retrofit retrofit;
    private ApiService apiService;
    
    private RetrofitClient() {
        // 创建OkHttpClient并添加日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
        
        // 创建Retrofit实例
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        // 创建API服务实例
        apiService = retrofit.create(ApiService.class);
        
        LogUtil.i(TAG + " 初始化成功: baseUrl = " + BASE_URL);
    }
    
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }
    
    public ApiService getApiService() {
        return apiService;
    }
}
