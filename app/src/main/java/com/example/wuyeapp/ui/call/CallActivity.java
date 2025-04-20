package com.example.wuyeapp.ui.call;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.databinding.ActivityCallBinding;
import com.example.wuyeapp.sip.SipCall;
import com.example.wuyeapp.sip.SipCallback;
import com.example.wuyeapp.sip.SipService;
import com.example.wuyeapp.sip.SipManager;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CallActivity extends AppCompatActivity implements SipCallback {

    private ActivityCallBinding binding;
    private SipService sipService;
    private boolean isBound = false;
    private SipCall currentCall;
    
    private boolean isMuted = false;
    private boolean isSpeakerOn = false;
    
    private long callStartTime = 0;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            updateCallDuration();
            timerHandler.postDelayed(this, 1000);
        }
    };
    
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SipService.SipBinder binder = (SipService.SipBinder) service;
            sipService = binder.getService();
            sipService.setSipCallback(CallActivity.this);
            isBound = true;
            
            // 检查是接听来电还是拨打电话
            processIntent(getIntent());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            sipService = null;
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 设置SIP回调
        SipManager.getInstance().setSipCallback(this);
        
        // 设置按钮监听器
        setupButtonListeners();
        
        // 处理Intent
        processIntent(getIntent());
    }
    
    private void setupButtonListeners() {
        // 挂断按钮
        binding.btnHangup.setOnClickListener(v -> {
            if (currentCall != null) {
                currentCall.hangup();
            }
            finish();
        });
        
        // 麦克风静音按钮
        binding.btnMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            // 实际应用中需要实现静音功能
            // currentCall.setMicrophoneMute(isMuted);
            
            // 更新UI
            // binding.btnMute.setImageResource(isMuted ? R.drawable.ic_mic_on : R.drawable.ic_mic_off);
            Toast.makeText(this, isMuted ? "麦克风已静音" : "麦克风已取消静音", Toast.LENGTH_SHORT).show();
        });
        
        // 扬声器按钮
        binding.btnSpeaker.setOnClickListener(v -> {
            isSpeakerOn = !isSpeakerOn;
            // 实际应用中需要实现扬声器切换功能
            // currentCall.setSpeakerphoneOn(isSpeakerOn);
            
            // 更新UI
            // binding.btnSpeaker.setImageResource(isSpeakerOn ? R.drawable.ic_volume_off : R.drawable.ic_volume_up);
            Toast.makeText(this, isSpeakerOn ? "扬声器已开启" : "扬声器已关闭", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void processIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if ("MAKE_CALL".equals(action)) {
                // 拨打电话
                String number = intent.getStringExtra("number");
                if (number != null && !number.isEmpty()) {
                    binding.tvCallState.setText("正在拨打...");
                    binding.tvCallerId.setText(number);
                    SipManager.getInstance().makeCall(number);
                } else {
                    Toast.makeText(this, "无效号码", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else if ("ANSWER_CALL".equals(action)) {
                // 接听来电
                binding.tvCallState.setText("正在接通...");
                binding.tvCallerId.setText(intent.getStringExtra("caller"));
            }
        }
    }
    
    private void startCallTimer() {
        callStartTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }
    
    private void stopCallTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }
    
    private void updateCallDuration() {
        if (callStartTime > 0) {
            long durationMillis = System.currentTimeMillis() - callStartTime;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60;
            
            String durationText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            binding.tvCallDuration.setText(durationText);
        }
    }
    
    @Override
    protected void onDestroy() {
        stopCallTimer();
        
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        
        if (currentCall != null) {
            // 确保通话结束
            try {
                currentCall.hangup();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        
        super.onDestroy();
    }
    
    // SipCallback 接口实现
    @Override
    public void onRegistrationSuccess() {
        // 不需要处理
    }
    
    @Override
    public void onRegistrationFailed(String reason) {
        // 不需要处理
    }
    
    @Override
    public void onIncomingCall(SipCall call, String caller) {
        // 不应该在通话界面再接到其他来电
        // 实际应用中，您可能需要处理在通话过程中接到新来电的情况
    }
    
    @Override
    public void onCallFailed(String reason) {
        runOnUiThread(() -> {
            Toast.makeText(this, "通话失败: " + reason, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
    
    @Override
    public void onCallEstablished() {
        runOnUiThread(() -> {
            binding.tvCallState.setText("通话中");
            startCallTimer();
        });
    }
    
    @Override
    public void onCallEnded() {
        runOnUiThread(() -> {
            Toast.makeText(this, "通话已结束", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
