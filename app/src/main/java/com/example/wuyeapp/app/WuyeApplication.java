package com.example.wuyeapp.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import android.os.Build;

import com.example.wuyeapp.utils.LogUtil;
// 删除旧引用
// import com.example.wuyeapp.sip.SipManager;
import com.example.wuyeapp.sip.LinphoneSipManager;
import com.example.wuyeapp.sip.LinphoneCallback;
import com.example.wuyeapp.ui.call.CallActivity;
import com.example.wuyeapp.ui.call.WakeupActivity;
import org.linphone.core.Call;
import org.linphone.core.Factory;
import com.example.wuyeapp.sip.LinphoneService;
// 注释掉有问题的导入语句
// import com.example.wuyeapp.BuildConfig;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

import com.example.wuyeapp.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import android.os.PowerManager;
import android.view.WindowManager;
import android.text.TextUtils;
import android.graphics.Color;
import android.app.AlertDialog;
import android.provider.Settings;
import android.content.SharedPreferences;

import com.example.wuyeapp.network.api.ApiService;
import com.example.wuyeapp.network.api.ShopApiService;
import com.example.wuyeapp.network.api.ShopAuthApiService;
import com.example.wuyeapp.network.client.ApiClientFactory;
import com.example.wuyeapp.session.UnifiedAuthManager;

public class WuyeApplication extends Application {
    private static Context context;
    private static final String TAG = "WuyeApplication";
    
    // 应用前台/后台状态跟踪
    private static boolean isAppInForeground = false;
    private static int activityReferences = 0;
    
    // 来电通知相关常量
    private static final String CHANNEL_ID = "incoming_call_channel";
    private static final int NOTIFICATION_ID = 100;
    
    // 电源锁，用于唤醒屏幕
    // private PowerManager.WakeLock screenWakeLock;
    
    private UnifiedAuthManager authManager;
    private ApiClientFactory apiClientFactory;
    private ApiService apiService;
    private ShopApiService shopApiService;
    private ShopAuthApiService shopAuthService;
    
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        
        // 初始化日志功能
        Log.i("WuyeApp", "应用程序启动，启用详细日志");
        
        // 初始化Linphone Factory
        Factory.instance();
        
        // 首先创建API客户端工厂（不带认证管理器）
        apiClientFactory = new ApiClientFactory(null);
        
        // 创建API服务实例
        apiService = apiClientFactory.createMainApiService();
        shopApiService = apiClientFactory.createShopApiService();
        shopAuthService = apiClientFactory.createShopAuthApiService();
        
        // 初始化认证管理器
        authManager = UnifiedAuthManager.getInstance(this, apiService, shopAuthService);
        
        // 更新ApiClientFactory中的认证管理器
        apiClientFactory.setAuthManager(authManager);
        
        // 其他初始化...
        checkAndRequestNotificationPermissions();
        
        // 检查并申请通知权限、锁屏权限、后台弹窗权限
        checkAndRequestNotificationPermissions();
        
