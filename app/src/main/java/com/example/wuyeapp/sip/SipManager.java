package com.example.wuyeapp.sip;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class SipManager {
    private static final String TAG = "SipManager";
    private static SipManager instance;
    
    private Context context;
    private SipService sipService;
    private boolean isBound = false;
    private SipCallback sipCallback;
    
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SipService.SipBinder binder = (SipService.SipBinder) service;
            sipService = binder.getService();
            isBound = true;
            
            // 加载SIP设置并自动注册
            loadSettingsAndRegister();
            
            // 设置回调
            if (sipCallback != null) {
                sipService.setSipCallback(sipCallback);
            }
            
            Log.i(TAG, "SIP服务已连接");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            sipService = null;
            isBound = false;
            Log.i(TAG, "SIP服务已断开");
        }
    };
    
    // 私有构造函数
    private SipManager() {}
    
    // 获取单例实例
    public static synchronized SipManager getInstance() {
        if (instance == null) {
            instance = new SipManager();
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
        Intent intent = new Intent(context, SipService.class);
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
            if (sipService != null) {
                sipService.registerAccount(username, password, domain);
            }
        }
    }
    
    // 设置回调
    public void setSipCallback(SipCallback callback) {
        this.sipCallback = callback;
        if (isBound && sipService != null) {
            sipService.setSipCallback(callback);
        }
    }
    
    // 拨打电话
    public void makeCall(String number) {
        if (isBound && sipService != null) {
            sipService.makeCall(number);
        } else {
            Log.e(TAG, "SIP服务未连接，无法拨打电话");
        }
    }
    
    // 释放资源
    public void release() {
        if (isBound && context != null) {
            context.unbindService(connection);
            isBound = false;
        }
    }
}
