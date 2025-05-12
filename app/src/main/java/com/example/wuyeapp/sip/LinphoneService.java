package com.example.wuyeapp.sip;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.wuyeapp.R;
import com.example.wuyeapp.ui.call.CallActivity;

import org.linphone.core.Address;
import org.linphone.core.AudioDevice;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.Factory;
import org.linphone.core.MediaEncryption;
import org.linphone.core.TransportType;
import org.linphone.core.Account;
import org.linphone.core.AccountParams;
import org.linphone.core.AuthInfo;
import org.linphone.core.MediaDirection;
import org.linphone.core.RegistrationState;
import org.linphone.core.PayloadType;
import org.linphone.core.VideoActivationPolicy;

import java.util.List;

import android.content.pm.ServiceInfo;
import android.content.SharedPreferences;

import com.example.wuyeapp.utils.NetworkUtil;

import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;

import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;

import android.app.PendingIntent;

public class LinphoneService extends Service {
    private static final String TAG = "LinphoneService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "LinphoneServiceChannel";
    
    private LinphoneManager linphoneManager;
    private final IBinder binder = new LocalBinder();
    private LinphoneCallback linphoneCallback;
    
    private AudioManager audioManager;
    private boolean hasAudioFocus = false;
    
    // 用于绑定服务
    public class LocalBinder extends Binder {
        public LinphoneService getService() {
            return LinphoneService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.i(TAG, "LinphoneService正在创建");
        
        // 创建通知渠道
        createNotificationChannel();
        
        // 启动为前台服务，提高优先级避免系统杀死
        startForeground(NOTIFICATION_ID, createForegroundNotification());
        
        // 请求忽略电池优化
        requestIgnoreBatteryOptimization();
        
        // 初始化LinphoneManager
        linphoneManager = LinphoneManager.getInstance(getApplicationContext());
        Log.i(TAG, "LinphoneManager已初始化");
        
        // 设置监听器
        linphoneManager.setListener(new LinphoneCallback() {
            @Override
            public void onRegistrationSuccess() {
                Log.i(TAG, "SIP注册成功回调");
                if (linphoneCallback != null) {
                    linphoneCallback.onRegistrationSuccess();
                }
            }
            
            @Override
            public void onRegistrationFailed(String reason) {
                Log.e(TAG, "SIP注册失败回调: " + reason);
                if (linphoneCallback != null) {
                    linphoneCallback.onRegistrationFailed(reason);
                }
            }
            
            // 其他回调方法也添加日志
            @Override
            public void onIncomingCall(Call call, String caller) {
                Log.i(TAG, "收到来电: " + caller);
                if (linphoneCallback != null) {
                    linphoneCallback.onIncomingCall(call, caller);
                }
            }

            @Override
            public void onCallProgress() {
                Log.i(TAG, "通话进行中");
                if (linphoneCallback != null) {
                    linphoneCallback.onCallProgress();
                }
            }

            @Override
            public void onCallEstablished() {
                Log.i(TAG, "通话已建立");
                if (linphoneCallback != null) {
                    linphoneCallback.onCallEstablished();
                }
            }

            @Override
            public void onCallEnded() {
                Log.i(TAG, "通话已结束");
                if (linphoneCallback != null) {
                    linphoneCallback.onCallEnded();
                }
            }

            @Override
            public void onCallFailed(String reason) {
                Log.e(TAG, "通话失败: " + reason);
                if (linphoneCallback != null) {
                    linphoneCallback.onCallFailed(reason);
                }
            }
        });

        // 在onCreate中初始化
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public void onDestroy() {
        // 释放资源
        linphoneManager.release();
        super.onDestroy();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SIP服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    // 请求忽略电池优化
    private void requestIgnoreBatteryOptimization() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String packageName = getPackageName();
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    Log.i(TAG, "请求忽略电池优化以保持SIP服务在后台运行");
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "请求忽略电池优化失败", e);
        }
    }
    
    // 创建前台服务通知
    private Notification createForegroundNotification() {
        Intent notificationIntent = new Intent(this, CallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("物业SIP服务")
                .setContentText("保持连接，随时接听户户通来电")
                .setSmallIcon(R.drawable.ic_call)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MIN) // 静默通知，不打扰用户
                .build();
    }
    
