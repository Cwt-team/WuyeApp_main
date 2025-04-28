package com.example.wuyeapp.ui.call;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.databinding.ActivityCallBinding;
import com.example.wuyeapp.sip.LinphoneSipManager;
import com.example.wuyeapp.sip.LinphoneService;
import com.example.wuyeapp.sip.LinphoneCallback;

import org.linphone.core.Call;
import org.linphone.core.Core;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CallActivity extends AppCompatActivity implements LinphoneCallback {

    private static final String TAG = "CallActivity";
    private ActivityCallBinding binding;
    private LinphoneService linphoneService;
    private boolean isBound = false;
    
    private boolean isMuted = false;
    private boolean isSpeakerOn = false;
    private boolean isVideoCall = false;
    
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
            isBound = true;
            
            // 设置回调
            linphoneService.setLinphoneCallback(CallActivity.this);
            
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
        
        // 设置LinphoneCallback
        LinphoneSipManager.getInstance().setLinphoneCallback(this);
        
        // 绑定Linphone服务
        Intent intent = new Intent(this, LinphoneService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        
        // 设置按钮监听器
        setupButtonListeners();
        
        // 处理Intent
        if (linphoneService == null) {
            // 服务未绑定，先初始化UI
            String action = getIntent().getAction();
            if ("MAKE_CALL".equals(action)) {
                String number = getIntent().getStringExtra("number");
                binding.tvCallState.setText("正在拨打...");
                binding.tvCallerId.setText(number);
            } else if ("ANSWER_CALL".equals(action)) {
                String caller = getIntent().getStringExtra("caller");
                binding.tvCallState.setText("来电...");
                binding.tvCallerId.setText(caller);
            }
        }
    }
    
    private void setupButtonListeners() {
        // 挂断按钮
        binding.btnHangup.setOnClickListener(v -> {
            if (linphoneService != null) {
                linphoneService.hangupCall();
                Toast.makeText(this, "已挂断", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "linphoneService为空，无法挂断");
            }
            finish();
        });
        
        // 麦克风静音按钮
        binding.btnMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            if (linphoneService != null) {
                linphoneService.toggleMute(isMuted);
                Toast.makeText(this, isMuted ? "麦克风已静音" : "麦克风已取消静音", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "linphoneService为空，无法操作麦克风");
            }
        });
        
        // 扬声器按钮
        binding.btnSpeaker.setOnClickListener(v -> {
            isSpeakerOn = !isSpeakerOn;
            if (linphoneService != null) {
                linphoneService.toggleSpeaker(isSpeakerOn);
                Toast.makeText(this, isSpeakerOn ? "扬声器已开启" : "扬声器已关闭", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "linphoneService为空，无法操作扬声器");
            }
        });
        
        // 添加"*"开门按钮监听器
        binding.btnOpenDoorStar.setOnClickListener(v -> {
            if (linphoneService != null) {
                linphoneService.sendDtmf('*');
                Toast.makeText(this, "已发送*信号开门", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "linphoneService为空，无法发送DTMF");
            }
        });
        
        // 添加"#"开门按钮监听器
        binding.btnOpenDoorHash.setOnClickListener(v -> {
            if (linphoneService != null) {
                linphoneService.sendDtmf('#');
                Toast.makeText(this, "已发送#信号开门", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "linphoneService为空，无法发送DTMF");
            }
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
                        Log.d(TAG, "开始拨打: " + number);
                        
                        // 判断是否是视频通话
                        isVideoCall = intent.getBooleanExtra("isVideo", false);
                        if (isVideoCall) {
                            linphoneService.makeVideoCall(number);
                        } else {
                            linphoneService.makeCall(number);
                        }
                    } else {
                        Toast.makeText(this, "SIP服务未准备就绪，无法拨打", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(this, "无效号码", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else if ("ANSWER_CALL".equals(action)) {
                // 接听来电
                binding.tvCallState.setText("来电...");
                binding.tvCallerId.setText(intent.getStringExtra("caller"));
                
                // 由于可能有系统来电界面，这里可能需要延迟接听
                new Handler().postDelayed(() -> {
                    if (linphoneService != null) {
                        Log.d(TAG, "接听来电");
                        linphoneService.answerCall();
                    } else {
                        Log.e(TAG, "linphoneService为空，无法接听");
                    }
                }, 500);
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (linphoneService != null) {
            processIntent(intent);
        }
    }
    
    @Override
    protected void onDestroy() {
        stopCallTimer();
        
        // 取消回调
        if (linphoneService != null) {
            // 保险起见，清除回调，但不要关闭通话
            // linphoneService.setLinphoneCallback(null);
        }
        
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        
        super.onDestroy();
    }
    
    // LinphoneCallback 接口实现
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
        runOnUiThread(() -> {
            // 1. 关闭当前通话界面（可选，或弹窗提示）
            // finish();

            // 2. 启动新的来电界面
            Intent intent = new Intent(this, CallActivity.class);
            intent.setAction("ANSWER_CALL");
            intent.putExtra("caller", caller);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }
    
    @Override
    public void onCallProgress() {
        runOnUiThread(() -> {
            binding.tvCallState.setText("呼叫中...");
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
}
