package com.example.wuyeapp.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.wuyeapp.model.user.LoginRequest;
import com.example.wuyeapp.model.user.LoginResponse;
import com.example.wuyeapp.model.user.UserAuthMapping;
import com.example.wuyeapp.network.api.ApiService;
import com.example.wuyeapp.network.api.ShopAuthApiService;
import com.example.wuyeapp.model.shop.ShopAuthResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UnifiedAuthManager {
    private static final String PREF_NAME = "unified_auth";
    private static final String KEY_MAIN_TOKEN = "main_token";
    private static final String KEY_SHOP_TOKEN = "shop_token";
    private static final String KEY_USER_ID = "user_id";
    
    private static UnifiedAuthManager instance;
    private final Context context;
    private final ApiService apiService;
    private final ShopAuthApiService shopAuthService;
    private final SharedPreferences preferences;
    
    private UnifiedAuthManager(Context context, ApiService apiService, ShopAuthApiService shopAuthService) {
        this.context = context.getApplicationContext();
        this.apiService = apiService;
        this.shopAuthService = shopAuthService;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized UnifiedAuthManager getInstance(Context context, ApiService apiService, ShopAuthApiService shopAuthService) {
        if (instance == null) {
            instance = new UnifiedAuthManager(context, apiService, shopAuthService);
        }
        return instance;
    }
    
    public interface AuthCallback {
        void onSuccess(LoginResponse response);
        void onError(String message);
    }
    
    public void login(String username, String password, final AuthCallback callback) {
        LoginRequest request = new LoginRequest(username, password);
        
        // 调用统一登录接口
        apiService.unifiedLogin(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    // 保存主程序token
                    saveMainToken(loginResponse.getToken());
                    saveUserId(loginResponse.getUserId());
                    
                    // 获取用户认证映射
                    getUserAuthMapping(loginResponse.getUserId(), callback, loginResponse);
                } else {
                    callback.onError("登录失败");
                }
            }
            
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }
    
    private void getUserAuthMapping(long personalInfoId, final AuthCallback callback, final LoginResponse mainLoginResponse) {
        apiService.getUserAuthMapping(personalInfoId).enqueue(new Callback<UserAuthMapping>() {
            @Override
            public void onResponse(Call<UserAuthMapping> call, Response<UserAuthMapping> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 用户映射存在，直接使用商城token
                    callback.onSuccess(mainLoginResponse);
                } else {
                    // 需要创建新的用户映射
                    createUserAuthMapping(personalInfoId, callback, mainLoginResponse);
                }
            }
            
            @Override
            public void onFailure(Call<UserAuthMapping> call, Throwable t) {
                callback.onError("获取用户映射失败: " + t.getMessage());
            }
        });
    }
    
    private void createUserAuthMapping(long personalInfoId, final AuthCallback callback, final LoginResponse mainLoginResponse) {
        UserAuthMapping mapping = new UserAuthMapping();
        mapping.setPersonalInfoId(personalInfoId);
        
        apiService.createUserAuthMapping(mapping).enqueue(new Callback<UserAuthMapping>() {
            @Override
            public void onResponse(Call<UserAuthMapping> call, Response<UserAuthMapping> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(mainLoginResponse);
                } else {
                    callback.onError("创建用户映射失败");
                }
            }
            
            @Override
            public void onFailure(Call<UserAuthMapping> call, Throwable t) {
                callback.onError("创建用户映射失败: " + t.getMessage());
            }
        });
    }
    
    public String getMainToken() {
        return preferences.getString(KEY_MAIN_TOKEN, null);
    }
    
    public String getShopToken() {
        return preferences.getString(KEY_SHOP_TOKEN, null);
    }
    
    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, -1);
    }
    
    private void saveMainToken(String token) {
        preferences.edit().putString(KEY_MAIN_TOKEN, token).apply();
    }
    
    private void saveShopToken(String token) {
        preferences.edit().putString(KEY_SHOP_TOKEN, token).apply();
    }
    
    private void saveUserId(long userId) {
        preferences.edit().putLong(KEY_USER_ID, userId).apply();
    }
    
    public void logout() {
        preferences.edit()
                .remove(KEY_MAIN_TOKEN)
                .remove(KEY_SHOP_TOKEN)
                .remove(KEY_USER_ID)
                .apply();
    }
    
    public boolean isLoggedIn() {
        return !TextUtils.isEmpty(getMainToken());
    }
    
    public String getAuthorizationHeader() {
        String token = getMainToken();
        return token != null ? "Bearer " + token : null;
    }
} 