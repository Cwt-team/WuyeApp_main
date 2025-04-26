package com.example.wuyeapp.sip;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.Factory;

/**
 * Linphone SIP管理类，用于替代原来的SipManager
 */
public class LinphoneSipManager {
    private static final String TAG = "LinphoneSipManager";
    private static LinphoneSipManager instance;
    
    private Context context;
    private LinphoneService linphoneService;
    private boolean isBound = false;
    private LinphoneCallback linphoneCallback;
    private SipCallback sipCallback; // 用于兼容旧接口
    
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
    
    // 设置SIP回调（兼容旧接口）
    public void setSipCallback(SipCallback callback) {
        this.sipCallback = callback;
        
        // 创建一个适配器将SipCallback转换为LinphoneCallback
        setLinphoneCallback(new LinphoneCallback() {
            @Override
            public void onRegistrationSuccess() {
                if (sipCallback != null) {
                    sipCallback.onRegistrationSuccess();
                }
            }
            
            @Override
            public void onRegistrationFailed(String reason) {
                if (sipCallback != null) {
                    sipCallback.onRegistrationFailed(reason);
                }
            }
            
            @Override
            public void onIncomingCall(Call call, String caller) {
                if (sipCallback != null) {
                    // 创建一个适配器将Linphone的Call转换为SipCall
                    SipCall sipCall = new LinphoneCallAdapter(call);
                    sipCallback.onIncomingCall(sipCall, caller);
                }
            }
            
            @Override
            public void onCallProgress() {
                // 在SipCallback中没有对应方法
            }
            
            @Override
            public void onCallEstablished() {
                if (sipCallback != null) {
                    sipCallback.onCallEstablished();
                }
            }
            
            @Override
            public void onCallEnded() {
                if (sipCallback != null) {
                    sipCallback.onCallEnded();
                }
            }
            
            @Override
            public void onCallFailed(String reason) {
                if (sipCallback != null) {
                    sipCallback.onCallFailed(reason);
                }
            }
        });
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
    
    // Linphone Call适配器
    private class LinphoneCallAdapter extends SipCall {
        private final Call linphoneCall;
        
        public LinphoneCallAdapter(Call linphoneCall) {
            this.linphoneCall = linphoneCall;
        }
        
        @Override
        public void answer() {
            if (isBound && linphoneService != null) {
                linphoneService.answerCall();
            }
        }
        
        @Override
        public void hangup() {
            if (isBound && linphoneService != null) {
                linphoneService.hangupCall();
            }
        }
    }
    
    // 添加一个方法获取服务
    public LinphoneService getLinphoneService() {
        return linphoneService;
    }
    
    // 添加直接注册测试的方法
    public void testRegistration(String username, String password, String domain, LinphoneCallback callback) {
        this.linphoneCallback = callback;
        if (linphoneService != null) {
            linphoneService.setLinphoneCallback(callback);
            linphoneService.registerAccount(username, password, domain);
        } else {
            if (callback != null) {
                callback.onRegistrationFailed("服务尚未准备就绪");
            }
        }
    }
    
    // 添加一个方法获取当前版本信息，用于调试
    public String getVersionInfo() {
        try {
            // 获取Linphone SDK版本信息的替代方式
            Core core = Factory.instance().createCore(null, null, context);
            return "Linphone Core 初始化成功";
        } catch (Exception e) {
            return "无法获取Linphone版本信息: " + e.getMessage();
        }
    }
    
    // 拨打视频电话
    public void makeVideoCall(String number) {
        if (isBound && linphoneService != null) {
            linphoneService.makeVideoCall(number);
        } else {
            Log.e(TAG, "SIP服务未连接，无法拨打视频电话");
        }
    }
    
    // 切换摄像头
    public void switchCamera() {
        if (isBound && linphoneService != null) {
            linphoneService.switchCamera();
        }
    }
    
    // 设置视频预览和显示
    public void setVideoSurfaces(Object localVideoSurface, Object remoteVideoSurface) {
        if (isBound && linphoneService != null) {
            linphoneService.setVideoSurfaces(localVideoSurface, remoteVideoSurface);
        }
    }
    
    // 转接通话
    public void transferCall(String destination) {
        if (isBound && linphoneService != null) {
            linphoneService.transferCall(destination);
        }
    }
    
    // 保持/恢复通话
    public void toggleHoldCall() {
        if (isBound && linphoneService != null) {
            linphoneService.toggleHoldCall();
        }
    }
    
    // 合并通话（创建会议）
    public void mergeCallsIntoConference() {
        if (isBound && linphoneService != null) {
            linphoneService.mergeCallsIntoConference();
        }
    }
} 