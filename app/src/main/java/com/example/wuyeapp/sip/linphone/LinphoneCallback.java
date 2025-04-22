package com.example.wuyeapp.sip.linphone;

import org.linphone.core.Call;

/**
 * Linphone回调接口
 */
public interface LinphoneCallback {
    void onRegistrationSuccess();
    void onRegistrationFailed(String reason);
    void onIncomingCall(Call call, String caller);
    void onCallProgress();
    void onCallEstablished();
    void onCallEnded();
    void onCallFailed(String reason);
} 