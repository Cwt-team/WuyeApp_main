package com.example.wuyeapp.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // 接口定义：成功回调
    public interface OnSuccessListener<T> {
        void onSuccess(T result);
    }

    // 接口定义：失败回调
    public interface OnFailureListener {
        void onFailure(Exception e);
    }

    // 异步执行任务的方法
    public static <T> void executeAsync(Task<T> task, OnSuccessListener<T> successListener, OnFailureListener failureListener) {
        executor.execute(() -> {
            try {
                // 执行任务
                T result = task.execute();
                
                // 在主线程返回结果
                mainHandler.post(() -> {
                    if (successListener != null) {
                        successListener.onSuccess(result);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error executing task: " + e.getMessage(), e);
                
                // 在主线程返回错误
                mainHandler.post(() -> {
                    if (failureListener != null) {
                        failureListener.onFailure(e);
                    }
                });
            }
        });
    }

    // 任务接口
    public interface Task<T> {
        T execute() throws Exception;
    }
} 