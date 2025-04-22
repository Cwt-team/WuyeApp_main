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
import com.example.wuyeapp.sip.LinphoneSipManager;
import com.example.wuyeapp.sip.LinphoneService;
import com.example.wuyeapp.sip.LinphoneCallback;
import org.linphone.core.Call;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CallActivity extends AppCompatActivity implements SipCallback {

    private ActivityCallBinding binding;
    private LinphoneService linphoneService;
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
            LinphoneService.LocalBinder binder = (LinphoneService.LocalBinder) service;
            linphoneService = binder.getService();
            
            // 设置回调
            linphoneService.setLinphoneCallback(new LinphoneCallback() {
                @Override
                public void onRegistrationSuccess() {
                    // 不需要处理
                }
                
                @Override
                public void onRegistrationFailed(String reason) {
                    // 不需要处理
                }
                
                @Override
                public void onIncomingCall(Call call, String caller) {
                    // 不应该在通话界面再接到其他来电
                }
                
                @Override
                public void onCallProgress() {
                    // 呼叫进行中
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
                        Toast.makeText(CallActivity.this, "通话已结束", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
                
                @Override
                public void onCallFailed(String reason) {
                    runOnUiThread(() -> {
                        Toast.makeText(CallActivity.this, "通话失败: " + reason, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });
            
            isBound = true;
            
            // 检查是接听来电还是拨打电话
            processIntent(getIntent());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            linphoneService = null;
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 设置SIP回调
        LinphoneSipManager.getInstance().setSipCallback(this);
        
        // 绑定Linphone服务
        Intent intent = new Intent(this, LinphoneService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        
        // 设置按钮监听器
        setupButtonListeners();
        
        // 处理Intent
        processIntent(getIntent());
    }
    
    private void setupButtonListeners() {
        // 挂断按钮
        binding.btnHangup.setOnClickListener(v -> {
            if (linphoneService != null) {
                linphoneService.hangupCall();
            } else if (currentCall != null) {
                currentCall.hangup();
            }
            finish();
        });
        
        // 麦克风静音按钮
        binding.btnMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            if (linphoneService != null) {
                linphoneService.toggleMute(isMuted);
            }
            
            // 更新UI
            // binding.btnMute.setImageResource(isMuted ? R.drawable.ic_mic_on : R.drawable.ic_mic_off);
            Toast.makeText(this, isMuted ? "麦克风已静音" : "麦克风已取消静音", Toast.LENGTH_SHORT).show();
        });
        
        // 扬声器按钮
        binding.btnSpeaker.setOnClickListener(v -> {
            isSpeakerOn = !isSpeakerOn;
            if (linphoneService != null) {
                linphoneService.toggleSpeaker(isSpeakerOn);
            }
            
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
                    
                    if (linphoneService != null) {
                        linphoneService.makeCall(number);
                    } else {
                        LinphoneSipManager.getInstance().makeCall(number);
                    }
                } else {
                    Toast.makeText(this, "无效号码", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else if ("ANSWER_CALL".equals(action)) {
                // 接听来电
                binding.tvCallState.setText("正在接通...");
                binding.tvCallerId.setText(intent.getStringExtra("caller"));
                
                // 如果是来电，尝试接听
                if (linphoneService != null) {
                    linphoneService.answerCall();
                } else if (currentCall != null) {
                    currentCall.answer();
                }
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
        
        if (linphoneService != null) {
            linphoneService.hangupCall();
        } else if (currentCall != null) {
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
        runOnUiThread(() -> {
            currentCall = call;
            binding.tvCallerId.setText(caller);
            binding.tvCallState.setText("来电...");
            
            // 如果是自动接听模式，则自动接听
            if ("ANSWER_CALL".equals(getIntent().getAction())) {
                call.answer();
            }
        });
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
