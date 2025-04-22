package com.example.wuyeapp.sip.linphone;

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
import android.content.pm.ServiceInfo;

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
        
        // 创建通知渠道
        createNotificationChannel();
        
        // 创建并启动前台服务
        Notification notification = createForegroundNotification();
        
        // 修改这里，添加 FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK 或 FOREGROUND_SERVICE_TYPE_PHONE_CALL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
        
        // 初始化LinphoneManager
        linphoneManager = LinphoneManager.getInstance(getApplicationContext());
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
            Core core = linphoneManager.getCore();
            
            // 创建认证信息
            AuthInfo authInfo = Factory.instance().createAuthInfo(
                    username, null, password, null, null, domain, null
            );
            
            // 创建账户参数
            AccountParams accountParams = core.createAccountParams();
            
            // 设置SIP地址
            String sipAddress = "sip:" + username + "@" + domain;
            Address identity = Factory.instance().createAddress(sipAddress);
            accountParams.setIdentityAddress(identity);
            
            // 设置服务器地址
            Address address = Factory.instance().createAddress("sip:" + domain);
            address.setTransport(TransportType.Udp);
            accountParams.setServerAddress(address);
            
            // 启用注册
            accountParams.setRegisterEnabled(true);
            
            // 创建账户
            Account account = core.createAccount(accountParams);
            
            // 添加认证信息和账户
            core.addAuthInfo(authInfo);
            core.addAccount(account);
            
            // 设置为默认账户
            core.setDefaultAccount(account);
            
            Log.i(TAG, "SIP账户注册中: " + username + "@" + domain);
        } catch (Exception e) {
            Log.e(TAG, "SIP账户注册失败", e);
            if (linphoneCallback != null) {
                linphoneCallback.onRegistrationFailed(e.getMessage());
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
        this.linphoneCallback = callback;
        linphoneManager.setListener(callback);
    }
    
    // 切换扬声器
    public void toggleSpeaker(boolean enable) {
        try {
            Core core = linphoneManager.getCore();
            AudioDevice.Type audioDeviceType = enable ? AudioDevice.Type.Speaker : AudioDevice.Type.Earpiece;
            
            AudioDevice[] audioDevices = core.getAudioDevices();
            for (AudioDevice device : audioDevices) {
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
} 