    // 注册SIP账户
    public void registerAccount(String username, String password, String domain) {
        try {
            Log.i(TAG, "====== 开始注册SIP账户: " + username + "@" + domain + " ======");
            Core core = linphoneManager.getCore();
            
            // 获取STUN服务器设置
            SharedPreferences preferences = getApplicationContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
            String stunServer = preferences.getString("stun_server", "stun:116.198.199.38:3478");
            Log.i(TAG, "当前STUN服务器配置: " + stunServer);
            
            // 清除现有账户
            Log.d(TAG, "清除现有账户和认证信息");
            for (Account account : core.getAccountList()) {
                core.removeAccount(account);
            }
            for (AuthInfo authInfo : core.getAuthInfoList()) {
                core.removeAuthInfo(authInfo);
            }
            
            // 创建认证信息
            Log.d(TAG, "创建认证信息");
            AuthInfo authInfo = Factory.instance().createAuthInfo(
                    username,       // 用户名
                    username,       // 认证ID
                    password,       // 密码
                    null,           // ha1
                    null,           // realm
                    domain          // 域
            );
            
            // 创建账户参数
            Log.d(TAG, "创建账户参数");
            AccountParams accountParams = core.createAccountParams();
            
            // 设置身份地址 - 确保这是SIP服务器地址
            String sipAddress = "sip:" + username + "@" + domain;
            Log.d(TAG, "设置身份地址: " + sipAddress);
            Address identity = Factory.instance().createAddress(sipAddress);
            accountParams.setIdentityAddress(identity);
            
            // 设置服务器地址 - 确保这是SIP服务器地址
            String serverAddress = "sip:" + domain;
            Log.d(TAG, "设置SIP服务器地址: " + serverAddress);
            Address address = Factory.instance().createAddress(serverAddress);
            
            // 尝试多种传输方式 - 同时支持UDP和TCP
            address.setTransport(TransportType.Udp); // 先尝试UDP
            accountParams.setServerAddress(address);
            
            // 设置NAT策略 - 在这里使用STUN服务器配置
            org.linphone.core.NatPolicy natPolicy = createNatPolicy(core);
            accountParams.setNatPolicy(natPolicy);
            Log.d(TAG, "NAT策略已设置，使用STUN服务器: " + stunServer);
            
            // 设置更合适的注册超时
            accountParams.setRegisterEnabled(true);
            accountParams.setExpires(120); // 使用更短的超时
            
            // 创建账户
            Log.d(TAG, "创建账户");
            Account account = core.createAccount(accountParams);
            
            // 添加到Core
            core.addAuthInfo(authInfo);
            core.addAccount(account);
            core.setDefaultAccount(account);
            
            // 添加额外日志
            Log.d(TAG, "账户配置已应用");
            Log.d(TAG, "注册状态: " + account.getState());
            
            // 刷新注册
            Log.d(TAG, "刷新注册");
            core.refreshRegisters();
            
            // 创建账户参数后，只使用兼容的方法
            accountParams.setContactUriParameters(null); // 清除任何额外的URI参数
            accountParams.setPushNotificationAllowed(true); // 禁用推送通知
            accountParams.setQualityReportingEnabled(false); // 禁用质量报告
            
            Log.i(TAG, "SIP账户注册请求已发送");
        } catch (Exception e) {
            Log.e(TAG, "SIP账户注册失败", e);
            e.printStackTrace();
            if (linphoneCallback != null) {
                linphoneCallback.onRegistrationFailed("注册异常: " + e.getMessage());
            }
        }
    }
    
    // 注销SIP账户
    public void unregisterAccount() {
        try {
            Core core = linphoneManager.getCore();
            Account account = core.getDefaultAccount();
            if (account != null) {
                AccountParams params = account.getParams().clone();
                params.setRegisterEnabled(false);
                account.setParams(params);
            }
        } catch (Exception e) {
            Log.e(TAG, "SIP账户注销失败", e);
        }
    }
    
