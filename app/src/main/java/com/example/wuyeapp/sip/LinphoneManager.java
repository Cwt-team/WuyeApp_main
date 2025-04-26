package com.example.wuyeapp.sip;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.MediaEncryption;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;
import org.linphone.core.Config;

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
    
    private Context appContext;
    
    // 私有构造函数，确保单例
    private LinphoneManager(Context context) {
        try {
            Log.i(TAG, "开始初始化Linphone Core");
            this.appContext = context.getApplicationContext();
            
            // 启用详细日志
            Factory.instance().setDebugMode(true, "LinphoneSIP");
            
            factory = Factory.instance();
            
            // 创建Core
            core = factory.createCore(null, null, context);
            
            // 详细配置网络和NAT设置
            configureNatAndNetwork();
            
            // 视频配置
            core.getVideoActivationPolicy().setAutomaticallyInitiate(true);
            core.getVideoActivationPolicy().setAutomaticallyAccept(true);
            
            // 配置核心参数
            configureCore();
            
            // 音频和视频格式配置
            configurePayloadTypes();
            
            // 确保网络可达
            core.setNetworkReachable(true);

            // 启动Core
            core.start();
            Log.i(TAG, "Linphone Core初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "Linphone Core初始化失败", e);
            e.printStackTrace();
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

    // 配置音视频编解码器
    private void configurePayloadTypes() {
        try {
            // 配置视频编解码器
            org.linphone.core.PayloadType[] videoPayloads = core.getVideoPayloadTypes();
            for (org.linphone.core.PayloadType pt : videoPayloads) {
                String mimeType = pt.getMimeType();
                // 优先使用H264
                boolean enable = "H264".equals(mimeType);
                Log.d(TAG, "视频编解码器: " + mimeType + " - " + (enable ? "启用" : "禁用"));
                pt.enable(enable);
            }
            
            // 配置音频编解码器
            org.linphone.core.PayloadType[] audioPayloads = core.getAudioPayloadTypes();
            for (org.linphone.core.PayloadType pt : audioPayloads) {
                String mimeType = pt.getMimeType();
                // 优先使用OPUS，其次是PCMA/PCMU
                boolean enable = "opus".equalsIgnoreCase(mimeType) || 
                                "PCMA".equalsIgnoreCase(mimeType) || 
                                "PCMU".equalsIgnoreCase(mimeType);
                Log.d(TAG, "音频编解码器: " + mimeType + " - " + (enable ? "启用" : "禁用"));
                pt.enable(enable);
            }
        } catch (Exception e) {
            Log.e(TAG, "配置编解码器失败", e);
        }
    }

    public void setNetworkReachable() {
        if (core != null) {
            boolean connected = isNetworkConnected();
            Log.d(TAG, "设置网络状态: " + (connected ? "可达" : "不可达"));
            core.setNetworkReachable(connected);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void setVideoWindows(Object localVideoView, Object remoteVideoView) {
        if (core != null) {
            try {
                core.setNativePreviewWindowId(localVideoView);
                core.setNativeVideoWindowId(remoteVideoView);
                Log.d(TAG, "视频窗口已设置");
            } catch (Exception e) {
                Log.e(TAG, "设置视频窗口失败", e);
            }
        }
    }

    // 配置NAT和网络设置
    private void configureNatAndNetwork() {
        try {
            // 配置NAT穿透
            org.linphone.core.Transports transports = core.getTransports();
            Log.d(TAG, "当前传输配置: UDP=" + transports.getUdpPort() + ", TCP=" + transports.getTcpPort());
            
            // 确保所有端口都启用，增加连接成功率
            transports.setUdpPort(0); // 0表示随机端口
            transports.setTcpPort(0);
            transports.setTlsPort(0);
            core.setTransports(transports);
            
            // 配置防火墙策略
            core.setNatPolicy(createNatPolicy());
            
            Log.d(TAG, "NAT和网络设置已配置");
        } catch (Exception e) {
            Log.e(TAG, "配置NAT和网络失败", e);
        }
    }

    // 创建NAT策略
    private org.linphone.core.NatPolicy createNatPolicy() {
        org.linphone.core.NatPolicy natPolicy = core.createNatPolicy();
        natPolicy.setStunEnabled(true);
        natPolicy.setIceEnabled(true); 
        natPolicy.setStunServer("stun.linphone.org"); // 使用公共STUN服务器
        return natPolicy;
    }

    // 核心配置参数
    private void configureCore() {
        try {
            if (core != null) {
                // SIP配置
                core.getConfig().setBool("sip", "guess_hostname", true);
                core.getConfig().setBool("sip", "register_only_when_network_is_up", true);
                core.getConfig().setBool("sip", "auto_net_state_mon", true);
                core.getConfig().setBool("net", "firewall_policy", false); // false=no firewall
                
                // 连接超时设置
                core.getConfig().setInt("sip", "sip_tcp_transport_timeout", 15);
                core.getConfig().setInt("sip", "register_timeout", 30);
                
                // 回声消除配置
                core.getConfig().setBool("sound", "echocancellation", true);
                
                Log.d(TAG, "Core参数配置完成");
            }
        } catch (Exception e) {
            Log.e(TAG, "配置Core参数失败", e);
        }
    }
} 