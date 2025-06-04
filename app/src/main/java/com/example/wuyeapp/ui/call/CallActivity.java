package com.example.wuyeapp.ui.call;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import com.example.wuyeapp.R;
import com.example.wuyeapp.databinding.ActivityCallBinding;
import com.example.wuyeapp.sip.LinphoneSipManager;
import com.example.wuyeapp.sip.LinphoneService;
import com.example.wuyeapp.sip.LinphoneCallback;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.MediaDirection;
import org.linphone.core.AudioDevice;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CallActivity extends AppCompatActivity implements LinphoneCallback {

    private static final String TAG = "CallActivity";
    private static boolean isInCallScreen = false;
    public static boolean isInCallScreen() {
        return isInCallScreen;
    }
    private ActivityCallBinding binding;
    private LinphoneService linphoneService;
    private boolean isBound = false;
    
    private boolean isMuted = false;
    private boolean isSpeakerOn = false;
    private boolean isVideoCall = false;
    private boolean isVideoEnabled = false;
    
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
            // linphoneService.setLinphoneCallback(CallActivity.this); // 注释掉，避免覆盖全局回调
            // 检查是接听来电还是拨打电话
            processIntent(getIntent());
            // 新增：如果是视频通话，绑定视频窗口
            if (isVideoCall || isVideoEnabled) {
                setupVideoSurfaces();
            }
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
        isInCallScreen = true;
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 设置LinphoneCallback（已由Application全局设置，这里注释掉，避免覆盖全局回调）
        // LinphoneSipManager.getInstance().setLinphoneCallback(this);
        
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
                isVideoCall = getIntent().getBooleanExtra("isVideo", false);
                isVideoEnabled = isVideoCall; // 保证视频通话时isVideoEnabled为true
                if (isVideoCall) {
                    binding.tvCallState.setText("正在发起视频通话...");
                    showVideoLayout(true);
                } else {
                    binding.tvCallState.setText("正在发起语音通话...");
                    showVideoLayout(false);
                }
                binding.tvCallerId.setText(number);
                // 只显示挂断按钮
                showOnlyHangupButton();
            } else if ("ANSWER_CALL".equals(action)) {
                String caller = getIntent().getStringExtra("caller");
                isVideoCall = getIntent().getBooleanExtra("isVideo", false);
                isVideoEnabled = isVideoCall;
                binding.tvCallState.setText("来电...");
                binding.tvCallerId.setText(caller);
                // 来电时显示接听/拒接按钮，隐藏开门按钮
                showOnlyAnswerRejectButtons();
                // 默认先隐藏视频布局
                showVideoLayout(false);
            }
        }
        
        // 确认麦克风权限并检查状态
        checkMicrophoneStatus();
        
        // 检查相机权限
        checkCameraPermission();
    }
    
    private void setupButtonListeners() {
        // 挂断按钮
        binding.btnHangup.setOnClickListener(v -> {
            if (linphoneService != null) {
                linphoneService.hangUp();
                Toast.makeText(this, "已挂断", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "linphoneService为空，无法挂断");
            }
            finish();
        });
        
        // 接听按钮
        binding.btnAnswer.setOnClickListener(v -> {
            if (linphoneService != null) {
                linphoneService.answerCall(isVideoCall);
                Toast.makeText(this, "已接听", Toast.LENGTH_SHORT).show();
                // 接听后隐藏接听/拒接按钮，显示开门按钮
                showCallControls();
            } else {
                Log.e(TAG, "linphoneService为空，无法接听");
            }
        });
        
        // 拒接按钮
        binding.btnReject.setOnClickListener(v -> {
            if (linphoneService != null) {
                linphoneService.hangUp();
                Toast.makeText(this, "已拒绝", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "linphoneService为空，无法拒绝");
            }
            finish();
        });
        
        // 麦克风静音按钮
        binding.btnMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            if (linphoneService != null) {
                linphoneService.toggleMute(isMuted);
                // 更新麦克风图标
                binding.btnMute.setImageResource(isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic_on);
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
                // 更新扬声器图标
                binding.btnSpeaker.setImageResource(isSpeakerOn ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
                Toast.makeText(this, isSpeakerOn ? "扬声器已开启" : "扬声器已关闭", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "linphoneService为空，无法操作扬声器");
            }
        });
        
        // 视频按钮
        binding.btnVideo.setOnClickListener(v -> {
            isVideoEnabled = !isVideoEnabled;
            if (linphoneService != null) {
                // 获取当前通话
                Call currentCall = linphoneService.getCore().getCurrentCall();
                if (currentCall != null) {
                    // 获取通话参数，启用或禁用视频
                    CallParams params = linphoneService.getCore().createCallParams(currentCall);
                    params.setVideoEnabled(isVideoEnabled);
                    currentCall.update(params);
                    
                    Toast.makeText(this, isVideoEnabled ? "视频已开启" : "视频已关闭", Toast.LENGTH_SHORT).show();
                    
                    // 根据视频状态显示或隐藏视频布局
                    showVideoLayout(isVideoEnabled);
                    
                    // 如果启用视频，设置相应的视频显示区域
                    if (isVideoEnabled) {
                        setupVideoSurfaces();
                        // 显示摄像头切换按钮
                        binding.btnSwitchCamera.setVisibility(View.VISIBLE);
                    } else {
                        // 隐藏摄像头切换按钮
                        binding.btnSwitchCamera.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(this, "当前没有活动通话", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "linphoneService为空，无法切换视频");
            }
        });
        
        // 摄像头切换按钮
        binding.btnSwitchCamera.setOnClickListener(v -> {
            if (linphoneService != null && isVideoEnabled) {
                linphoneService.switchCamera();
                Toast.makeText(this, "已切换摄像头", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "linphoneService为空或视频未启用，无法切换摄像头");
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
                    isVideoCall = intent.getBooleanExtra("isVideo", false);
                    isVideoEnabled = isVideoCall;
                    if (isVideoCall) {
                        binding.tvCallState.setText("正在发起视频通话...");
                        showVideoLayout(true);
                    } else {
                        binding.tvCallState.setText("正在发起语音通话...");
                        showVideoLayout(false);
                    }
                    binding.tvCallerId.setText(number);
                    // 只显示挂断按钮
                    showOnlyHangupButton();
                    if (linphoneService != null) {
                        Log.d(TAG, "开始拨打: " + number);
                        // 确保音频设备已初始化
                        ensureAudioDevicesReady();
                        // 判断是否是视频通话
                        if (isVideoCall) {
                            linphoneService.makeVideoCall(number);
                            setupVideoSurfaces();
                            binding.btnSwitchCamera.setVisibility(View.VISIBLE);
                        } else {
                            linphoneService.makeCall(number, false);
                            binding.btnSwitchCamera.setVisibility(View.GONE);
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
                binding.tvCallState.setText("来电...");
                binding.tvCallerId.setText(intent.getStringExtra("caller"));
                isVideoCall = intent.getBooleanExtra("isVideo", false);
                isVideoEnabled = isVideoCall;
                // 只显示接听/拒接按钮
                showOnlyAnswerRejectButtons();
                // 确保音频设备已初始化
                ensureAudioDevicesReady();
                // 不再自动接听，而是等待用户点击接听按钮
                showVideoLayout(false);
            }
        }
    }
    
    // 显示或隐藏视频布局
    private void showVideoLayout(boolean show) {
        if (show) {
            binding.remoteVideoLayout.setVisibility(View.VISIBLE);
            // 视频通话时也显示号码和状态等信息（只隐藏通话时的按钮区）
            binding.audioCallLayout.setVisibility(View.VISIBLE);
            // 只隐藏音频相关的按钮区（如有需要可细化）
            binding.incomingCallButtons.setVisibility(View.GONE);
            binding.callControlsLayout.setVisibility(View.GONE);
        } else {
            binding.remoteVideoLayout.setVisibility(View.GONE);
            binding.audioCallLayout.setVisibility(View.VISIBLE);
        }
    }
    
    // 设置视频显示区域
    private void setupVideoSurfaces() {
        try {
            if (linphoneService != null) {
                TextureView localVideoView = binding.localVideoSurface;
                TextureView remoteVideoView = binding.remoteVideoSurface;
                
                // 将TextureView传递给LinphoneService进行视频显示
                linphoneService.setVideoSurfaces(localVideoView, remoteVideoView);
                
                Log.d(TAG, "视频表面已设置");
            }
        } catch (Exception e) {
            Log.e(TAG, "设置视频表面失败", e);
        }
    }
    
    // 检查相机权限
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // 如无权限，立即请求
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    101);
        }
    }
    
    // 确保音频设备已准备好
    private void ensureAudioDevicesReady() {
        if (linphoneService == null || linphoneService.getCore() == null) {
            Log.e(TAG, "无法初始化音频设备，Core为空");
            return;
        }
        
        // 初始化音频设备 - 通过LinphoneManager
        try {
            Core core = linphoneService.getCore();
            
            // 确保麦克风已启用
            core.setMicEnabled(true);
            
            // 借助LinphoneManager进行音频初始化
            if (core.getInputAudioDevice() == null) {
                Log.w(TAG, "输入音频设备未设置，尝试初始化...");
                
                // 优先使用LinphoneManager的音频初始化方法
                try {
                    // 通过反射获取LinphoneManager实例并调用initAudioDevices
                    java.lang.reflect.Method getInstance = Class.forName("com.example.wuyeapp.sip.LinphoneManager")
                        .getMethod("getInstance", Context.class);
                    Object manager = getInstance.invoke(null, this);
                    
                    java.lang.reflect.Method initAudio = manager.getClass().getMethod("initAudioDevices");
                    initAudio.invoke(manager);
                    
                    Log.i(TAG, "通过LinphoneManager初始化音频设备成功");
                } catch (Exception e) {
                    Log.e(TAG, "通过反射调用初始化失败，回退到本地方法", e);
                    // 如果失败，使用本地方法
                    initializeLinphoneAudio(core);
                }
            } else {
                Log.i(TAG, "音频设备已设置，当前输入设备: " + core.getInputAudioDevice().getDeviceName());
            }
            
            // 设置音频编解码器
            for (org.linphone.core.PayloadType pt : core.getAudioPayloadTypes()) {
                if ("PCMA".equalsIgnoreCase(pt.getMimeType()) || 
                    "PCMU".equalsIgnoreCase(pt.getMimeType())) {
                    pt.enable(true);
                    Log.i(TAG, "已确保 " + pt.getMimeType() + " 编解码器已启用");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "确保音频设备准备就绪时出错", e);
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
        isInCallScreen = true;
        setIntent(intent);
        processIntent(intent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isInCallScreen = false;
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
            // 检查是否为视频通话
            if (call != null && call.getRemoteParams() != null) {
                isVideoCall = call.getRemoteParams().isVideoEnabled();
            }
            
            // 启动新的来电界面
            Intent intent = new Intent(this, CallActivity.class);
            intent.setAction("ANSWER_CALL");
            intent.putExtra("caller", caller);
            intent.putExtra("isVideo", isVideoCall);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }
    
    @Override
    public void onCallProgress() {
        runOnUiThread(() -> {
            if (isVideoCall || isVideoEnabled) {
                binding.tvCallState.setText("视频通话呼叫中...");
            } else {
                binding.tvCallState.setText("语音通话呼叫中...");
            }
        });
    }
    
    @Override
    public void onCallEstablished() {
        runOnUiThread(() -> {
            if (isVideoCall || isVideoEnabled) {
                binding.tvCallState.setText("视频通话中");
            } else {
                binding.tvCallState.setText("语音通话中");
            }
            showCallControls();
            if (isVideoEnabled) {
                setupVideoSurfaces();
            }
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
    
    // 确认麦克风权限并检查状态
    private void checkMicrophoneStatus() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // 如无权限，立即请求
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    100);
            return;
        }
        
        // 尝试打开麦克风以验证其可用性
        try {
            int sampleRate = 44100;
            int minBufferSize = AudioRecord.getMinBufferSize(sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
                    
            if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "麦克风不可用：缓冲区大小无效");
                Toast.makeText(this, "麦克风初始化失败", Toast.LENGTH_SHORT).show();
                showAudioDeviceErrorDialog();
                return;
            }
            
            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2);
                    
            if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "麦克风不可用：无法初始化");
                showAudioDeviceErrorDialog();
                recorder.release();
                return;
            }
            
            // 测试录音是否有效
            recorder.startRecording();
            short[] buffer = new short[minBufferSize];
            int read = recorder.read(buffer, 0, minBufferSize);
            recorder.stop();
            recorder.release();
            
            if (read <= 0) {
                Log.e(TAG, "麦克风不可用：无法读取数据");
                showAudioDeviceErrorDialog();
                return;
            }
            
            Log.i(TAG, "麦克风状态检查：正常，可读取数据");
            
            // 确保Linphone的音频设备已初始化
            if (linphoneService != null) {
                Core core = linphoneService.getCore();
                if (core != null) {
                    // 初始化音频设备
                    initializeLinphoneAudio(core);
                    Log.i(TAG, "Linphone音频设备已初始化");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "麦克风检查失败", e);
            showAudioDeviceErrorDialog();
        }
    }
    
    // 显示音频设备错误对话框
    private void showAudioDeviceErrorDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("音频设备错误")
               .setMessage("麦克风不可用或被占用。请确保已授予麦克风权限，并且没有其他应用正在使用麦克风。")
               .setPositiveButton("去设置", (dialog, which) -> {
                   // 跳转到应用设置页面
                   Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                   Uri uri = Uri.fromParts("package", getPackageName(), null);
                   intent.setData(uri);
                   startActivity(intent);
               })
               .setNegativeButton("取消", (dialog, which) -> {
                   finish(); // 结束活动
               })
               .setCancelable(false)
               .show();
    }
    
    // 初始化Linphone音频设备
    private void initializeLinphoneAudio(Core core) {
        if (core != null) {
            // 检查并设置音频设备
            AudioDevice[] devices = core.getAudioDevices();
            boolean hasMic = false;
            boolean hasSpeaker = false;
            
            for (AudioDevice device : devices) {
                Log.i(TAG, "检测到音频设备: " + device.getDeviceName() + ", 类型: " + device.getType());
                
                if (device.getType() == AudioDevice.Type.Microphone) {
                    core.setInputAudioDevice(device);
                    hasMic = true;
                    Log.i(TAG, "已设置麦克风: " + device.getDeviceName());
                }
                
                if (device.getType() == AudioDevice.Type.Speaker) {
                    // 备用扬声器
                    hasSpeaker = true;
                }
            }
            
            // 确保麦克风已启用
            core.setMicEnabled(true);
            
            if (!hasMic) {
                Log.e(TAG, "未找到可用麦克风设备");
                Toast.makeText(this, "未找到可用麦克风", Toast.LENGTH_LONG).show();
            }
            
            if (!hasSpeaker) {
                Log.e(TAG, "未找到可用扬声器设备");
            }
            
            // 确保音频编解码器已启用
            for (org.linphone.core.PayloadType pt : core.getAudioPayloadTypes()) {
                if ("PCMU".equalsIgnoreCase(pt.getMimeType()) || 
                    "PCMA".equalsIgnoreCase(pt.getMimeType())) {
                    pt.enable(true);
                    Log.i(TAG, "已启用 " + pt.getMimeType() + " 编解码器");
                }
            }
        }
    }

    private void showOnlyHangupButton() {
        binding.incomingCallButtons.setVisibility(View.GONE);
        binding.callControlsLayout.setVisibility(View.VISIBLE);
        // 只显示挂断按钮，隐藏其它功能按钮
        binding.btnMute.setVisibility(View.GONE);
        binding.btnSpeaker.setVisibility(View.GONE);
        binding.btnVideo.setVisibility(View.GONE);
        binding.btnSwitchCamera.setVisibility(View.GONE);
        binding.doorControlButtons.setVisibility(View.GONE);
        binding.btnHangup.setVisibility(View.VISIBLE);
    }

    private void showOnlyAnswerRejectButtons() {
        binding.incomingCallButtons.setVisibility(View.VISIBLE);
        binding.callControlsLayout.setVisibility(View.GONE);
    }

    private void showCallControls() {
        binding.incomingCallButtons.setVisibility(View.GONE);
        binding.callControlsLayout.setVisibility(View.VISIBLE);
        // 显示所有功能按钮
        binding.btnMute.setVisibility(View.VISIBLE);
        binding.btnSpeaker.setVisibility(View.VISIBLE);
        binding.btnVideo.setVisibility(View.VISIBLE);
        binding.doorControlButtons.setVisibility(View.VISIBLE);
        binding.btnHangup.setVisibility(View.VISIBLE);
        // 视频相关
        if (isVideoEnabled) {
            binding.btnSwitchCamera.setVisibility(View.VISIBLE);
        } else {
            binding.btnSwitchCamera.setVisibility(View.GONE);
        }
    }
}
