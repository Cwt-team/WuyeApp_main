package com.example.wuyeapp.sip.linphone;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

/**
 * Linphone SIP管理类
 */
public class LinphoneSipManager {
    private static final String TAG = "LinphoneSipManager";
    private static LinphoneSipManager instance;
    
    private Context context;
    private LinphoneService linphoneService;
    private boolean isBound = false;
    private LinphoneCallback linphoneCallback;
    
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LinphoneService.LocalBinder binder = (LinphoneService.LocalBinder) service;
            linphoneService = binder.getService();
            isBound = true;
            
            // 加载SIP设置并自动注册
            loadSettingsAndRegister();
            
            // 设置回调
            if (linphoneCallback != null) {
                linphoneService.setLinphoneCallback(linphoneCallback);
            }
            
            Log.i(TAG, "SIP服务已连接");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            linphoneService = null;
            isBound = false;
            Log.i(TAG, "SIP服务已断开");
        }
    };
    
    // 私有构造函数
    private LinphoneSipManager() {}
    
    // 获取单例实例
    public static synchronized LinphoneSipManager getInstance() {
        if (instance == null) {
            instance = new LinphoneSipManager();
        }
        return instance;
    }
    
    // 初始化
    public void init(Context context) {
        this.context = context.getApplicationContext();
        startAndBindService();
    }
    
    // 启动并绑定服务
    private void startAndBindService() {
        Intent intent = new Intent(context, LinphoneService.class);
        context.startForegroundService(intent);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    
    // 从设置中加载SIP账户信息并注册
    private void loadSettingsAndRegister() {
        SharedPreferences preferences = context.getSharedPreferences("SipSettings", Context.MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");
        String domain = preferences.getString("domain", "");
        String port = preferences.getString("port", "5060");
        
        if (!username.isEmpty() && !password.isEmpty() && !domain.isEmpty()) {
            // 如果端口不是默认的5060，则添加到域名后
            if (!port.equals("5060")) {
                domain = domain + ":" + port;
            }
            
            // 注册SIP账户
            if (linphoneService != null) {
                linphoneService.registerAccount(username, password, domain);
            }
        }
    }
    
    // 设置Linphone回调
    public void setLinphoneCallback(LinphoneCallback callback) {
        this.linphoneCallback = callback;
        if (isBound && linphoneService != null) {
            linphoneService.setLinphoneCallback(callback);
        }
    }
    
    // 拨打电话
    public void makeCall(String number) {
        if (isBound && linphoneService != null) {
            linphoneService.makeCall(number);
        } else {
            Log.e(TAG, "SIP服务未连接，无法拨打电话");
        }
    }
    
    // 接听来电
    public void answerCall() {
        if (isBound && linphoneService != null) {
            linphoneService.answerCall();
        }
    }
    
    // 挂断电话
    public void hangupCall() {
        if (isBound && linphoneService != null) {
            linphoneService.hangupCall();
        }
    }
    
    // 切换扬声器
    public void toggleSpeaker(boolean enable) {
        if (isBound && linphoneService != null) {
            linphoneService.toggleSpeaker(enable);
        }
    }
    
    // 切换麦克风静音
    public void toggleMute(boolean mute) {
        if (isBound && linphoneService != null) {
            linphoneService.toggleMute(mute);
        }
    }
    
    // 释放资源
    public void release() {
        if (isBound && context != null) {
            context.unbindService(connection);
            isBound = false;
        }
        
        context = null;
    }
} 