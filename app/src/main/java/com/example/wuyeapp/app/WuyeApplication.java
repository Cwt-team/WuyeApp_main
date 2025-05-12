package com.example.wuyeapp.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import android.os.Build;

import com.example.wuyeapp.utils.LogUtil;
// 删除旧引用
// import com.example.wuyeapp.sip.SipManager;
import com.example.wuyeapp.sip.LinphoneSipManager;
import com.example.wuyeapp.sip.LinphoneCallback;
import com.example.wuyeapp.ui.call.CallActivity;
import org.linphone.core.Call;
import org.linphone.core.Factory;
import com.example.wuyeapp.sip.LinphoneService;
// 注释掉有问题的导入语句
// import com.example.wuyeapp.BuildConfig;

public class WuyeApplication extends Application {
    private static Context context;
    private static final String TAG = "WuyeApplication";
    
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
        
        // 初始化LinphoneSipManager
        LinphoneSipManager.getInstance().init(this);
        
        // 设置全局来电回调
        setupGlobalCallHandler();
        
        // 启动LinphoneService前台服务
        startLinphoneService();
        
        // 旧SipManager已不再需要，但为了平滑过渡可以保留
        // SipManager.getInstance().init(this);
    }
    
    // 设置全局来电处理
    private void setupGlobalCallHandler() {
        LinphoneSipManager.getInstance().setLinphoneCallback(new LinphoneCallback() {
            @Override
            public void onRegistrationSuccess() {
                Log.i(TAG, "全局SIP注册成功");
            }

            @Override
            public void onRegistrationFailed(String reason) {
                Log.e(TAG, "全局SIP注册失败: " + reason);
            }

            @Override
            public void onIncomingCall(Call call, String caller) {
                Log.i(TAG, "全局收到来电: " + caller);
                // 直接启动来电界面
                handleIncomingCall(caller, call);
            }

            @Override
            public void onCallProgress() {
                // 不需要全局处理
            }

            @Override
            public void onCallEstablished() {
                // 不需要全局处理
            }

            @Override
            public void onCallEnded() {
                // 不需要全局处理
            }

            @Override
            public void onCallFailed(String reason) {
                // 不需要全局处理
            }
        });
    }
    
    // 处理来电的方法
    private void handleIncomingCall(String caller, Call call) {
        // 创建带FLAG_ACTIVITY_NEW_TASK标志的Intent
        Intent callIntent = new Intent(this, CallActivity.class);
        callIntent.setAction("ANSWER_CALL");
        callIntent.putExtra("caller", caller);
        
        // 检查是否为视频通话
        if (call != null && call.getRemoteParams() != null) {
            boolean isVideoCall = call.getRemoteParams().isVideoEnabled();
            callIntent.putExtra("isVideo", isVideoCall);
        }
        
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(callIntent);
    }

    private void startLinphoneService() {
        Intent serviceIntent = new Intent(this, LinphoneService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Log.i(TAG, "启动LinphoneService前台服务，确保后台接听来电");
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