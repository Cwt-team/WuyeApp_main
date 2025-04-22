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
            Log.d(TAG, "开始注册SIP账户: " + username + "@" + domain);
            Core core = linphoneManager.getCore();
            
            // 首先清除现有账户
            Log.d(TAG, "清除现有账户和认证信息");
            for (Account account : core.getAccountList()) {
                core.removeAccount(account);
            }
            
            // 清除现有认证信息
            for (AuthInfo authInfo : core.getAuthInfoList()) {
                core.removeAuthInfo(authInfo);
            }
            
            // 创建认证信息
            Log.d(TAG, "创建新的认证信息: 用户名=" + username + ", 密码长度=" + password.length() + ", 域=" + domain);
            AuthInfo authInfo = Factory.instance().createAuthInfo(
                    username,       // 用户名
                    username,       // 认证用户名
                    password,       // 密码
                    null,           // ha1
                    null,           // realm
                    domain          // 域
            );
            
            // 创建账户参数
            Log.d(TAG, "创建账户参数");
            AccountParams accountParams = core.createAccountParams();
            
            // 设置SIP地址
            String sipAddress = "sip:" + username + "@" + domain;
            Log.d(TAG, "设置SIP身份地址: " + sipAddress);
            Address identity = Factory.instance().createAddress(sipAddress);
            identity.setDisplayName(username);
            accountParams.setIdentityAddress(identity);
            
            // 设置服务器地址
            String serverAddress = "sip:" + domain + ";transport=udp";
            Log.d(TAG, "设置SIP服务器地址: " + serverAddress);
            Address address = Factory.instance().createAddress(serverAddress);
            accountParams.setServerAddress(address);
            
            // 启用注册并设置期限
            accountParams.setRegisterEnabled(true);
            accountParams.setExpires(3600);
            Log.d(TAG, "注册期限设置为: 3600秒");
            
            // 创建账户
            Log.d(TAG, "创建SIP账户");
            Account account = core.createAccount(accountParams);
            
            // 添加认证信息和账户
            Log.d(TAG, "添加认证信息和账户到Core");
            core.addAuthInfo(authInfo);
            core.addAccount(account);
            
            // 设置为默认账户
            Log.d(TAG, "设置为默认账户");
            core.setDefaultAccount(account);
            
            // 添加更多核心配置日志
            Log.d(TAG, "当前Core配置:");
            Log.d(TAG, "网络状态: " + (core.isNetworkReachable() ? "可达" : "不可达"));
            Log.d(TAG, "验证策略: " + core.getPrimaryContactParsed().asString());
            Log.d(TAG, "当前传输配置: " + core.getTransports().toString());
            
            Log.i(TAG, "SIP账户注册请求已发送: " + username + "@" + domain);
        } catch (Exception e) {
            Log.e(TAG, "SIP账户注册失败", e);
            e.printStackTrace(); // 打印详细堆栈
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
    
    // 拨打电话
    public void makeCall(String destination) {
        makeCall(destination, false);
    }
    
    // 拨打电话（带视频选项）
    public void makeCall(String destination, boolean withVideo) {
        try {
            Core core = linphoneManager.getCore();
            Account account = core.getDefaultAccount();
            
            if (account != null) {
                String domain = account.getParams().getDomain();
                
                // 创建通话参数
                CallParams params = core.createCallParams(null);
                params.setMediaEncryption(MediaEncryption.None);
                params.setVideoEnabled(withVideo);
                
                // 创建远程地址
                String remoteSipUri = "sip:" + destination + "@" + domain;
                Address remoteAddress = Factory.instance().createAddress(remoteSipUri);
                
                // 发起呼叫
                core.inviteAddressWithParams(remoteAddress, params);
                Log.i(TAG, "拨打电话: " + remoteSipUri + ", 视频: " + withVideo);
            } else {
                Log.e(TAG, "未注册SIP账户，无法拨打电话");
                if (linphoneCallback != null) {
                    linphoneCallback.onCallFailed("未注册SIP账户");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "拨打电话失败", e);
            if (linphoneCallback != null) {
                linphoneCallback.onCallFailed(e.getMessage());
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
            Core core = linphoneManager.getCore();
            Call call = core.getCurrentCall();
            if (call != null) {
                CallParams params = core.createCallParams(call);
                params.setVideoEnabled(withVideo);
                call.acceptWithParams(params);
                Log.i(TAG, "已接听来电");
            }
        } catch (Exception e) {
            Log.e(TAG, "接听来电失败", e);
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
} 