package com.example.wuyeapp.sip;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.MediaEncryption;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;
import org.linphone.core.Config;
import org.linphone.core.AudioDevice;
import org.linphone.core.VideoActivationPolicy;

import com.example.wuyeapp.utils.NetworkUtil;

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
            VideoActivationPolicy policy = core.getVideoActivationPolicy().clone();
            policy.setAutomaticallyInitiate(true);
            policy.setAutomaticallyAccept(true);
            core.setVideoActivationPolicy(policy);
            
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
                    
                    // 打印SDP和ICE候选信息
                    printSdpAndIceCandidates(call);

                    // 检查是否出现"Not Acceptable Here"错误
                    if (message != null && message.contains("Not Acceptable Here")) {
                        Log.e(TAG, "检测到'Not Acceptable Here'错误，正在打印诊断信息...");
                        printNotAcceptableHereDetails(call);
                    }
                    
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
                // 支持更多视频编解码器，而不仅仅是H264
                boolean enable = "H264".equals(mimeType) || "VP8".equals(mimeType) || "AV1".equals(mimeType);
                Log.d(TAG, "视频编解码器: " + mimeType + " - " + (enable ? "启用" : "禁用"));
                pt.enable(enable);
                
                // H264编解码器参数优化
                if ("H264".equals(mimeType)) {
                    pt.setRecvFmtp("profile-level-id=42801F");
                }
                
                // VP8编解码器参数设置
                if ("VP8".equals(mimeType)) {
                    pt.setRecvFmtp("max-fs=12288;max-fr=60");
                }
            }
            
            // 配置音频编解码器 - 启用更多的编解码器
            org.linphone.core.PayloadType[] audioPayloads = core.getAudioPayloadTypes();
            for (org.linphone.core.PayloadType pt : audioPayloads) {
                String mimeType = pt.getMimeType();
                int clockRate = pt.getClockRate();
                
                // 启用多种音频编解码器，并保证PCMA和PCMU为首选
                boolean enable = "PCMA".equalsIgnoreCase(mimeType) || 
                               "PCMU".equalsIgnoreCase(mimeType) ||
                               "opus".equalsIgnoreCase(mimeType) ||
                               "speex".equalsIgnoreCase(mimeType) ||
                               "G722".equalsIgnoreCase(mimeType) ||
                               "ILBC".equalsIgnoreCase(mimeType) ||
                               "GSM".equalsIgnoreCase(mimeType);
                
                // 优先级设置：PCMA/PCMU > opus > 其他
                if ("PCMA".equalsIgnoreCase(mimeType) || "PCMU".equalsIgnoreCase(mimeType)) {
                    pt.setRecvFmtp("annexb=no");
                    pt.enable(true);    // 最高优先级
                } else if ("opus".equalsIgnoreCase(mimeType)) {
                    pt.setRecvFmtp("useinbandfec=1;stereo=1");
                    pt.enable(true);    // 次高优先级
                } else if ("G722".equalsIgnoreCase(mimeType)) {
                    pt.enable(true);    // 三级优先级
                } else if (enable) {
                    pt.enable(true);    // 其他启用的编解码器
                } else {
                    pt.enable(false);   // 禁用的编解码器
                }
                
                Log.d(TAG, "音频编解码器: " + mimeType + "/" + clockRate + " - " + (enable ? "启用" : "禁用"));
                pt.enable(enable);
            }
            
            Log.i(TAG, "音视频编解码器配置完成，已启用更多选项");
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
                
                // 获取STUN服务器列表
                String[] stunServers = {
                    getStunServerAddress(),       // 主STUN服务器
                    "stun:stun.miwifi.com:3478",  // 小米路由器STUN
                    "stun:stun.qq.com:3478"       // 腾讯STUN
                };
                
                // 尝试多个STUN服务器直到找到可用的
                String stunServer = stunServers[0]; // 默认使用首选
                for (String server : stunServers) {
                    if (isStunServerReachable(server)) {
                        stunServer = server;
                        Log.i(TAG, "已选择可用的STUN服务器: " + stunServer);
                        break;
                    }
                }
                
                Log.i(TAG, "使用STUN服务器: " + stunServer);
                
                // 启用STUN和ICE
                org.linphone.core.NatPolicy natPolicy = core.createNatPolicy();
                natPolicy.setStunEnabled(true);
                natPolicy.setIceEnabled(true);
                natPolicy.setUpnpEnabled(true);  // 启用UPnP提高穿透成功率
                natPolicy.setStunServer(stunServer);
                core.setNatPolicy(natPolicy);
                
                // 网络相关设置
                core.setNetworkReachable(true);
                
                // 允许UDP通过NAT
                core.getConfig().setBool("net", "enable_nat_helper", true);
                
                // 增加ICE候选收集时间
                core.getConfig().setInt("net", "stun_server_timeout", 10000); // 10秒
                
                // 允许使用IPV6
                core.getConfig().setBool("net", "ipv6_enabled", true);
                
                // 禁用UDP端口复用，避免某些NAT下的问题
                core.getConfig().setBool("rtp", "disable_upnp", false);
                
                // 配置媒体ICE相关设置
                core.getConfig().setBool("net", "allow_late_ice", true); // 允许后期ICE协商
                core.getConfig().setInt("net", "ice_session_timer_value", 20); // 增加ICE会话时间
                
                Log.d(TAG, "NAT和网络配置完成");
            }
        } catch (Exception e) {
            Log.e(TAG, "配置NAT和网络失败", e);
        }
    }

    // 检查STUN服务器是否可达
    private boolean isStunServerReachable(String stunServer) {
        try {
            // 从stun:stun.server.com:port格式中提取地址和端口
            String server = stunServer.replace("stun:", "");
            String host = server;
            int port = 3478; // 默认STUN端口
            
            if (server.contains(":")) {
                String[] parts = server.split(":");
                host = parts[0];
                port = Integer.parseInt(parts[1]);
            }
            
            // 尝试解析域名
            Log.d(TAG, "尝试解析STUN服务器: " + host);
            java.net.InetAddress address = java.net.InetAddress.getByName(host);
            Log.d(TAG, "STUN服务器IP: " + address.getHostAddress());
            
            // 测试端口是否开放
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new java.net.InetSocketAddress(address, port), 3000); // 3秒超时
            socket.close();
            
            Log.i(TAG, "STUN服务器可达: " + stunServer);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "STUN服务器不可达: " + stunServer + " - " + e.getMessage());
            return false;
        }
    }

    // 获取STUN服务器地址
    private String getStunServerAddress() {
        // 首先尝试从应用配置中获取
        SharedPreferences preferences = appContext.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String stunServer = preferences.getString("stun_server", null);
        
        // 如果未配置，则使用用户购买的中国STUN服务器
        if (stunServer == null || stunServer.isEmpty()) {
            // 使用用户购买的中国STUN服务器，支持国内访问
            stunServer = "stun:116.198.199.38:3478";
            Log.i(TAG, "使用中国STUN服务器: " + stunServer);
        }
        
        return stunServer;
    }

    // 创建NAT策略
    private org.linphone.core.NatPolicy createNatPolicy() {
        org.linphone.core.NatPolicy natPolicy = core.createNatPolicy();
        natPolicy.setStunEnabled(true);
        natPolicy.setIceEnabled(true); 
        natPolicy.setStunServer(getStunServerAddress()); // 使用配置的STUN服务器
        return natPolicy;
    }

    // 核心配置参数
    private void configureCore() {
        try {
            if (core != null) {
                // 配置回声消除和噪声抑制 - 使用配置方式替代直接方法调用
                core.getConfig().setBool("sound", "echocancellation", true);
                core.getConfig().setBool("sound", "echo_limiter", true);
                core.getConfig().setBool("sound", "noise_gate", true);
                
                // 允许自适应比特率控制，根据网络条件调整
                core.getConfig().setBool("net", "adaptive_rate_control", true);
                
                // 设置默认视频大小
                core.setPreferredVideoDefinitionByName("720p"); // 设置为720p
                
                // 设置视频预览启用
                core.getConfig().setBool("video", "preview", false);
                
                // 设置自动下载编解码器
                core.setDownloadBandwidth(0); // 不限制下载带宽
                core.setUploadBandwidth(0);   // 不限制上传带宽
                
                // 设置抖动缓冲区参数
                core.getConfig().setInt("rtp", "audio_jitt_comp", 60); 
                
                // 日志记录
                Log.i(TAG, "回声消除已配置: " + core.getConfig().getBool("sound", "echocancellation", false));
                Log.i(TAG, "自适应比特率控制已配置: " + core.getConfig().getBool("net", "adaptive_rate_control", false));  
                
                // 更多细节配置 - 使用配置文件方式
                core.getConfig().setBool("sip", "reuse_authorization", true);
                core.getConfig().setInt("sip", "transport_timeout", 30);
                
                // RTP配置
                core.getConfig().setInt("rtp", "audio_jitt_comp", 100);
                core.getConfig().setInt("rtp", "audio_rtp_port", 7078);
                core.getConfig().setInt("rtp", "audio_rtp_port_max", 7178);
                
                // 通话相关设置
                core.getConfig().setInt("sip", "guess_hostname", 1);
                core.getConfig().setBool("sip", "register_only_when_network_is_up", true);
                core.getConfig().setBool("net", "auto_net_state_mon", true);
                
                // 音频配置
                core.getConfig().setBool("sound", "echocancellation", true);
                core.getConfig().setFloat("sound", "mic_gain_db", 0.0f);
                core.getConfig().setFloat("sound", "playback_gain_db", 0.0f);
                
                // 视频配置
                core.getConfig().setString("video", "displaytype", "MSAndroidTextureDisplay");
                core.getConfig().setBool("video", "auto_resize_preview_to_keep_ratio", true);
                
                // 禁用opus编解码器配置 - 改为启用但微调参数
                core.getConfig().setBool("audio_codec", "opus/48000/2", true);
                core.getConfig().setString("audio_codec", "opus/48000/2", "useinbandfec=1;stereo=1");
                
                // 为VP8和H264设置配置
                core.getConfig().setBool("video_codec", "VP8", true);
                core.getConfig().setString("video_codec", "VP8", "max-fs=12288;max-fr=60");
                core.getConfig().setBool("video_codec", "H264", true);
                core.getConfig().setString("video_codec", "H264", "profile-level-id=42801F");
                
                // 启用高质量视频处理
                core.getConfig().setBool("video", "preview_vsize", true);
                
                // 确保DTMF信号通过RFC2833和SIP INFO两种方式都发送，最大兼容性
                core.getConfig().setInt("sip", "dtmf_with_info", 1);
                core.getConfig().setInt("sip", "dtmf_with_sipinfo", 1);
                
                // 配置视频自动接受政策
                core.getConfig().setBool("video", "automatically_accept", true);
                core.getConfig().setBool("video", "automatically_initiate", true);
                
                // 确保音频编解码器配置正确
                for (org.linphone.core.PayloadType pt : core.getAudioPayloadTypes()) {
                    if ("PCMA".equalsIgnoreCase(pt.getMimeType()) && pt.getClockRate() == 8000) {
                        pt.enable(true);
                    } else if ("PCMU".equalsIgnoreCase(pt.getMimeType()) && pt.getClockRate() == 8000) {
                        pt.enable(true);
                    }
                }

                // 禁用不必要的高级功能
                core.getConfig().setBool("rtp", "audio_multicast_enabled", false);
                core.getConfig().setBool("rtp", "video_multicast_enabled", false);

                // 设置音频抖动缓冲区
                core.getConfig().setInt("rtp", "audio_jitt_comp", 60);
                core.getConfig().setInt("sound", "audio_buffer_size", 8); // ms

                // 确保音频初始化配置正确
                core.getConfig().setBool("sound", "play_during_early_media", true);
                
                Log.i(TAG, "Core核心配置完成");
            }
        } catch (Exception e) {
            Log.e(TAG, "配置Core失败", e);
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

    /**
     * 打印SDP和ICE候选信息
     * 用于检查SDP中的c=行和a=candidate行是否包含公网IP
     */
    private void printSdpAndIceCandidates(Call call) {
        try {
            // 获取当前通话的参数
            CallParams params = call.getCurrentParams();
            if (params != null) {
                Log.i(TAG, "===== 本地SDP信息 =====");
                Log.i(TAG, "是否启用视频: " + params.isVideoEnabled());
                Log.i(TAG, "使用的媒体加密: " + params.getMediaEncryption().toString());
                // 音频总是启用的，不需要检查
                Log.i(TAG, "音频已启用");
            }

            // 获取远程参数
            CallParams remoteParams = call.getRemoteParams();
            if (remoteParams != null) {
                Log.i(TAG, "===== 远程SDP信息 =====");
                // 音频总是启用的，不需要检查
                Log.i(TAG, "远程音频已启用");
                Log.i(TAG, "远程视频是否启用: " + remoteParams.isVideoEnabled());
            }
            
            // 获取网络相关信息
            if (call.getParams() != null) {
                Log.i(TAG, "当前通话状态: " + call.getState().toString());
                
                // 获取NAT策略
                org.linphone.core.NatPolicy natPolicy = core.getNatPolicy();
                if (natPolicy != null) {
                    Log.i(TAG, "STUN服务器: " + natPolicy.getStunServer());
                    // 检查是否启用了STUN和ICE
                    Log.i(TAG, "已配置STUN服务器: " + (natPolicy.getStunServer() != null && !natPolicy.getStunServer().isEmpty()));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "打印SDP和ICE信息时出错", e);
        }
    }
    
    /**
     * 解析并打印SDP中的地址信息
     * 注意：当前API版本可能不支持直接获取SDP内容
     */
    private void printSdpAddressInfo(String sdp) {
        // 由于API限制，此方法可能无法直接访问SDP内容
        Log.i(TAG, "当前API版本可能不支持直接访问SDP内容");
    }

    /**
     * 打印与"Not Acceptable Here"错误相关的详细诊断信息
     * @param call 当前通话
     */
    private void printNotAcceptableHereDetails(Call call) {
        Log.e(TAG, "========= Not Acceptable Here 错误诊断 =========");
        
        try {
            // 1. 检查NAT策略
            Log.e(TAG, "1. ICE配置状态:");
            org.linphone.core.NatPolicy natPolicy = core.getNatPolicy();
            if (natPolicy != null) {
                Log.e(TAG, "   - STUN服务器: " + natPolicy.getStunServer());
                // 判断是否正确配置了STUN服务器
                boolean stunConfigured = natPolicy.getStunServer() != null && !natPolicy.getStunServer().isEmpty();
                Log.e(TAG, "   - STUN服务器已配置: " + stunConfigured);
            } else {
                Log.e(TAG, "   - NAT策略未配置");
            }
            
            // 2. 获取本地网络信息
            Log.e(TAG, "2. 本地网络信息:");
            String localIp = NetworkUtil.getLocalIpAddress();
            if (localIp != null) {
                Log.e(TAG, "   - 本地IP地址: " + localIp);
                Log.e(TAG, "   - 是否为内网IP: " + NetworkUtil.isPrivateIpAddress(localIp));
            } else {
                Log.e(TAG, "   - 无法获取本地IP地址");
            }
            
            // 3. 获取通话参数信息
            Log.e(TAG, "3. 媒体协商问题:");
            CallParams params = call.getCurrentParams();
            if (params != null) {
                // 检查媒体类型
                Log.e(TAG, "   - 音频始终启用");
                Log.e(TAG, "   - 视频是否启用: " + params.isVideoEnabled());
                Log.e(TAG, "   - 媒体加密类型: " + params.getMediaEncryption().toString());
            }
            
            // 4. 检查原因和错误代码
            Log.e(TAG, "4. 错误原因:");
            Log.e(TAG, "   - 错误原因: " + call.getErrorInfo().getReason().toString());
            Log.e(TAG, "   - 协议错误代码: " + call.getErrorInfo().getProtocolCode());
            Log.e(TAG, "   - 详细错误信息: " + call.getErrorInfo().getPhrase());
            
            Log.e(TAG, "============================================");
        } catch (Exception e) {
            Log.e(TAG, "诊断Not Acceptable Here错误时发生异常", e);
            e.printStackTrace();
        }
    }
    
    /**
     * 检查网络配置是否适合SIP通话
     */
    private boolean isNetworkConfiguredProperly() {
        // 检查是否有网络连接
        if (!isNetworkConnected()) {
            Log.e(TAG, "网络未连接");
            return false;
        }
        
        // 检查NAT策略
        org.linphone.core.NatPolicy natPolicy = core.getNatPolicy();
        if (natPolicy == null || natPolicy.getStunServer() == null || natPolicy.getStunServer().isEmpty()) {
            Log.e(TAG, "STUN服务器未正确配置");
            return false;
        }
        
        return true;
    }

    public void initAudioDevices() {
        Log.i(TAG, "初始化音频设备...");
        try {
            // 获取并打印可用音频设备
            AudioDevice[] devices = core.getAudioDevices();
            if (devices != null && devices.length > 0) {
                Log.i(TAG, "发现" + devices.length + "个音频设备");
                for (AudioDevice device : devices) {
                    Log.i(TAG, "音频设备: " + device.getDeviceName() + " 类型:" + device.getType());
                }
            } else {
                Log.e(TAG, "未发现音频设备，这可能导致通话问题");
            }

            // 确保音频采集设备已设置
            AudioDevice captureDevice = core.getInputAudioDevice();
            if (captureDevice != null) {
                Log.i(TAG, "当前音频采集设备: " + captureDevice.getDeviceName());
            } else {
                Log.e(TAG, "未设置音频采集设备，尝试设置默认设备");
                // 尝试设置默认的麦克风设备
                for (AudioDevice device : devices) {
                    if (device.getType() == AudioDevice.Type.Microphone) {
                        core.setInputAudioDevice(device);
                        Log.i(TAG, "已设置默认麦克风: " + device.getDeviceName());
                        break;
                    }
                }
            }
            
            // 检查音频输出设备
            AudioDevice outputDevice = core.getOutputAudioDevice();
            if (outputDevice == null) {
                // 尝试设置默认的扬声器
                for (AudioDevice device : devices) {
                    if (device.getType() == AudioDevice.Type.Speaker) {
                        core.setOutputAudioDevice(device);
                        Log.i(TAG, "已设置默认输出设备: " + device.getDeviceName());
                        break;
                    }
                }
            }
            
            // 确保音频流已启用
            if (!core.isMicEnabled()) {
                core.setMicEnabled(true);
                Log.i(TAG, "已启用麦克风");
            }
        } catch (Exception e) {
            Log.e(TAG, "初始化音频设备时出错", e);
        }
    }
} 