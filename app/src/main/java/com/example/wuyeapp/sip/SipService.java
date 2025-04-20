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

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.TransportConfig;

public class SipService extends Service {
    private static final String TAG = "SipService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "SipServiceChannel";
    
    private Endpoint endpoint;
    private SipAccount account;
    private final IBinder binder = new SipBinder();
    private SipCallback sipCallback;
    private SipCall currentCall;
    
    // 用于绑定服务
    public class SipBinder extends Binder {
        public SipService getService() {
            return SipService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 创建通知渠道（Android 8.0及以上需要）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SIP服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        
        // 创建前台服务通知
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("物业SIP服务")
                .setContentText("SIP服务正在运行")
                .setSmallIcon(R.drawable.ic_call)
                .build();
        
        // 启动前台服务
        startForeground(NOTIFICATION_ID, notification);
        
        try {
            // 初始化PJSIP
            System.loadLibrary("pjsua2");
            endpoint = new Endpoint();
            endpoint.libCreate();
            
            // 配置PJSIP
            EpConfig epConfig = new EpConfig();
            endpoint.libInit(epConfig);
            
            // 创建传输配置
            TransportConfig udpTransport = new TransportConfig();
            udpTransport.setPort(0); // 随机端口
            
            // 添加传输
            endpoint.transportCreate(org.pjsip.pjsua2.pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, udpTransport);
            
            // 启动PJSIP库
            endpoint.libStart();
            
            Log.i(TAG, "PJSIP初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "PJSIP初始化失败", e);
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public void onDestroy() {
        try {
            if (account != null) {
                account.delete();
            }
            endpoint.libDestroy();
        } catch (Exception e) {
            Log.e(TAG, "PJSIP关闭失败", e);
        }
        super.onDestroy();
    }
    
    // 注册SIP账户
    public void registerAccount(String username, String password, String domain) {
        try {
            // 创建账户配置
            AccountConfig accountConfig = new AccountConfig();
            
            // SIP账户地址: "sip:username@domain"
            accountConfig.setIdUri("sip:" + username + "@" + domain);
            
            // 注册信息
            accountConfig.getRegConfig().setRegistrarUri("sip:" + domain);
            
            // 认证信息
            AuthCredInfo cred = new AuthCredInfo("digest", "*", username, 0, password);
            accountConfig.getSipConfig().getAuthCreds().add(cred);
            
            // 创建账户
            account = new SipAccount(this);
            account.create(accountConfig);
            
            Log.i(TAG, "SIP账户注册中: " + username + "@" + domain);
        } catch (Exception e) {
            Log.e(TAG, "SIP账户注册失败", e);
            if (sipCallback != null) {
                sipCallback.onRegistrationFailed(e.getMessage());
            }
        }
    }
    
    // 注销SIP账户
    public void unregisterAccount() {
        try {
            if (account != null) {
                account.setRegistration(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "SIP账户注销失败", e);
        }
    }
    
    // 拨打电话
    public void makeCall(String destination) {
        try {
            if (account != null) {
                SipCall call = new SipCall(account);
                CallOpParam param = new CallOpParam();
                param.setStatusCode(pjsua_status_code.PJSIP_SC_OK);
                
                // 拨打电话 "sip:destination@domain"
                String uri = "sip:" + destination;
                call.makeCall(uri, param);
                
                Log.i(TAG, "拨打电话: " + uri);
            } else {
                Log.e(TAG, "未注册SIP账户，无法拨打电话");
                if (sipCallback != null) {
                    sipCallback.onCallFailed("未注册SIP账户");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "拨打电话失败", e);
            if (sipCallback != null) {
                sipCallback.onCallFailed(e.getMessage());
            }
        }
    }
    
    // 设置回调
    public void setSipCallback(SipCallback callback) {
        this.sipCallback = callback;
    }
    
    // 处理注册结果
    void onRegState(boolean success) {
        if (sipCallback != null) {
            if (success) {
                sipCallback.onRegistrationSuccess();
            } else {
                sipCallback.onRegistrationFailed("注册失败");
            }
        }
    }
    
    // 处理来电
    void onIncomingCall(SipCall call) {
        if (sipCallback != null) {
            try {
                CallInfo info = call.getInfo();
                String caller = info.getRemoteUri();
                
                // 保存当前来电
                this.currentCall = call;
                
                // 通知回调
                sipCallback.onIncomingCall(call, caller);
                
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