        // 注册Activity生命周期回调
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                LogUtil.i(activity.getClass().getSimpleName() + " - onCreated");
            }

            @Override
            public void onActivityStarted(Activity activity) {
                LogUtil.i(activity.getClass().getSimpleName() + " - onStarted");
                if (++activityReferences == 1) {
                    // 应用进入前台
                    isAppInForeground = true;
                    Log.i(TAG, "应用进入前台状态");
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                LogUtil.i(activity.getClass().getSimpleName() + " - onResumed");
                isAppInForeground = true;
                Log.i(TAG, "Activity已恢复，应用在前台");
                // 新增：检测通知渠道重要性和锁屏权限
                checkNotificationChannelAndLockscreen(activity);
                // 新增：如果有来电且不在来电界面，强制跳转
                try {
                    if (com.example.wuyeapp.sip.LinphoneSipManager.getInstance().hasIncomingCall() &&
                        !com.example.wuyeapp.ui.call.CallActivity.isInCallScreen()) {
                        Log.i(TAG, "检测到有来电且未在来电界面，强制跳转CallActivity");
                        Call call = com.example.wuyeapp.sip.LinphoneSipManager.getInstance().getCurrentIncomingCall();
                        String caller = (call != null && call.getRemoteAddress() != null) ? call.getRemoteAddress().asStringUriOnly() : "未知号码";
                        boolean isVideo = (call != null && call.getRemoteParams() != null) ? call.getRemoteParams().isVideoEnabled() : false;
                        Intent intent = new Intent(context, CallActivity.class);
                        intent.setAction("ANSWER_CALL");
                        intent.putExtra("caller", caller);
                        intent.putExtra("isVideo", isVideo);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "全局检测来电并跳转CallActivity异常", e);
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                LogUtil.i(activity.getClass().getSimpleName() + " - onPaused");
                // 可能要进入后台，但不确定，交给onStop判断
            }

            @Override
            public void onActivityStopped(Activity activity) {
                LogUtil.i(activity.getClass().getSimpleName() + " - onStopped");
                if (--activityReferences == 0) {
                    // 应用进入后台
                    isAppInForeground = false;
                    Log.i(TAG, "应用进入后台状态");
                } else {
                    Log.i(TAG, "Activity已停止，但应用仍在前台, activityReferences=" + activityReferences);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                LogUtil.i(activity.getClass().getSimpleName() + " - onSaveInstanceState");
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                LogUtil.i(activity.getClass().getSimpleName() + " - onDestroyed");
            }
        });
        
        // 初始化来电通知渠道
        createIncomingCallNotificationChannel();
        
        // 初始化LinphoneSipManager
        // LinphoneSipManager.getInstance().init(this); 
        
        // 设置全局来电回调
        setupGlobalCallHandler();
        
        // 如果是小米设备，提示用户设置权限
        if (isMiuiDevice()) {
            Log.i(TAG, "检测到小米设备，建议设置通知和自启动权限");
        }
        
        // 旧SipManager已不再需要，但为了平滑过渡可以保留
        // SipManager.getInstance().init(this);
        
        // 如果是小米设备，提示用户设置权限
        if (isMiuiDevice()) {
            Log.i(TAG, "检测到小米设备，建议设置通知和自启动权限");
        }
    }
    
    // 创建来电通知渠道
    private void createIncomingCallNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(TAG, "创建来电通知渠道: " + CHANNEL_ID);
            
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "来电通知",
                    NotificationManager.IMPORTANCE_HIGH  // 使用HIGH重要性
            );
            
            // 设置来电通知声音
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build();
            
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            channel.setSound(ringtoneUri, audioAttributes);
            
            // 允许亮屏
            channel.setLightColor(Color.RED);
            channel.enableLights(true);
            
            // 允许震动
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            
            // 设置可以显示在锁屏上
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            
            // 绕过勿扰模式
            channel.setBypassDnd(true);
            
            // 设置角标支持（针对部分手机）
            channel.setShowBadge(true);
            
            // 设置渠道描述
            channel.setDescription("用于显示来电信息的重要通知");
            
            // 获取NotificationManager并创建通知渠道
            NotificationManager notificationManager = 
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            
            // 确认通知渠道设置是否成功应用
            NotificationChannel createdChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (createdChannel != null) {
                Log.i(TAG, "通知渠道创建成功，重要性级别: " + createdChannel.getImportance());
                if (createdChannel.getImportance() != NotificationManager.IMPORTANCE_HIGH) {
                    Log.w(TAG, "警告: 通知渠道重要性级别被系统降低");
                }
            } else {
                Log.e(TAG, "错误: 无法创建通知渠道");
            }
            
            // 检查MIUI特定设置
            if (isMiuiDevice()) {
                Log.i(TAG, "检测到MIUI设备，尝试进行额外设置");
                try {
                    // 尝试设置小米特有的锁屏通知权限
                    Intent intent = new Intent();
                    intent.setAction("miui.intent.action.APP_PERM_EDITOR");
                    intent.putExtra("extra_pkgname", getPackageName());
                    intent.setClassName("com.miui.securitycenter", 
                            "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    
                    // 仅检测设置可用性，不直接启动
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        Log.i(TAG, "MIUI权限编辑器可用");
                    } else {
                        Log.i(TAG, "MIUI权限编辑器不可用");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "尝试MIUI特定设置时出错: " + e.getMessage());
                }
            }
        }
    }
    
    // 设置全局来电处理
    public void setupGlobalCallHandler() {
        LinphoneSipManager.getInstance().setLinphoneCallback(new LinphoneCallback() {
            @Override
            public void onRegistrationSuccess() {
                Log.i(TAG, "全局SIP注册成功");
            }

            @Override
            public void onRegistrationFailed(String reason) {
                Log.e(TAG, "全局SIP注册失败: " + reason);
            }

            @Override
            public void onIncomingCall(Call call, String caller) {
                Log.i(TAG, "全局收到来电: " + caller);
                // 处理来电
                handleIncomingCall(caller, call);
            }

            @Override
            public void onCallProgress() {
                // 不需要全局处理
            }

            @Override
            public void onCallEstablished() {
                // 不需要全局处理
            }

            @Override
            public void onCallEnded() {
                // 不需要全局处理
            }

            @Override
            public void onCallFailed(String reason) {
                // 不需要全局处理
            }
        });
    }
    
    // 处理来电的方法
    private void handleIncomingCall(String caller, Call call) {
        Log.i(TAG, "处理来电: " + caller + ", 应用前台状态: " + isAppInForeground);
        try {
            if (isAppInForeground) {
                // 应用在前台，直接拉起来电页面，不发通知
                Log.i(TAG, "App在前台，直接跳转CallActivity");
                // 防止重复弹出通话界面
                if (com.example.wuyeapp.ui.call.CallActivity.isInCallScreen()) {
                    Log.i(TAG, "已在通话界面，忽略重复弹窗");
                    return;
                }
                Intent directCallIntent = new Intent(this, CallActivity.class);
                directCallIntent.setAction("ANSWER_CALL");
                directCallIntent.putExtra("caller", caller);
                boolean isVideoCall = false;
                if (call != null && call.getRemoteParams() != null) {
                    isVideoCall = call.getRemoteParams().isVideoEnabled();
                    directCallIntent.putExtra("isVideo", isVideoCall);
                }
                directCallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                             Intent.FLAG_ACTIVITY_CLEAR_TOP |
                             Intent.FLAG_ACTIVITY_SINGLE_TOP |
                             Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(directCallIntent);
            } else {
                // 应用在后台或息屏，亮屏+发全屏通知兜底
                Log.i(TAG, "App在后台或息屏，亮屏+发全屏通知");
                for (int i = 0; i < 2; i++) {
                    Log.i(TAG, "第" + (i+1) + "次调用wakeupDevice()尝试亮屏");
                    wakeupDevice();
                }
                com.example.wuyeapp.utils.NotificationHelper.showIncomingCallNotification(this, caller, (call != null && call.getRemoteParams() != null) ? call.getRemoteParams().isVideoEnabled() : false);
            }
        } catch (Exception e) {
            Log.e(TAG, "处理来电时出错: " + e.getMessage(), e);
        }
    }
    
    // 唤醒设备屏幕
    private void wakeupDevice() {
        Log.i(TAG, "wakeupDevice()被调用，尝试唤醒设备屏幕");
        try {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "WuyeApp:IncomingCallWakeLock");
            wakeLock.acquire(10*1000); // 10秒
            Log.i(TAG, "PowerManager.WakeLock获取成功，强制屏幕点亮10秒");
            // 启动WakeupActivity辅助亮屏
            Intent intent = new Intent(this, com.example.wuyeapp.ui.call.WakeupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
            Log.i(TAG, "WakeupActivity已启动");
        } catch (Exception e) {
            Log.e(TAG, "唤醒设备屏幕时出错: " + e.getMessage(), e);
        }
    }
    
    // 释放唤醒锁
    private void releaseWakeLock() {
        // 我们现在不需要释放唤醒锁，因为我们在wakeupDevice方法中设置了超时时间
        // PowerManager.WakeLock已经会自动释放
        Log.i(TAG, "不需要手动释放唤醒锁，已设置超时自动释放");
    }
    
    // 显示来电系统通知
    private void showIncomingCallNotification(String caller, Call call) {
        Log.i(TAG, "显示系统通知: " + caller);
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        // 检查是否有通知权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationManager.areNotificationsEnabled()) {
                Log.e(TAG, "没有通知权限！请在系统设置中开启");
                return;
            }
        }
        
        try {
            // 创建接听按钮的Intent
            Intent answerIntent = new Intent(this, CallActivity.class);
            answerIntent.setAction("ANSWER_CALL");
            answerIntent.putExtra("caller", caller);
            
            // 检查是否为视频通话
            if (call != null && call.getRemoteParams() != null) {
                boolean isVideoCall = call.getRemoteParams().isVideoEnabled();
                answerIntent.putExtra("isVideo", isVideoCall);
            }
            
            // 确保这个Intent会唤醒设备并在锁屏上显示
            answerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                                 Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                                 Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            
            PendingIntent answerPendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    answerIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // 创建拒接按钮的Intent
            Intent declineIntent = new Intent(this, LinphoneSipManager.CallDeclineReceiver.class);
            declineIntent.setAction("DECLINE_CALL");
            
            PendingIntent declinePendingIntent = PendingIntent.getBroadcast(
                    this,
                    1,
                    declineIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // 构建高优先级通知
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("来电")
                    .setContentText("来自 " + caller + " 的呼叫")
                    .setSmallIcon(R.drawable.ic_call)
                    .setPriority(NotificationCompat.PRIORITY_MAX) // 最高优先级
                    .setCategory(NotificationCompat.CATEGORY_CALL) // 来电类别
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 在锁屏上完全可见
                    .setFullScreenIntent(answerPendingIntent, true) // 使用全屏意图显示
                    .addAction(R.drawable.ic_call, "接听", answerPendingIntent)
                    .addAction(R.drawable.ic_call_end, "拒接", declinePendingIntent)
                    .setOngoing(true) // 持续通知，不可滑动删除
                    .setAutoCancel(false) // 不自动取消
                    .setTimeoutAfter(45000); // 45秒后自动关闭，与来电超时一致
                    
            // 确保通知有声音、震动等
            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
            
            // 设置通知强制声音（即使在静音模式下）
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
            
            // 尝试使用小米特定的通知增强
            addXiaomiNotificationEnhancement(builder);
            
            // 显示通知
            Notification notification = builder.build();
            Log.i(TAG, "显示通知ID: " + NOTIFICATION_ID);
            
            // 强制使用FLAG_INSISTENT标志使通知持续提示
            notification.flags |= Notification.FLAG_INSISTENT;
            
            // 对于小米设备，尝试应用特殊处理
            if (isMiuiDevice()) {
                try {
                    // 尝试使用反射设置更多小米特定标志
                    Field field = notification.getClass().getDeclaredField("extraNotification");
                    Object extraNotification = field.get(notification);
                    Method method = extraNotification.getClass().getDeclaredMethod("setFloatEnable", boolean.class);
                    method.invoke(extraNotification, true); // 启用浮动通知
                    
                    // 尝试设置为重要通知
                    Method methodPriority = extraNotification.getClass().getDeclaredMethod("setImportance", int.class);
                    methodPriority.invoke(extraNotification, 5); // 5为最高重要性级别
                    
                    Log.i(TAG, "成功应用MIUI特定通知设置");
                } catch (Exception e) {
                    Log.e(TAG, "设置MIUI特定通知属性失败: " + e.getMessage());
                }
                
                // 尝试直接启动永久通知服务
                try {
                    // 启动前台服务确保通知显示
                    Intent serviceIntent = new Intent(this, LinphoneService.class);
                    serviceIntent.putExtra("SHOW_CALL_NOTIFICATION", true);
                    serviceIntent.putExtra("CALLER", caller);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent);
                    } else {
                        startService(serviceIntent);
                    }
                    Log.i(TAG, "已启动前台服务以确保通知显示");
                } catch (Exception e) {
                    Log.e(TAG, "无法启动前台服务: " + e.getMessage());
                }
            }
            
            // 显示通知
            notificationManager.notify(NOTIFICATION_ID, notification);
            Log.i(TAG, "通知已提交给系统，检查是否有问题");
            
            // 检查通知是否被系统屏蔽
            checkNotificationStatus();
            
            // 确保铃声能够播放，即使在静音模式下
            try {
                playRingtoneForCall();
            } catch (Exception e) {
                Log.e(TAG, "播放铃声失败: " + e.getMessage());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "创建或显示通知时出错: " + e.getMessage(), e);
        }
    }
    
    // 检查通知状态
    private void checkNotificationStatus() {
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 检查通知渠道
            NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (channel != null) {
                Log.i(TAG, "通知渠道状态: " + 
                        "重要性=" + channel.getImportance() + 
                        ", 声音=" + (channel.getSound() != null) +
                        ", 锁屏可见性=" + channel.getLockscreenVisibility());
                
                if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                    Log.e(TAG, "通知渠道已被禁用!");
                }
            } else {
                Log.e(TAG, "通知渠道不存在!");
            }
        }
        
        // 检查应用的通知权限
        boolean enabled = notificationManager.areNotificationsEnabled();
        Log.i(TAG, "应用通知权限状态: " + (enabled ? "已启用" : "已禁用"));
        
        // 检查电源管理限制
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(getPackageName());
            Log.i(TAG, "电池优化状态: " + (isIgnoringBatteryOptimizations ? "已忽略优化" : "受到优化限制"));
        }
    }
    
    // 强制播放铃声，即使在静音模式下
    private void playRingtoneForCall() {
        try {
            // 获取当前AudioManager
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
            int ringerMode = audioManager.getRingerMode();
            
            Log.i(TAG, "当前铃声音量: " + currentVolume + "/" + maxVolume + 
                    ", 铃声模式: " + ringerMode + 
                    " (0=静音, 1=震动, 2=正常)");
            
            // 尝试强制临时提高音量
            if (currentVolume < maxVolume/2) {
                try {
                    // 临时提高音量
                    audioManager.setStreamVolume(
                            AudioManager.STREAM_RING, 
                            maxVolume, 
                            0);
                    Log.i(TAG, "临时提高音量至: " + maxVolume);
                } catch (Exception e) {
                    Log.e(TAG, "无法提高音量: " + e.getMessage());
                }
            }
            
            // 获取默认铃声
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone.setVolume(1.0f); // 最大音量
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AudioAttributes attributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();
                    ringtone.setAudioAttributes(attributes);
                }
                
                Log.i(TAG, "开始播放铃声");
                ringtone.play();
            }
        } catch (Exception e) {
            Log.e(TAG, "播放铃声时出错: " + e.getMessage(), e);
        }
    }
    
    // 添加小米特定的通知增强功能
    private void addXiaomiNotificationEnhancement(NotificationCompat.Builder builder) {
        try {
            // 对小米设备应用特殊处理
            if (isMiuiDevice()) {
                Log.i(TAG, "检测到MIUI设备，应用特殊通知增强");
                
                // 尝试设置"lockscreen_info"额外参数让通知在锁屏上显示
                builder.getExtras().putString("lockscreen_info", "show");
                
                // 尝试设置显式铃声
                builder.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
                    AudioManager.STREAM_RING
                );
                
                // 设置最大优先级
                builder.setPriority(NotificationCompat.PRIORITY_MAX);
                
                // 设置自定义小米标志
                try {
                    Field field = builder.getClass().getDeclaredField("mMiuiFlags");
                    field.setAccessible(true);
                    int miuiFlags = field.getInt(builder);
                    miuiFlags |= 0x00000020; // MIUI_FORCE_SHOW_IN_STATUS_BAR
                    miuiFlags |= 0x00000040; // MIUI_HEADS_UP_MODE
                    field.setInt(builder, miuiFlags);
                    Log.i(TAG, "成功设置MIUI特定通知标志");
                } catch (Exception e) {
                    Log.e(TAG, "无法设置MIUI特定通知标志: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "应用小米通知增强时出错: " + e.getMessage(), e);
        }
    }
    
    // 检测是否为MIUI设备
    private boolean isMiuiDevice() {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name", ""));
    }
    
    // 获取系统属性
    private String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getMethod("get", String.class, String.class);
            return (String) method.invoke(null, key, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "获取系统属性失败: " + e.getMessage());
            return defaultValue;
        }
    }

    @Override
    public void onTerminate() {
        // 释放唤醒锁
        releaseWakeLock();
        
        // 释放LinphoneSipManager资源
        LinphoneSipManager.getInstance().release();
        
        // 旧SipManager已不再需要，但为了平滑过渡可以保留
        // SipManager.getInstance().release();
        
        super.onTerminate();
    }

    public static Context getContext() {
        return context;
    }
    
    // 获取应用前台/后台状态
    public static boolean isAppInForeground() {
        return isAppInForeground;
    }

    private void requestMiuiPermissions() {
        Intent intent = new Intent();
        intent.setAction("miui.intent.action.APP_PERM_EDITOR");
        intent.putExtra("extra_pkgname", getPackageName());
        intent.setClassName("com.miui.securitycenter", 
            "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
            Log.i(TAG, "启动MIUI权限设置页面");
        } catch (Exception e) {
            Log.e(TAG, "无法启动MIUI权限设置", e);
        }
    }

    // 检查并申请通知权限、锁屏权限、后台弹窗权限
    private void checkAndRequestNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (!nm.areNotificationsEnabled()) {
                Log.w(TAG, "通知权限未开启，可能影响来电提醒");
                // 引导用户开启通知权限
                Intent intent = new Intent();
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
        // 检查锁屏显示权限（部分ROM）
        // 检查后台弹窗/自启动权限（国产ROM）
        // 可根据ROM类型引导用户进入设置页面
    }

    // 检查通知渠道重要性和锁屏通知权限，不达标则弹窗引导
    private void checkNotificationChannelAndLockscreen(Activity activity) {
        try {
            // 新增：只弹一次
            SharedPreferences sp = activity.getSharedPreferences("notify_guide", MODE_PRIVATE);
            if (sp.getBoolean("notified_once", false)) return;
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = nm.getNotificationChannel("call_channel");
                if (channel != null) {
                    if (channel.getImportance() < NotificationManager.IMPORTANCE_HIGH || channel.getLockscreenVisibility() != Notification.VISIBILITY_PUBLIC) {
                        showNotificationChannelGuideDialog(activity, sp);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "检测通知渠道重要性失败", e);
        }
    }

    // 弹窗引导用户设置通知渠道为重要并允许锁屏显示（只弹一次）
    private void showNotificationChannelGuideDialog(Activity activity, SharedPreferences sp) {
        new AlertDialog.Builder(activity)
            .setTitle("重要提示")
            .setMessage("为保证来电能在锁屏弹窗，请将『来电通知』渠道设置为'重要'并允许锁屏显示。点击『去设置』后，找到『来电通知』并设置为'重要'，锁屏显示为'显示所有通知内容'。")
            .setPositiveButton("去设置", (dialog, which) -> {
                sp.edit().putBoolean("notified_once", true).apply();
                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                } else {
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            })
            .setNegativeButton("取消", (dialog, which) -> {
                sp.edit().putBoolean("notified_once", true).apply();
            })
            .setCancelable(true)
            .show();
    }

    public UnifiedAuthManager getAuthManager() {
        if (authManager == null) {
            if (apiClientFactory == null) {
                apiClientFactory = new ApiClientFactory(null);
            }
            if (apiService == null) {
                apiService = apiClientFactory.createMainApiService();
            }
            if (shopAuthService == null) {
                shopAuthService = apiClientFactory.createShopAuthApiService();
            }
            authManager = UnifiedAuthManager.getInstance(this, apiService, shopAuthService);
            apiClientFactory.setAuthManager(authManager);
        }
        return authManager;
    }

    public ApiService getApiService() {
        if (apiService == null) {
            if (apiClientFactory == null) {
                apiClientFactory = new ApiClientFactory(null);
            }
            apiService = apiClientFactory.createMainApiService();
        }
        return apiService;
    }

    public ShopApiService getShopApiService() {
        if (shopApiService == null) {
            if (apiClientFactory == null) {
                apiClientFactory = new ApiClientFactory(null);
            }
            shopApiService = apiClientFactory.createShopApiService();
        }
        return shopApiService;
    }

    public ShopAuthApiService getShopAuthService() {
        if (shopAuthService == null) {
            if (apiClientFactory == null) {
                apiClientFactory = new ApiClientFactory(null);
            }
            shopAuthService = apiClientFactory.createShopAuthApiService();
        }
        return shopAuthService;
    }
} 