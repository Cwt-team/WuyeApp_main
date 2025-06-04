package com.example.wuyeapp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.wuyeapp.R;
import com.example.wuyeapp.ui.call.CallActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID = "call_channel";
    private static final String CHANNEL_NAME = "来电通知";
    private static final int NOTIFY_ID = 1001;

    public static void showIncomingCallNotification(Context context, String caller, boolean isVideo) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // 创建通知渠道（Android 8.0+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("SIP来电提醒");
            channel.enableVibration(true);
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), null);
            nm.createNotificationChannel(channel);
        }
        // 跳转到通话界面
        Intent intent = new Intent(context, CallActivity.class);
        intent.setAction("ANSWER_CALL");
        intent.putExtra("caller", caller);
        intent.putExtra("isVideo", isVideo);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_call)
                .setContentTitle("有新来电")
                .setContentText("来自: " + caller)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setFullScreenIntent(pi, true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
                .setVibrate(new long[]{0, 500, 500, 500})
                .setContentIntent(pi);
        // 兼容国产ROM，强制Heads-up
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        nm.notify(NOTIFY_ID, builder.build());
    }
    public static void cancelIncomingCallNotification(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFY_ID);
    }
} 