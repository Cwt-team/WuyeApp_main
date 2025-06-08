package com.example.wuyeapp.sip;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.wuyeapp.R;
import com.example.wuyeapp.ui.call.CallActivity;

public class SipCallReceiver extends BroadcastReceiver {
    private static final String TAG = "SipCallReceiver";
    private static final String CHANNEL_ID = "IncomingCallChannel";
    private static final int NOTIFICATION_ID = 100;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if ("INCOMING_CALL".equals(action)) {
            String caller = intent.getStringExtra("caller");
            Log.i(TAG, "收到来电通知: " + caller);
            
            // 显示来电通知
            showIncomingCallNotification(context, caller);
            
            // 防止重复弹出通话界面
            if (com.example.wuyeapp.ui.call.CallActivity.isInCallScreen()) {
                Log.i(TAG, "已在通话界面，忽略重复弹窗");
                return;
            }
            
            // 直接启动来电界面，添加标志以确保从任何界面都能正常启动
            Intent callIntent = new Intent(context, CallActivity.class);
            callIntent.setAction("ANSWER_CALL");
            callIntent.putExtra("caller", caller);
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(callIntent);
        }
    }
    
    private void showIncomingCallNotification(Context context, String caller) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // 创建通知渠道（Android 8.0及以上需要）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "来电通知",
                    NotificationManager.IMPORTANCE_HIGH
            );
            
            // 设置来电通知声音
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build();
            
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            channel.setSound(ringtoneUri, audioAttributes);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            
            notificationManager.createNotificationChannel(channel);
        }
        
        // 创建接听按钮的Intent
        Intent answerIntent = new Intent(context, CallActivity.class);
        answerIntent.setAction("ANSWER_CALL");
        answerIntent.putExtra("caller", caller);
        PendingIntent answerPendingIntent = PendingIntent.getActivity(
                context,
                0,
                answerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 创建拒接按钮的Intent
        Intent declineIntent = new Intent(context, LinphoneSipManager.class);
        declineIntent.setAction("DECLINE_CALL");
        PendingIntent declinePendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 构建通知
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("来电")
                .setContentText("来自 " + caller + " 的呼叫")
                .setSmallIcon(R.drawable.ic_call)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(answerPendingIntent, true) // 全屏显示
                .addAction(R.drawable.ic_call, "接听", answerPendingIntent)
                .addAction(R.drawable.ic_call_end, "拒接", declinePendingIntent)
                .setAutoCancel(true)
                .build();
        
        // 显示通知
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
