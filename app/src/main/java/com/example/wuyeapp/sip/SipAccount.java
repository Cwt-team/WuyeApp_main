package com.example.wuyeapp.sip;

import android.util.Log;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnRegStateParam;

public class SipAccount extends Account {
    private static final String TAG = "SipAccount";
    private final SipService service;
    
    public SipAccount(SipService service) {
        this.service = service;
    }
    
    @Override
    public void onRegState(OnRegStateParam prm) {
        Log.i(TAG, "账户注册状态: " + prm.getCode());
        service.onRegState(prm.getCode() / 100 == 2); // 2xx 表示成功
    }
    
    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {
        Log.i(TAG, "收到来电");
        SipCall call = new SipCall(this);
        try {
            call.setCall(prm.getCallId());
            service.onIncomingCall(call);
        } catch (Exception e) {
            Log.e(TAG, "处理来电失败", e);
        }
    }
}
