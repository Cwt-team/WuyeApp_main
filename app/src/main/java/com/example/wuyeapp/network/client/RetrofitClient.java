package com.example.wuyeapp.network.client;

import android.util.Log;

import com.example.wuyeapp.network.api.ApiService;
import com.example.wuyeapp.utils.LogUtil;
import com.example.wuyeapp.network.api.ShopApiService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    private static final String BASE_URL = "http://1.14.198.98:8090/"; // 示例URL，请务必修改
    private static RetrofitClient instance;
    private Retrofit retrofit;
    private ApiService apiService;

    private OkHttpClient.Builder httpClientBuilder;

    private RetrofitClient() {
        // 创建OkHttpClient并添加日志拦截器
        httpClientBuilder = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder.addInterceptor(loggingInterceptor);

        // 创建Retrofit实例
        rebuildRetrofit();

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

    // 添加认证头的方法
    public void addAuthHeader(final String headerName, final String headerValue) {
        // 添加拦截器，为每个请求添加认证头
        httpClientBuilder.interceptors().clear();

        // 重新添加日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder.addInterceptor(loggingInterceptor);

        // 添加认证拦截器
        httpClientBuilder.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header(headerName, headerValue)
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        });

        // 重建Retrofit实例
        rebuildRetrofit();
    }

    // 重建Retrofit实例
    private void rebuildRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // 创建API服务实例
        apiService = retrofit.create(ApiService.class);
    }

    /**
     * 获取ShopApiService的实例。
     * @return ShopApiService实例
     */
    public static ShopApiService getShopApiService() {
        return getInstance().retrofit.create(ShopApiService.class);
    }
}
