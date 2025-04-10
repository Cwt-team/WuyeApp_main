package com.example.wuyeapp.database;

import android.util.Log;
import com.example.wuyeapp.api.ApiClient;
import com.example.wuyeapp.api.ApiService;
import com.example.wuyeapp.model.OwnerInfo;
import com.example.wuyeapp.utils.LogUtil;

/**
 * 数据访问层 - 重构版
 * 所有数据库操作都通过API接口完成，不再直接连接数据库
 */
public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";
    private static DatabaseHelper instance;
    private final ApiService apiService;

    public DatabaseHelper() {
        // 初始化API服务
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    // 以下所有方法都通过API调用实现，不再直接连接数据库
    
//    // 例如，通过API获取业主信息
//    public void getOwnerByPhone(String phone, ApiCallback<OwnerInfo> callback) {
//        LogUtil.i(TAG, "通过API获取业主信息: " + phone);
//        // 使用Retrofit进行API调用
//        apiService.getOwnerByPhone(phone).enqueue(new retrofit2.Callback<OwnerInfo>() {
//            @Override
//            public void onResponse(retrofit2.Call<OwnerInfo> call, retrofit2.Response<OwnerInfo> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    callback.onSuccess(response.body());
//                } else {
//                    callback.onError("获取业主信息失败: " + response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(retrofit2.Call<OwnerInfo> call, Throwable t) {
//                callback.onError("网络错误: " + t.getMessage());
//                LogUtil.e(TAG, "获取业主信息失败", t);
//            }
//        });
//    }

    // API回调接口
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMsg);
    }
} 