    // 拨打电话（带视频选项）
    public void makeCall(String destination, boolean withVideo) {
        try {
            Log.i(TAG, "====== 开始拨打电话到: " + destination + " ======");
            
            Core core = linphoneManager.getCore();
            if (core == null) {
                Log.e(TAG, "Core为空，无法拨打电话");
                return;
            }
            
            // 确保初始化音频设备
            linphoneManager.initAudioDevices();
            
            // 请求音频焦点
            requestAudioFocus();
            
            // 打印SIP配置状态信息
            printSipConfigStatus();
            
            // 在打电话前预先收集ICE候选
            ensureICECandidatesCollection(core);
            
            // 配置网络参数，允许视频呼叫
            if (withVideo) {
                // 视频通话时确保视频功能启用
                core.getConfig().setBool("video", "capture", true);
                core.getConfig().setBool("video", "display", true);
            }
            
            // 配置编解码器 - 扩展了更多编解码器支持
            configureFreeswitchCompatibleCodecs(core);
            
            // 创建地址
            Address remoteAddress = createRemoteAddress(destination);
            if (remoteAddress == null) return;
            
            // 创建参数
            CallParams params = core.createCallParams(null);
            if (params != null) {
                // 设置通话参数
                params.setMediaEncryption(MediaEncryption.None); // 不使用加密，提高兼容性
                params.setVideoEnabled(withVideo); // 根据参数启用视频
                params.setAudioEnabled(true); // 确保音频启用
                params.setAudioDirection(MediaDirection.SendRecv);
                params.setEarlyMediaSendingEnabled(true); // 启用早期媒体
                
                // 如果是视频通话，配置视频参数
                if (withVideo) {
                    Log.i(TAG, "配置视频参数");
                    params.setVideoDirection(MediaDirection.SendRecv);
                    params.setLowBandwidthEnabled(false); // 视频通话时不用低带宽模式
                } else {
                    Log.i(TAG, "纯音频通话，不启用视频");
                }
                
                // 自定义头，帮助服务器识别
                params.addCustomHeader("X-FS-Support", "update_display,timer");
                params.addCustomHeader("X-App-Type", "WuyeApp");
                
                // 配置RTP相关参数，增强媒体协商
                core.getConfig().setInt("rtp", "timeout", 30);
                core.getConfig().setBool("rtp", "symmetric", true); // 使用对称RTP
                core.getConfig().setInt("net", "dns_timeout", 15); // 增加DNS解析超时
                
                // 发起呼叫
                Call call = core.inviteAddressWithParams(remoteAddress, params);
                
                if (call != null) {
                    Log.i(TAG, "呼叫请求已发送" + (withVideo ? " (带视频)" : " (纯音频)"));
                    
                    // 如果是视频通话，设置视频输出
                    if (withVideo) {
                        // 确保视频通道已准备好
                        core.getConfig().setBool("video", "automatically_accept", true);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "拨打电话异常", e);
        }
    }
    
    // 确保ICE候选收集完成
    private void ensureICECandidatesCollection(Core core) {
        try {
            // 检查当前NAT策略
            org.linphone.core.NatPolicy natPolicy = core.getNatPolicy();
            if (natPolicy != null) {
                Log.i(TAG, "当前ICE状态: 启用=" + natPolicy.isIceEnabled() + 
                        ", STUN=" + natPolicy.isStunEnabled() +
                        ", 服务器=" + natPolicy.getStunServer());
                
                // 确保STUN和ICE启用
                if (!natPolicy.isStunEnabled() || !natPolicy.isIceEnabled()) {
                    Log.w(TAG, "STUN或ICE未启用，重新配置NAT策略");
                    natPolicy.setStunEnabled(true);
                    natPolicy.setIceEnabled(true);
                    natPolicy.setStunServer("stun:116.198.199.38:3478");
                    core.setNatPolicy(natPolicy);
                }
                
                // 如果STUN服务器不可达，尝试备用服务器
                if (!isStunServerReachable(natPolicy.getStunServer())) {
                    Log.w(TAG, "当前STUN服务器不可达，尝试备用服务器");
                    
                    // 使用中国可访问的备用STUN服务器
                    // 这里可以添加其他中国可访问的STUN服务器
                    String[] backupServers = {
                        "stun:116.198.199.38:3478",
                        "stun:stun.miwifi.com:3478",  // 小米路由器STUN
                        "stun:stun.qq.com:3478"       // 腾讯STUN
                    };
                    
                    for (String server : backupServers) {
                        if (isStunServerReachable(server)) {
                            natPolicy.setStunServer(server);
                            core.setNatPolicy(natPolicy);
                            Log.i(TAG, "已切换到可用的STUN服务器: " + server);
                            break;
                        }
                    }
                }
            }
            
            // 配置RTP相关设置，提高NAT穿透能力
            core.getConfig().setBool("net", "allow_late_ice", true);
            
            Log.i(TAG, "ICE候选收集准备就绪");
        } catch (Exception e) {
            Log.e(TAG, "初始化ICE候选收集失败", e);
        }
    }

    // 检查STUN服务器是否可达
    private boolean isStunServerReachable(String stunServer) {
        if (stunServer == null || stunServer.isEmpty()) {
            return false;
        }
        
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
            
            // 简单的可达性测试
            return address != null; 
        } catch (Exception e) {
            Log.w(TAG, "STUN服务器不可达: " + stunServer + " - " + e.getMessage());
            return false;
        }
    }
    
    // FreeSwitch优化的编解码器配置
    private void configureFreeswitchCompatibleCodecs(Core core) {
        // 配置音频编解码器
        for (PayloadType pt : core.getAudioPayloadTypes()) {
            String mimeType = pt.getMimeType();
            int clockRate = pt.getClockRate();
            
            // 扩展支持的音频编解码器，确保启用所有常用编解码器
            if (mimeType.equals("PCMA") && clockRate == 8000) {
                pt.enable(true);
                Log.d(TAG, "优先启用PCMA编解码器");
            } 
            else if (mimeType.equals("PCMU") && clockRate == 8000) {
                pt.enable(true);
                Log.d(TAG, "启用PCMU编解码器");
            }
            else if (mimeType.equalsIgnoreCase("opus")) {
                pt.enable(true);
                pt.setRecvFmtp("useinbandfec=1;stereo=1");
                Log.d(TAG, "启用opus编解码器");
            }
            else if (mimeType.equalsIgnoreCase("speex")) {
                pt.enable(true);
                if (clockRate == 8000) {
                    pt.setRecvFmtp("vbr=on");
                } else if (clockRate == 16000) {
                    pt.setRecvFmtp("vbr=on");
                } else if (clockRate == 32000) {
                    pt.setRecvFmtp("vbr=on");
                }
                Log.d(TAG, "启用speex编解码器(" + clockRate + ")");
            }
            else if (mimeType.equals("G722")) {
                pt.enable(true);
                Log.d(TAG, "启用G722编解码器");
            }
            else if (mimeType.equals("ILBC")) {
                pt.enable(true);
                pt.setRecvFmtp("mode=30");
                Log.d(TAG, "启用ILBC编解码器");
            }
            else if (mimeType.equals("GSM")) {
                pt.enable(true);
                Log.d(TAG, "启用GSM编解码器");
            }
            else {
                // 其他编解码器也启用但低优先级
                pt.enable(true);
                Log.d(TAG, "保留其他音频编解码器: " + mimeType + "/" + clockRate);
            }
        }
        
        // 配置视频编解码器，确保所有可能用于通话的编解码器都已启用
        for (PayloadType pt : core.getVideoPayloadTypes()) {
            String mimeType = pt.getMimeType();
            
            if ("H264".equals(mimeType)) {
                pt.enable(true);
                pt.setRecvFmtp("profile-level-id=42801F;packetization-mode=1");
                Log.d(TAG, "优先启用H264视频编解码器");
            } 
            else if ("VP8".equals(mimeType)) {
                pt.enable(true);
                pt.setRecvFmtp("max-fs=12288;max-fr=60");
                Log.d(TAG, "启用VP8视频编解码器");
            }
            else if ("AV1".equals(mimeType)) {
                pt.enable(true);
                Log.d(TAG, "启用AV1视频编解码器");
            }
            else {
                // 其他视频编解码器也启用但低优先级
                pt.enable(true);
                Log.d(TAG, "启用其他视频编解码器: " + mimeType);
            }
        }
        
        Log.d(TAG, "完成配置FreeSwitch兼容的编解码器");
    }
    
    // 接听来电
    public void answerCall() {
        answerCall(false);
    }
    
    // 接听来电（带视频选项）
    public void answerCall(boolean withVideo) {
        try {
            Log.i(TAG, "====== 接听来电 ======");
            
            // 请求音频焦点
            requestAudioFocus();
            
            // 初始化音频设备
            checkAndInitializeAudioDevices();
            
            Core core = linphoneManager.getCore();
            if (core == null) {
                Log.e(TAG, "Core为空，无法接听电话");
                return;
            }
            
            Call call = core.getCurrentCall();
            if (call == null) {
                Log.e(TAG, "当前没有来电，无法接听");
                return;
            }
            
            // 创建通话参数 - 修改为直接创建新参数
            CallParams params = core.createCallParams(call);
            if (params != null) {
                // 确保音频流设置正确
                params.setAudioEnabled(true);
                params.setAudioDirection(MediaDirection.SendRecv);
                params.setVideoEnabled(withVideo);
                
                // 使用更兼容的设置
                params.setLowBandwidthEnabled(false);
                
                // 接听电话
                call.acceptWithParams(params);
                Log.i(TAG, "已接听" + (withVideo ? "视频" : "语音") + "通话");
            } else {
                // 如果无法创建参数，使用简单方式接听
                call.accept();
                Log.i(TAG, "已使用默认参数接听通话");
            }
        } catch (Exception e) {
            Log.e(TAG, "接听电话失败", e);
        }
    }
    
    // 检查并初始化音频设备
    private void checkAndInitializeAudioDevices() {
        Core core = linphoneManager.getCore();
        if (core != null) {
            // 检查当前音频设备状态
            AudioDevice inputDevice = core.getInputAudioDevice();
            AudioDevice outputDevice = core.getOutputAudioDevice();
            
            Log.i(TAG, "当前音频状态 - 输入: " + 
                  (inputDevice != null ? inputDevice.getDeviceName() : "未设置") +
                  ", 输出: " + (outputDevice != null ? outputDevice.getDeviceName() : "未设置"));
            
            // 确保麦克风已设置
            if (inputDevice == null) {
                for (AudioDevice device : core.getAudioDevices()) {
                    if (device.getType() == AudioDevice.Type.Microphone) {
                        core.setInputAudioDevice(device);
                        Log.i(TAG, "已设置麦克风: " + device.getDeviceName());
                        break;
                    }
                }
            }
        }
    }
    
    // 路由音频到扬声器
    private void routeAudioToSpeaker() {
        Core core = linphoneManager.getCore();
        if (core != null) {
            for (AudioDevice device : core.getAudioDevices()) {
                if (device.getType() == AudioDevice.Type.Speaker) {
                    core.setOutputAudioDevice(device);
                    Log.i(TAG, "已将音频路由到扬声器");
                    break;
                }
            }
        }
    }
    
    // 路由音频到听筒
    private void routeAudioToEarpiece() {
        Core core = linphoneManager.getCore();
        if (core != null) {
            for (AudioDevice device : core.getAudioDevices()) {
                if (device.getType() == AudioDevice.Type.Earpiece) {
                    core.setOutputAudioDevice(device);
                    Log.i(TAG, "已将音频路由到听筒");
                    break;
                }
            }
        }
    }
    
    // 挂断电话
    public void hangUp() {
        try {
            Core core = linphoneManager.getCore();
            Call call = core.getCurrentCall();
            if (call == null && core.getCallsNb() > 0) {
                call = core.getCalls()[0];
            }
            
            if (call != null) {
                call.terminate();
                Log.i(TAG, "已挂断电话");
            }
        } catch (Exception e) {
            Log.e(TAG, "挂断电话失败", e);
        }
    }
    
    // 设置回调
    public void setLinphoneCallback(LinphoneCallback callback) {
        Log.d(TAG, "设置LinphoneCallback: " + (callback != null ? "非空" : "空"));
        this.linphoneCallback = callback;
    }
    
    // 切换扬声器
    public void toggleSpeaker(boolean enable) {
        try {
            Core core = linphoneManager.getCore();
            AudioDevice.Type audioDeviceType = enable ? AudioDevice.Type.Speaker : AudioDevice.Type.Earpiece;
            
            AudioDevice[] devices = core.getAudioDevices();
            for (AudioDevice device : devices) {
                if (device.getType() == audioDeviceType) {
                    Call call = core.getCurrentCall();
                    if (call != null) {
                        call.setOutputAudioDevice(device);
                        Log.i(TAG, "已" + (enable ? "启用" : "禁用") + "扬声器");
                    }
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "切换扬声器失败", e);
        }
    }
    
    // 切换麦克风静音
    public void toggleMute(boolean mute) {
        try {
            Core core = linphoneManager.getCore();
            core.setMicEnabled(!mute);
            Log.i(TAG, "麦克风已" + (mute ? "静音" : "取消静音"));
        } catch (Exception e) {
            Log.e(TAG, "切换麦克风静音失败", e);
        }
    }
    
    // 处理来电
    void onIncomingCall(Call call) {
        if (linphoneCallback != null) {
            try {
                String caller = call.getRemoteAddress().asStringUriOnly();
                
                // 通知回调
                linphoneCallback.onIncomingCall(call, caller);
                
                // 发送广播通知
                Intent intent = new Intent(this, SipCallReceiver.class);
                intent.setAction("INCOMING_CALL");
                intent.putExtra("caller", caller);
                sendBroadcast(intent);
                
            } catch (Exception e) {
                Log.e(TAG, "处理来电信息失败", e);
            }
        }
    }

    // 专门用于视频通话的方法
    public void makeVideoCall(String destination) {
        try {
            Log.i(TAG, "====== 开始拨打视频电话到: " + destination + " ======");
            
            Core core = linphoneManager.getCore();
            if (core == null) {
                Log.e(TAG, "Core为空，无法拨打视频电话");
                return;
            }
            
            // 启用视频功能
            core.getConfig().setBool("video", "capture", true);
            core.getConfig().setBool("video", "display", true);
            
            // 配置视频自动激活政策
            core.getConfig().setBool("video", "automatically_initiate", true);
            core.getConfig().setBool("video", "automatically_accept", true);
            
            Log.i(TAG, "视频功能已启用，自动接受/发起设置已配置");
            
            // 使用带视频参数拨打电话
            makeCall(destination, true);
            
        } catch (Exception e) {
            Log.e(TAG, "拨打视频电话异常", e);
        }
    }

    // 切换摄像头
    public void switchCamera() {
        try {
            Core core = linphoneManager.getCore();
            
            // 获取当前使用的摄像头
            String currentCameraId = core.getVideoDevice();
            
            // 获取所有摄像头
            String[] devices = core.getVideoDevicesList();
            
            // 查找下一个摄像头
            if (devices.length > 1) {
                String newCameraId = null;
                
                // 找到当前摄像头的下一个
                boolean foundCurrent = false;
                for (String device : devices) {
                    if (foundCurrent) {
                        newCameraId = device;
                        break;
                    }
                    if (device.equals(currentCameraId)) {
                        foundCurrent = true;
                    }
                }
                
                // 如果没有找到下一个，使用第一个
                if (newCameraId == null) {
                    newCameraId = devices[0];
                }
                
                // 设置新摄像头
                Log.i(TAG, "切换摄像头从 " + currentCameraId + " 到 " + newCameraId);
                core.setVideoDevice(newCameraId);
            } else {
                Log.w(TAG, "没有发现多个摄像头设备");
            }
        } catch (Exception e) {
            Log.e(TAG, "切换摄像头失败", e);
        }
    }

    // 设置视频显示表面
    public void setVideoSurfaces(Object localVideoSurface, Object remoteVideoSurface) {
        try {
            Log.i(TAG, "设置视频显示表面");
            Core core = linphoneManager.getCore();
            if (core != null) {
                // 设置视频预览和远程视频窗口
                core.setNativePreviewWindowId(localVideoSurface);
                core.setNativeVideoWindowId(remoteVideoSurface);
                
                // 确保视频功能已启用
                core.getConfig().setBool("video", "capture", true);
                core.getConfig().setBool("video", "display", true);
                
                // 检查当前通话
                Call currentCall = core.getCurrentCall();
                if (currentCall != null) {
                    boolean hasVideo = currentCall.getCurrentParams().isVideoEnabled();
                    Log.i(TAG, "当前通话" + (hasVideo ? "已启用" : "未启用") + "视频");
                    
                    // 如果通话中但没有视频，尝试添加视频
                    if (currentCall.getState() == Call.State.StreamsRunning && !hasVideo) {
                        Log.i(TAG, "尝试向现有通话添加视频");
                        CallParams params = core.createCallParams(currentCall);
                        if (params != null) {
                            params.setVideoEnabled(true);
                            currentCall.update(params);
                        }
                    }
                } else {
                    Log.i(TAG, "没有活动通话，视频窗口将在通话开始时显示");
                }
                
                Log.i(TAG, "视频显示表面设置完成");
            }
        } catch (Exception e) {
            Log.e(TAG, "设置视频表面异常", e);
        }
    }

    // 转接通话
    public void transferCall(String destination) {
        try {
            Core core = linphoneManager.getCore();
            Call currentCall = core.getCurrentCall();
            
            if (currentCall != null) {
                Address address = createRemoteAddress(destination);
                if (address != null) {
                    Log.i(TAG, "正在转接通话到: " + destination);
                    currentCall.transfer(address.asString());
                } else {
                    Log.e(TAG, "创建转接地址失败");
                }
            } else {
                Log.e(TAG, "没有当前通话，无法转接");
            }
        } catch (Exception e) {
            Log.e(TAG, "转接通话失败", e);
        }
    }

    // 保持/恢复通话
    public void toggleHoldCall() {
        try {
            Core core = linphoneManager.getCore();
            Call currentCall = core.getCurrentCall();
            
            if (currentCall != null) {
                if (currentCall.getState() == Call.State.Paused) {
                    Log.i(TAG, "恢复通话");
                    currentCall.resume();
                } else {
                    Log.i(TAG, "保持通话");
                    currentCall.pause();
                }
            } else {
                Log.e(TAG, "没有当前通话，无法保持/恢复");
            }
        } catch (Exception e) {
            Log.e(TAG, "保持/恢复通话失败", e);
        }
    }

    // 合并通话（创建会议）
    public void mergeCallsIntoConference() {
        try {
            Core core = linphoneManager.getCore();
            
            if (core.getCallsNb() > 1) {
                Log.i(TAG, "合并通话到会议");
                core.addAllToConference();
            } else {
                Log.e(TAG, "通话数量不足，无法创建会议");
            }
        } catch (Exception e) {
            Log.e(TAG, "创建会议失败", e);
        }
    }

    // 创建远程地址的方法修改
    private Address createRemoteAddress(String destination) {
        try {
            Core core = linphoneManager.getCore();
            
            // 记录原始目标
            Log.d(TAG, "创建地址, 原始目标: " + destination);
            
            // 检查是否包含域名
            if (!destination.contains("@")) {
                // 从默认账户获取域名
                Account account = core.getDefaultAccount();
                if (account != null) {
                    String domain = account.getParams().getDomain();
                    Log.d(TAG, "目标没有域名, 使用账户域名: " + domain);
                    destination = destination + "@" + domain;
                } else {
                    Log.e(TAG, "没有默认账户，无法获取域名");
                    return null;
                }
            }
            
            // 创建SIP地址
            String sipUri = "sip:" + destination;
            Log.d(TAG, "创建SIP URI: " + sipUri);
            
            Address address = Factory.instance().createAddress(sipUri);
            if (address == null) {
                Log.e(TAG, "创建地址失败，地址为空");
                return null;
            }
            
            Log.d(TAG, "地址创建成功: " + address.asString());
            return address;
        } catch (Exception e) {
            Log.e(TAG, "创建地址异常: " + e.getMessage(), e);
            return null;
        }
    }

    // 在类中添加新方法
    public Core getCore() {
        if (linphoneManager != null) {
            return linphoneManager.getCore();
        }
        return null;
    }

    /**
     * 检查并打印SIP配置状态
     * 用于排查"Not Acceptable Here"问题
     */
    public void printSipConfigStatus() {
        if (linphoneManager == null) {
            Log.e(TAG, "LinphoneManager未初始化，无法检查SIP配置");
            return;
        }
        
        Core core = linphoneManager.getCore();
        if (core == null) {
            Log.e(TAG, "Core未初始化，无法检查SIP配置");
            return;
        }
        
        Log.i(TAG, "========= SIP配置状态检查 =========");
        
        // 检查NAT策略
        org.linphone.core.NatPolicy natPolicy = core.getNatPolicy();
        if (natPolicy != null) {
            Log.i(TAG, "STUN服务器: " + natPolicy.getStunServer());
            // 检查是否配置了STUN服务器
            boolean isStunConfigured = natPolicy.getStunServer() != null && !natPolicy.getStunServer().isEmpty();
            Log.i(TAG, "STUN已配置: " + isStunConfigured);
            
            // 代替直接检查方法
            Log.i(TAG, "NAT策略已设置");
        } else {
            Log.e(TAG, "NAT策略未配置");
        }
        
        // 检查网络状态
        Log.i(TAG, "网络可达状态: " + core.isNetworkReachable());
        
        // 检查SIP账户
        if (core.getDefaultAccount() != null) {
            Account account = core.getDefaultAccount();
            Log.i(TAG, "默认账户: " + account.getParams().getIdentityAddress().asString());
            Log.i(TAG, "注册状态: " + account.getState());
        } else {
            Log.e(TAG, "没有配置默认SIP账户");
        }
        
        // 检查SDP传输地址
        if (core.getDefaultAccount() != null) {
            TransportType transportType = core.getDefaultAccount().getParams().getServerAddress().getTransport();
            Log.i(TAG, "SIP传输协议: " + transportType);
        }
        
        // 检查当前网络接口
        try {
            String localIp = NetworkUtil.getLocalIpAddress();
            Log.i(TAG, "当前设备IP地址: " + localIp);
            Log.i(TAG, "是否为内网IP: " + NetworkUtil.isPrivateIpAddress(localIp));
        } catch (Exception e) {
            Log.e(TAG, "获取网络信息失败", e);
        }
        
        Log.i(TAG, "====================================");
    }

    // 发送DTMF信号
    public void sendDtmf(char digit) {
        try {
            Core core = linphoneManager.getCore();
            Call currentCall = core.getCurrentCall();
            
            if (currentCall != null) {
                Log.i(TAG, "发送DTMF信号: " + digit);
                // 使用当前通话发送DTMF
                currentCall.sendDtmf(digit);
                
                // 同时播放DTMF音（如果API支持）
                core.playDtmf(digit, 200);
            } else {
                Log.e(TAG, "无法发送DTMF，当前无通话");
            }
        } catch (Exception e) {
            Log.e(TAG, "发送DTMF失败", e);
        }
    }

    // 创建NAT策略
    private org.linphone.core.NatPolicy createNatPolicy(Core core) {
        org.linphone.core.NatPolicy natPolicy = core.createNatPolicy();
        natPolicy.setStunEnabled(true);
        natPolicy.setIceEnabled(true);
        natPolicy.setUpnpEnabled(false);
        
        // 从应用设置中获取STUN服务器地址
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String stunServer = preferences.getString("stun_server", "stun:116.198.199.38:3478");
        Log.i(TAG, "使用STUN服务器: " + stunServer);
        
        natPolicy.setStunServer(stunServer); // 使用配置的STUN服务器
        return natPolicy;
    }

    // 请求音频焦点方法
    private void requestAudioFocus() {
        if (audioManager != null && !hasAudioFocus) {
            int result;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                    .build();
                result = audioManager.requestAudioFocus(focusRequest);
            } else {
                result = audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN);
            }
            hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
            Log.d(TAG, "音频焦点请求 " + (hasAudioFocus ? "成功" : "失败"));
        }
    }
} 