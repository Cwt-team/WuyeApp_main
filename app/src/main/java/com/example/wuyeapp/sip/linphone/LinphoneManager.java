package com.example.wuyeapp.sip.linphone;

import android.content.Context;
import android.util.Log;

import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.MediaEncryption;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;

/**
 * Linphone核心管理类，单例模式
 */
public class LinphoneManager {
    private static final String TAG = "LinphoneManager";
    
    private static volatile LinphoneManager instance;
    
    private Factory factory;
    private Core core;
    private CoreListenerStub listener;
    private LinphoneCallback callback;
    
    // 私有构造函数，确保单例
    private LinphoneManager(Context context) {
        // 初始化Linphone
        try {
            factory = Factory.instance();
            core = factory.createCore(null, null, context);
            
            // 正确设置视频功能
            // 使用视频策略来启用视频
            core.getVideoActivationPolicy().setAutomaticallyInitiate(true);
            core.getVideoActivationPolicy().setAutomaticallyAccept(true);
            
            // 回声消除和音频增益设置
            Log.d(TAG, "Echo Cancellation enabled: " + core.isEchoCancellationEnabled());
            Log.d(TAG, "Mic Gain: " + core.getMicGainDb());
            Log.d(TAG, "Playback Gain: " + core.getPlaybackGainDb());
            
            // 启动Core
            core.start();
            Log.i(TAG, "Linphone Core初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "Linphone Core初始化失败", e);
        }
    }
    
    // 获取单例实例
    public static LinphoneManager getInstance(Context context) {
        if (instance == null) {
            synchronized (LinphoneManager.class) {
                if (instance == null) {
                    instance = new LinphoneManager(context);
                }
            }
        }
        return instance;
    }
    
    // 设置事件监听器
    public void setListener(LinphoneCallback callback) {
        this.callback = callback;
        
        // 创建监听器
        listener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
                Log.d(TAG, "通话状态变更: " + state + ", 消息: " + message);
                
                switch (state) {
                    case OutgoingInit:
                    case OutgoingProgress:
                        // 呼出
                        if (callback != null) {
                            callback.onCallProgress();
                        }
                        break;
                    case IncomingReceived:
                        // 来电
                        if (callback != null) {
                            String caller = call.getRemoteAddress().asStringUriOnly();
                            callback.onIncomingCall(call, caller);
                        }
                        break;
                    case StreamsRunning:
                        // 通话中
                        if (callback != null) {
                            callback.onCallEstablished();
                        }
                        break;
                    case End:
                    case Released:
                        // 通话结束
                        if (callback != null) {
                            callback.onCallEnded();
                        }
                        break;
                    case Error:
                        // 通话错误
                        if (callback != null) {
                            callback.onCallFailed(message);
                        }
                        break;
                }
            }
            
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig proxyConfig, RegistrationState state, String message) {
                Log.i(TAG, "账户注册状态变更: " + state + ", 消息: " + message);
                
                switch (state) {
                    case Ok:
                        // 注册成功
                        if (callback != null) {
                            callback.onRegistrationSuccess();
                        }
                        break;
                    case Failed:
                        // 注册失败
                        if (callback != null) {
                            callback.onRegistrationFailed(message);
                        }
                        break;
                }
            }
        };
        
        // 添加监听器
        core.addListener(listener);
    }
    
    // 获取Core实例
    public Core getCore() {
        return core;
    }
    
    // 释放资源
    public void release() {
        if (listener != null && core != null) {
            core.removeListener(listener);
        }
        
        if (core != null) {
            core.stop();
        }
        
        instance = null;
    }
} 