package com.example.wuyeapp.sip;

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
            Log.i(TAG, "开始初始化Linphone Core");
            factory = Factory.instance();
            core = factory.createCore(null, null, context);
            
            // 配置音视频
            Log.d(TAG, "配置Linphone Core视频设置");
            core.getVideoActivationPolicy().setAutomaticallyInitiate(true);
            core.getVideoActivationPolicy().setAutomaticallyAccept(true);
            
            // 日志更多配置信息
            Log.d(TAG, "Linphone Core配置信息:");
            Log.d(TAG, "Echo Cancellation enabled: " + core.isEchoCancellationEnabled());
            Log.d(TAG, "Mic Gain: " + core.getMicGainDb());
            Log.d(TAG, "Playback Gain: " + core.getPlaybackGainDb());
            
            // 设置网络可达
            Log.d(TAG, "设置网络状态为可达");
            core.setNetworkReachable(true);

            // 记录传输配置
            try {
                Log.d(TAG, "当前使用的传输配置: " + core.getTransports().toString());
            } catch (Exception e) {
                Log.e(TAG, "无法获取传输配置信息", e);
            }

            // 启动Core
            Log.d(TAG, "启动Linphone Core");
            core.start();
            Log.i(TAG, "Linphone Core初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "Linphone Core初始化失败", e);
            e.printStackTrace(); // 打印详细堆栈
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
        Log.d(TAG, "设置LinphoneCallback监听器: " + (callback != null ? "非空" : "空"));
        this.callback = callback;
        
        // 创建监听器
        listener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
                Log.d(TAG, "通话状态变更: " + state + ", 消息: " + message);
                
                if (call != null) {
                    Log.d(TAG, "通话详情: 远程地址=" + call.getRemoteAddress().asString());
                    Log.d(TAG, "通话方向: " + (call.getDir() == Call.Dir.Incoming ? "来电" : "去电"));
                    Log.d(TAG, "通话持续时间: " + call.getDuration() + "秒");
                }
                
                switch (state) {
                    case OutgoingInit:
                    case OutgoingProgress:
                        // 呼出
                        Log.i(TAG, "呼出通话进行中");
                        if (callback != null) {
                            callback.onCallProgress();
                        }
                        break;
                    case IncomingReceived:
                        // 来电
                        Log.i(TAG, "收到来电");
                        if (callback != null) {
                            String caller = call.getRemoteAddress().asStringUriOnly();
                            Log.d(TAG, "来电者: " + caller);
                            callback.onIncomingCall(call, caller);
                        }
                        break;
                    case StreamsRunning:
                        // 通话中
                        Log.i(TAG, "通话已建立，媒体流运行中");
                        if (callback != null) {
                            callback.onCallEstablished();
                        }
                        break;
                    case End:
                    case Released:
                        // 通话结束
                        Log.i(TAG, "通话已结束: " + state);
                        if (callback != null) {
                            callback.onCallEnded();
                        }
                        break;
                    case Error:
                        // 通话错误
                        Log.e(TAG, "通话错误: " + message);
                        if (callback != null) {
                            callback.onCallFailed(message);
                        }
                        break;
                    default:
                        Log.d(TAG, "其他通话状态: " + state);
                        break;
                }
            }
            
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig proxyConfig, RegistrationState state, String message) {
                Log.i(TAG, "注册状态变更: " + state + ", 消息: " + message);
                
                if (proxyConfig != null) {
                    Log.d(TAG, "代理配置详情:");
                    Log.d(TAG, "身份地址: " + proxyConfig.getIdentityAddress().asString());
                    Log.d(TAG, "服务器地址: " + proxyConfig.getServerAddr());
                    Log.d(TAG, "传输类型: " + proxyConfig.getTransport());
                    Log.d(TAG, "注册期限: " + proxyConfig.getExpires() + "秒");
                    Log.d(TAG, "联系人参数: " + proxyConfig.getContactParameters());
                    Log.d(TAG, "联系人URI参数: " + proxyConfig.getContactUriParameters());
                    
                    if (state == RegistrationState.Failed) {
                        Log.e(TAG, "注册失败详情:");
                        Log.e(TAG, "错误代码: " + proxyConfig.getError());
                        Log.e(TAG, "错误信息: " + proxyConfig.getErrorInfo());
                    }
                }
                
                switch (state) {
                    case Ok:
                        // 注册成功
                        Log.i(TAG, "SIP注册成功");
                        if (callback != null) {
                            callback.onRegistrationSuccess();
                        }
                        break;
                    case Failed:
                        // 注册失败
                        Log.e(TAG, "SIP注册失败: " + message);
                        if (callback != null) {
                            callback.onRegistrationFailed(message);
                        }
                        break;
                    case Progress:
                        Log.d(TAG, "SIP注册进行中...");
                        break;
                    case Cleared:
                        Log.d(TAG, "SIP注册已清除");
                        break;
                    case None:
                        Log.d(TAG, "SIP注册状态：无");
                        break;
                    default:
                        Log.d(TAG, "其他SIP注册状态: " + state);
                        break;
                }
            }
        };
        
        // 添加监听器
        Log.d(TAG, "向Core添加监听器");
        if (core != null) {
            core.addListener(listener);
        } else {
            Log.e(TAG, "无法添加监听器，Core为空");
        }
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