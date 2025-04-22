package com.example.wuyeapp.app;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;

import com.example.wuyeapp.utils.LogUtil;
// 删除旧引用
// import com.example.wuyeapp.sip.SipManager;
import com.example.wuyeapp.sip.LinphoneSipManager;
import org.linphone.core.Factory;
import com.example.wuyeapp.BuildConfig;

public class WuyeApplication extends Application {
    private static Context context;
    
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        
        // 初始化日志功能
        Log.i("WuyeApp", "应用程序启动，启用详细日志");
        
        // 初始化Linphone Factory
        Factory.instance();
        
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
        
        // 初始化LinphoneSipManager（新的SIP管理器）
        LinphoneSipManager.getInstance().init(this);
        
        // 旧SipManager已不再需要，但为了平滑过渡可以保留
        // SipManager.getInstance().init(this);
    }

    @Override
    public void onTerminate() {
        // 释放LinphoneSipManager资源
        LinphoneSipManager.getInstance().release();
        
        // 旧SipManager已不再需要，但为了平滑过渡可以保留
        // SipManager.getInstance().release();
        
        super.onTerminate();
    }

    public static Context getContext() {
        return context;
    }
} 