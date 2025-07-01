package com.example.wuyeapp.network.client;

import android.os.Build;
import android.text.TextUtils;
import com.example.wuyeapp.network.api.ApiService;
import com.example.wuyeapp.network.api.ShopApiService;
import com.example.wuyeapp.network.api.ShopAuthApiService;
import com.example.wuyeapp.utils.LogUtil;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";

    // Base URLs for different backends
    private static final String PROPERTY_MANAGEMENT_BASE_URL;
    private static final String MALL_MANAGEMENT_BASE_URL;

    // 超时设置
    private static final int CONNECT_TIMEOUT = 20; // 连接超时时间（秒）
    private static final int READ_TIMEOUT = 20;    // 读取超时时间（秒）
    private static final int WRITE_TIMEOUT = 20;   // 写入超时时间（秒）

    static {
        if (isEmulator()) {
            PROPERTY_MANAGEMENT_BASE_URL = "http://10.0.2.2:5000/"; // 物业服务
            MALL_MANAGEMENT_BASE_URL = "http://10.0.2.2:5100/api/mobile/"; // 商城服务，添加api/mobile前缀
            LogUtil.i(TAG + " 检测到模拟器，API地址: " + PROPERTY_MANAGEMENT_BASE_URL);
        } else {
            PROPERTY_MANAGEMENT_BASE_URL = "http://10.0.2.2:5000/"; // 生产环境物业服务
            MALL_MANAGEMENT_BASE_URL = "http://10.0.2.2:5100/api/mobile/"; // 生产环境商城服务
            LogUtil.i(TAG + " 检测到物理设备，API地址: " + PROPERTY_MANAGEMENT_BASE_URL);
        }
    }

    private static RetrofitClient instance;

    // Separate Retrofit instances for each backend
    private Retrofit propertyManagementRetrofit;
    private Retrofit mallManagementRetrofit;

    // API Service interfaces
    private ApiService propertyManagementApiService;
    private ShopApiService mallManagementShopApiService;
    private ShopAuthApiService mallManagementAuthApiService;

    // OkHttpClient Builders for managing interceptors
    private OkHttpClient.Builder propertyHttpClientBuilder;
    private OkHttpClient.Builder mallHttpClientBuilder;

    // Authentication Token
    private String authToken = ""; // Initialize with empty string

    private RetrofitClient() {
        // Common logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Initialize OkHttpClient Builders with timeouts
        propertyHttpClientBuilder = new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)  // 启用自动重试
            .addInterceptor(loggingInterceptor);

        mallHttpClientBuilder = new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)  // 启用自动重试
            .addInterceptor(loggingInterceptor);

        // Build initial Retrofit instances
        rebuildPropertyManagementRetrofit();
        rebuildMallManagementRetrofit();

        LogUtil.i(TAG + " RetrofitClient 初始化成功");
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    // Set authentication token and rebuild relevant Retrofit instances
    public void setAuthToken(String token) {
        this.authToken = token;
        rebuildPropertyManagementRetrofit(); // Rebuild for authenticated property management calls
        rebuildMallManagementRetrofit();    // Rebuild for authenticated mall management calls
        LogUtil.i(TAG + " 认证令牌已设置并重建Retrofit实例");
    }

    // Get API Service for Property Management Backend
    public static ApiService getApiService() {
        return getInstance().propertyManagementApiService;
    }

    // Get Shop API Service for Mall Management Backend
    public ShopApiService getShopApiService() {
        return mallManagementShopApiService;
    }

    // Get Shop Auth API Service for Mall Management Backend
    public ShopAuthApiService getShopAuthApiService() {
        return mallManagementAuthApiService;
    }

    // Rebuild Retrofit instance for Property Management Backend
    private void rebuildPropertyManagementRetrofit() {
        // Clear previous auth interceptors if any, then add common ones (like logging)
        propertyHttpClientBuilder.interceptors().clear();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        propertyHttpClientBuilder.addInterceptor(loggingInterceptor);

        // Add auth interceptor if token exists
        if (!authToken.isEmpty()) {
            propertyHttpClientBuilder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + authToken)
                            .method(original.method(), original.body());
                    return chain.proceed(requestBuilder.build());
                }
            });
        }

        propertyManagementRetrofit = new Retrofit.Builder()
                .baseUrl(PROPERTY_MANAGEMENT_BASE_URL)
                .client(propertyHttpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        propertyManagementApiService = propertyManagementRetrofit.create(ApiService.class);
    }

    // Rebuild Retrofit instance for Mall Management Backend
    private void rebuildMallManagementRetrofit() {
        // Clear previous auth interceptors if any, then add common ones (like logging)
        mallHttpClientBuilder.interceptors().clear();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        mallHttpClientBuilder.addInterceptor(loggingInterceptor);

        if (!authToken.isEmpty()) {
            mallHttpClientBuilder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + authToken)
                            .method(original.method(), original.body());
                    return chain.proceed(requestBuilder.build());
                }
            });
        }

        mallManagementRetrofit = new Retrofit.Builder()
                .baseUrl(MALL_MANAGEMENT_BASE_URL)
                .client(mallHttpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mallManagementShopApiService = mallManagementRetrofit.create(ShopApiService.class);
        mallManagementAuthApiService = mallManagementRetrofit.create(ShopAuthApiService.class);
    }

    private static boolean isEmulator() {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT));
    }
}
