package com.example.wuyeapp.network.client;

import com.example.wuyeapp.session.UnifiedAuthManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final UnifiedAuthManager authManager;

    public AuthInterceptor(UnifiedAuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // 如果请求已经有Authorization header，直接使用
        if (originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest);
        }

        // 获取认证token
        String token = authManager.getAuthorizationHeader();
        if (token == null) {
            return chain.proceed(originalRequest);
        }

        // 添加认证header
        Request authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", token)
                .build();

        return chain.proceed(authenticatedRequest);
    }
} 