package com.example.wuyeapp.ui.call;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.wuyeapp.R;
import com.example.wuyeapp.sip.LinphoneSipManager;

public class FloatCallService extends Service {
    private WindowManager windowManager;
    private View floatView;
    private WindowManager.LayoutParams params;
    private Handler timerHandler = new Handler();
    private long callStartTime = 0;
    private Runnable timerRunnable;
    private TextView tvDuration;
    private boolean isMuted = false;
    private boolean isSpeakerOn = false;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        addFloatView();
    }

    private void addFloatView() {
        if (floatView != null) return;
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = 30;
        params.y = 300;

        floatView = LayoutInflater.from(this).inflate(R.layout.float_call_ball, null);
        tvDuration = floatView.findViewById(R.id.tv_float_call_duration);
        ImageButton btnHangup = floatView.findViewById(R.id.btn_float_hangup);
        ImageButton btnMute = floatView.findViewById(R.id.btn_float_mute);
        ImageButton btnSpeaker = floatView.findViewById(R.id.btn_float_speaker);

        btnHangup.setOnClickListener(v -> {
            LinphoneSipManager.getInstance().hangUp();
            stopSelf();
        });
        btnMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            LinphoneSipManager.getInstance().toggleMute(isMuted);
            btnMute.setImageResource(isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic_on);
        });
        btnSpeaker.setOnClickListener(v -> {
            isSpeakerOn = !isSpeakerOn;
            LinphoneSipManager.getInstance().toggleSpeaker(isSpeakerOn);
            // 你可以自定义扬声器icon
        });
        floatView.setOnTouchListener(new View.OnTouchListener() {
            private int lastX, lastY;
            private int paramX, paramY;
            private long downTime;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        paramX = params.x;
                        paramY = params.y;
                        downTime = System.currentTimeMillis();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        params.x = paramX - dx;
                        params.y = paramY + dy;
                        windowManager.updateViewLayout(floatView, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - downTime < 200) {
                            // 点击事件，回到通话界面
                            Intent intent = new Intent(FloatCallService.this, CallActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            stopSelf();
                        }
                        return true;
                }
                return false;
            }
        });
        windowManager.addView(floatView, params);
        // 启动计时器
        callStartTime = System.currentTimeMillis();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long duration = (System.currentTimeMillis() - callStartTime) / 1000;
                String min = String.format("%02d", duration / 60);
                String sec = String.format("%02d", duration % 60);
                tvDuration.setText(min + ":" + sec);
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatView != null) {
            windowManager.removeView(floatView);
            floatView = null;
        }
        timerHandler.removeCallbacksAndMessages(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }
} 