package com.example.wuyeapp.sip;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.MediaEncryption;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;
import org.linphone.core.Config;
import org.linphone.core.AudioDevice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
            Log.i(TAG, "====== 开始初始化Linphone Core ======");
            this.appContext = context.getApplicationContext();
            
            // 输出当前Android版本和网络状态
            Log.i(TAG, "当前Android版本: " + Build.VERSION.SDK_INT);
            Log.i(TAG, "当前网络状态: " + (isNetworkConnected() ? "已连接" : "未连接"));
            
            // 启用详细日志
            Factory.instance().setDebugMode(true, "LinphoneSIP");
            Log.i(TAG, "Linphone日志调试模式已启用");
            
            factory = Factory.instance();
            Log.i(TAG, "Linphone Factory初始化完成");
            
            // 创建Core前添加日志
            Log.i(TAG, "准备创建Linphone Core...");
            core = factory.createCore(null, null, context);
            Log.i(TAG, "Linphone Core创建成功");
            
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

            // 启动Core前添加日志
            Log.i(TAG, "准备启动Linphone Core...");
            core.start();
            Log.i(TAG, "Linphone Core启动成功");
            Log.i(TAG, "====== Linphone Core初始化完成 ======");
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
                    
                    // 处理特定场景的媒体通道配置
                    handleMediaPathConfiguration(call, state);
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
                        // 来电 - 自动路由音频到听筒
                        Log.i(TAG, "收到来电，路由音频到听筒");
                        routeAudioToEarpiece();
                        
                        if (callback != null) {
                            String caller = call.getRemoteAddress().asStringUriOnly();
                            Log.d(TAG, "来电者: " + caller);
                            callback.onIncomingCall(call, caller);
                        }
                        break;
                    case StreamsRunning:
                        // 通话中 - 根据是否为视频通话决定路由
                        Log.i(TAG, "通话已建立，媒体流运行中");
                        handleAudioRouting(call);
                        
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
                Log.i(TAG, "======= 注册状态变更 =======");
                Log.i(TAG, "状态: " + state + ", 消息: " + message);
                
                if (proxyConfig != null) {
                    Log.d(TAG, "账户: " + proxyConfig.getIdentityAddress().asString());
                    Log.d(TAG, "服务器: " + proxyConfig.getServerAddr());
                    
                    if (state == RegistrationState.Failed) {
                        Log.e(TAG, "注册失败: " + proxyConfig.getError());
                    }
                }
                
                switch (state) {
                    case Ok:
                        Log.i(TAG, "SIP注册成功");
                        if (callback != null) {
                            callback.onRegistrationSuccess();
                        }
                        break;
                    case Failed:
                        Log.e(TAG, "SIP注册失败: " + message);
                        if (callback != null) {
                            callback.onRegistrationFailed(message);
                        }
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
                // 优先使用H264，禁用其他编解码器
                boolean enable = "H264".equals(mimeType);
                Log.d(TAG, "视频编解码器: " + mimeType + " - " + (enable ? "启用" : "禁用"));
                pt.enable(enable);
            }
            
            // 配置音频编解码器
            org.linphone.core.PayloadType[] audioPayloads = core.getAudioPayloadTypes();
            for (org.linphone.core.PayloadType pt : audioPayloads) {
                String mimeType = pt.getMimeType();
                // 优先使用OPUS、PCMA和PCMU
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
            if (core != null) {
                // NAT穿透设置
                core.setNatAddress(null);  // 让系统自动处理
                core.setStunServer(null);  // 不使用STUN服务器
                
                // 网络相关设置
                core.setNetworkReachable(true);
                
                // 允许UDP通过NAT
                core.getConfig().setBool("net", "enable_nat_helper", true);
                
                Log.d(TAG, "NAT和网络配置完成");
            }
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
                
                // 使用更宽松的配置
                core.getConfig().setBool("net", "firewall_policy", false);
                
                // 修改用户代理以更好兼容
                core.setUserAgent("Linphone-Android", "");
                
                // 增加SIP超时设置，提高可靠性
                core.getConfig().setInt("sip", "sip_tcp_transport_timeout", 15);
                core.getConfig().setInt("sip", "register_timeout", 30);
                core.getConfig().setInt("sip", "default_expires", 60);  // 降低超时值
                
                // 允许在同一域中进行呼叫
                core.getConfig().setBool("sip", "use_domain_in_route", true);
                
                // DNS SRV配置
                core.getConfig().setBool("net", "dns_srv_enabled", true);
                
                // 回声消除配置
                core.setEchoCancellationEnabled(true);  
                
                // 自适应抖动补偿
                core.setAdaptiveRateControlEnabled(true);  
                
                // 调整RTP参数
                core.getConfig().setInt("rtp", "rtcp_xr_enabled", 1);
                core.getConfig().setInt("rtp", "audio_jitt_comp", 60); 
                
                // 日志记录
                Log.i(TAG, "回声消除已启用: " + core.isEchoCancellationEnabled());
                Log.i(TAG, "自适应比特率控制已启用: " + core.isAdaptiveRateControlEnabled());  
                
                Log.d(TAG, "Core参数配置完成");
            }
        } catch (Exception e) {
            Log.e(TAG, "配置Core参数失败", e);
        }
    }

    // 获取错误描述
    private String getErrorDescription(org.linphone.core.Reason reason) {
        if (reason == null) return "未知错误";
        
        switch (reason) {
            case None: return "无错误";
            case NoResponse: return "请求超时 - 服务器没有响应";
            case Forbidden: return "禁止 - 认证被拒绝";
            case Declined: return "请求被拒绝";
            case NotFound: return "未找到 - 用户或域名无效";
            case NotAnswered: return "无应答";
            case Busy: return "忙";
            case Unauthorized: return "未授权 - 认证信息有误";
            case UnsupportedContent: return "不支持的内容";
            case BadEvent: return "错误事件";
            case IOError: return "IO错误 - 检查网络连接";
            default: return "其他错误: " + reason.toString();
        }
    }

    private void initCore() {
        try {
            // 加载工厂配置
            String factoryConfigPath = appContext.getFilesDir().getAbsolutePath() + "/linphonerc";
            File factoryConfig = new File(factoryConfigPath);
            
            // 如果配置文件不存在，从assets复制
            if (!factoryConfig.exists()) {
                try {
                    copyAssetsToStorage("linphonerc_factory", factoryConfigPath);
                    Log.i(TAG, "已从assets复制配置文件到: " + factoryConfigPath);
                } catch (IOException e) {
                    Log.e(TAG, "复制配置文件失败", e);
                }
            }
            
            // 创建Factory和Core
            factory = Factory.instance();
            factory.setDebugMode(true, "LinphoneSIP");
            
            // 使用配置文件创建Core
            core = factory.createCore(factoryConfigPath, null, appContext);
            
            // 其他配置...
        } catch (Exception e) {
            Log.e(TAG, "初始化Core失败", e);
        }
    }

    // 从assets复制文件到存储
    private void copyAssetsToStorage(String assetFilename, String destinationPath) throws IOException {
        InputStream inputStream = appContext.getAssets().open(assetFilename);
        FileOutputStream outputStream = new FileOutputStream(destinationPath);
        
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        
        outputStream.close();
        inputStream.close();
    }

    // 处理媒体路径配置
    private void handleMediaPathConfiguration(Call call, Call.State state) {
        try {
            boolean isVideoCall = call.getCurrentParams().isVideoEnabled();  // 替换 videoEnabled
            
            if (state == Call.State.StreamsRunning) {
                // 通话建立后，根据视频状态设置合适的媒体路径
                if (isVideoCall) {
                    Log.i(TAG, "视频通话已建立，设置适当的媒体路径");
                    
                    // 如果是视频通话，检查是否需要切换到扬声器
                    routeAudioToSpeaker();
                } else {
                    Log.i(TAG, "语音通话已建立，设置适当的媒体路径");
                    
                    // 语音通话通常使用听筒
                    routeAudioToEarpiece();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "处理媒体路径配置失败", e);
        }
    }

    // 路由音频到扬声器
    private void routeAudioToSpeaker() {
        core.setOutputAudioDevice(getAudioDeviceForType(AudioDevice.Type.Speaker));
        Log.i(TAG, "音频已路由到扬声器");
    }

    // 路由音频到听筒
    private void routeAudioToEarpiece() {
        core.setOutputAudioDevice(getAudioDeviceForType(AudioDevice.Type.Earpiece));
        Log.i(TAG, "音频已路由到听筒");
    }

    // 根据设备类型获取音频设备
    private AudioDevice getAudioDeviceForType(AudioDevice.Type type) {
        for (AudioDevice device : core.getAudioDevices()) {
            if (device.getType() == type) {
                return device;
            }
        }
        
        // 如果找不到指定类型的设备，返回默认输出设备
        return core.getOutputAudioDevice();
    }

    // 根据通话类型处理音频路由
    private void handleAudioRouting(Call call) {
        boolean isVideoCall = call.getCurrentParams().isVideoEnabled();  // 替换 videoEnabled
        
        if (isVideoCall) {
            // 视频通话默认使用扬声器
            routeAudioToSpeaker();
        } else {
            // 音频通话默认使用听筒
            routeAudioToEarpiece();
        }
    }
} 