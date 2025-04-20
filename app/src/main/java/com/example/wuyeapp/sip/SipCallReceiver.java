package com.example.wuyeapp.sip;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.wuyeapp.R;
import com.example.wuyeapp.ui.call.CallActivity;

public class SipCallReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "incoming_calls";
    private static final int NOTIFICATION_ID = 100;

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("INCOMING_CALL".equals(intent.getAction())) {
            String caller = intent.getStringExtra("caller");
            
            // 创建通知渠道
            createNotificationChannel(context);
            
            // 创建接听来电的Intent
            Intent answerIntent = new Intent(context, CallActivity.class);
            answerIntent.setAction("ANSWER_CALL");
            answerIntent.putExtra("caller", caller);
            PendingIntent answerPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    answerIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // 显示通知
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_call)
                    .setContentTitle("来电")
                    .setContentText("来自: " + caller)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setFullScreenIntent(answerPendingIntent, true)
                    .setContentIntent(answerPendingIntent)
                    .setAutoCancel(true);
            
            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            
            // 直接启动来电界面
            answerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(answerIntent);
        }
    }
    
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "来电通知";
            String description = "显示SIP来电通知";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
