package com.example.wuyeapp;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.os.SystemClock;

import com.example.wuyeapp.utils.LogUtil;

public class WuyeApplication extends Application {
    private static Context context;
    
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        
        // 注册Activity生命周期回调
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                LogUtil.i(activity.getClass().getSimpleName() + " - onCreated");
            }

            @Override
            public void onActivityStarted(Activity activity) {
                LogUtil.i(activity.getClass().getSimpleName() + " - onStarted");
            }

            @Override
            public void onActivityResumed(Activity activity) {
                LogUtil.i(activity.getClass().getSimpleName() + " - onResumed");
            }

            @Override
            public void onActivityPaused(Activity activity) {
                LogUtil.i(activity.getClass().getSimpleName() + " - onPaused");
            }

            @Override
            public void onActivityStopped(Activity activity) {
                LogUtil.i(activity.getClass().getSimpleName() + " - onStopped");
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                LogUtil.i(activity.getClass().getSimpleName() + " - onSaveInstanceState");
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                LogUtil.i(activity.getClass().getSimpleName() + " - onDestroyed");
            }
        });
    }

    public static Context getContext() {
        return context;
    }
} 