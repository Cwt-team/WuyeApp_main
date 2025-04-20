package com.example.wuyeapp.sip;

public interface SipCallback {
    void onRegistrationSuccess();
    void onRegistrationFailed(String reason);
    void onIncomingCall(SipCall call, String caller);
    void onCallFailed(String reason);
    void onCallEstablished();
    void onCallEnded();
}
