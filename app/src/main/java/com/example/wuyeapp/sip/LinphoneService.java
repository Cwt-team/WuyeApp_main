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

import java.util.List;

import android.content.pm.ServiceInfo;

public class LinphoneService extends Service {
    private static final String TAG = "LinphoneService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "LinphoneServiceChannel";
    
    private LinphoneManager linphoneManager;
    private final IBinder binder = new LocalBinder();
    private LinphoneCallback linphoneCallback;
    
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
        
        // 创建并启动前台服务
        Notification notification = createForegroundNotification();
        startForeground(NOTIFICATION_ID, notification);
        Log.i(TAG, "LinphoneService已启动为前台服务");
        
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
    
    private Notification createForegroundNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("物业SIP服务")
                .setContentText("SIP服务正在运行")
                .setSmallIcon(R.drawable.ic_call)
                .build();
    }
    
    // 注册SIP账户
    public void registerAccount(String username, String password, String domain) {
        try {
            Log.i(TAG, "====== 开始注册SIP账户: " + username + "@" + domain + " ======");
            Core core = linphoneManager.getCore();
            
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
            
            // 设置身份地址
            String sipAddress = "sip:" + username + "@" + domain;
            Log.d(TAG, "设置身份地址: " + sipAddress);
            Address identity = Factory.instance().createAddress(sipAddress);
            accountParams.setIdentityAddress(identity);
            
            // 设置服务器地址
            String serverAddress = "sip:" + domain;
            Log.d(TAG, "设置服务器地址: " + serverAddress);
            Address address = Factory.instance().createAddress(serverAddress);
            
            // 尝试多种传输方式 - 同时支持UDP和TCP
            address.setTransport(TransportType.Udp); // 先尝试UDP
            accountParams.setServerAddress(address);
            
            // 设置NAT策略
            org.linphone.core.NatPolicy natPolicy = core.createNatPolicy();
            natPolicy.setStunEnabled(true); 
            natPolicy.setIceEnabled(true);  
            natPolicy.setStunServer("stun:stun.l.google.com:19302"); // 使用Google的STUN服务器
            accountParams.setNatPolicy(natPolicy);
            
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
    
    // 创建NAT策略
    private org.linphone.core.NatPolicy createNatPolicy(Core core) {
        org.linphone.core.NatPolicy natPolicy = core.createNatPolicy();
        natPolicy.setStunEnabled(false);
        natPolicy.setIceEnabled(false);
        natPolicy.setUpnpEnabled(false);
        return natPolicy;
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
    
    // 拨打电话
    public void makeCall(String destination) {
        makeCall(destination, false);
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
            
            // 优化FreeSwitch专用配置
            core.getConfig().setBool("net", "ice_enabled", false); // 禁用ICE
            
            // 配置编解码器 - FreeSwitch兼容性最好的顺序
            configureFreeswitchCompatibleCodecs(core);
            
            // 创建地址
            Address remoteAddress = createRemoteAddress(destination);
            if (remoteAddress == null) return;
            
            // 创建参数
            CallParams params = core.createCallParams(null);
            if (params != null) {
                // FreeSwitch特别喜欢这些配置
                params.setMediaEncryption(MediaEncryption.None);
                params.setVideoEnabled(false);
                params.setAudioDirection(MediaDirection.SendRecv);
                params.setEarlyMediaSendingEnabled(false); // 改为false试试
                
                // 自定义头，帮助服务器识别
                params.addCustomHeader("X-FS-Support", "update_display,timer");
                params.addCustomHeader("X-App-Type", "WuyeApp");
                
                // 设置更合适的RTP超时值
                core.getConfig().setInt("rtp", "timeout", 30);
                
                // 清除任何先前的NAT策略
                org.linphone.core.NatPolicy natPolicy = core.createNatPolicy();
                natPolicy.setStunEnabled(false);
                natPolicy.setIceEnabled(false);
                
                // 发起呼叫
                Call call = core.inviteAddressWithParams(remoteAddress, params);
                
                if (call != null) {
                    Log.i(TAG, "呼叫请求已发送");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "拨打电话异常", e);
        }
    }
    
    // FreeSwitch优化的编解码器配置
    private void configureFreeswitchCompatibleCodecs(Core core) {
        for (PayloadType pt : core.getAudioPayloadTypes()) {
            String mimeType = pt.getMimeType();
            int clockRate = pt.getClockRate();
            
            // FreeSwitch最兼容的编解码器配置
            if (mimeType.equals("PCMA") && clockRate == 8000) {
                pt.enable(true);
                pt.setRecvFmtp("annexb=no");
                Log.d(TAG, "优先启用PCMA编解码器");
            } 
            else if (mimeType.equals("PCMU") && clockRate == 8000) {
                pt.enable(true);
                Log.d(TAG, "启用PCMU编解码器");
            }
            else {
                // 禁用其他可能造成问题的编解码器
                pt.enable(false);
            }
        }
    }
    
    // 接听来电
    public void answerCall() {
        answerCall(false);
    }
    
    // 接听来电（带视频选项）
    public void answerCall(boolean withVideo) {
        try {
            Log.i(TAG, "====== 开始接听" + (withVideo ? "视频" : "语音") + "来电 ======");
            
            Core core = linphoneManager.getCore();
            if (core == null) {
                Log.e(TAG, "Core为空，无法接听来电");
                return;
            }
            
            Call call = core.getCurrentCall();
            if (call == null) {
                Log.e(TAG, "没有当前来电可接听");
                if (core.getCallsNb() > 0) {
                    Log.d(TAG, "尝试接听第一个通话");
                    call = core.getCalls()[0];
                } else {
                    Log.e(TAG, "没有任何通话可接听");
                    return;
                }
            }
            
            if (call.getState() != Call.State.IncomingReceived) {
                Log.e(TAG, "通话状态不是来电状态, 当前状态: " + call.getState());
                return;
            }
            
            Log.d(TAG, "准备接听来电，Call ID: " + call.getCallLog().getCallId());
            
            CallParams params = core.createCallParams(call);
            if (params == null) {
                Log.e(TAG, "创建呼叫参数失败，尝试直接接听");
                call.accept();
            } else {
                params.setVideoEnabled(withVideo);
                call.acceptWithParams(params);
            }
            
            Log.i(TAG, "已接听来电");
        } catch (Exception e) {
            Log.e(TAG, "接听来电异常", e);
        }
    }
    
    // 挂断电话
    public void hangupCall() {
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

    // 拨打视频电话
    public void makeVideoCall(String destination) {
        makeCall(destination, true);
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

    // 设置视频预览和显示
    public void setVideoSurfaces(Object localVideoSurface, Object remoteVideoSurface) {
        try {
            Core core = linphoneManager.getCore();
            
            // 设置预览窗口（本地摄像头）
            core.setNativePreviewWindowId(localVideoSurface);
            
            // 设置视频窗口（远程视频）
            core.setNativeVideoWindowId(remoteVideoSurface);
            
            Log.i(TAG, "视频窗口已配置");
        } catch (Exception e) {
            Log.e(TAG, "设置视频窗口失败", e);
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

    // 在LinphoneService.java中添加发送DTMF的方法
    public void sendDtmf(char digit) {
        try {
            Core core = linphoneManager.getCore();
            Call currentCall = core.getCurrentCall();
            
            if (currentCall != null) {
                Log.i(TAG, "发送DTMF信号: " + digit);
                currentCall.sendDtmf(digit);
                
                // 同时播放DTMF音 - 为用户提供听觉反馈
                core.playDtmf(digit, 200);
            } else {
                Log.e(TAG, "无法发送DTMF，当前无通话");
            }
        } catch (Exception e) {
            Log.e(TAG, "发送DTMF失败", e);
        }
    }
} 