package com.example.wuyeapp.sip;

import android.util.Log;
import org.linphone.core.Call;

/**
 * 适配器类，将Linphone的Call转换为SipCall接口
 */
public class LinphoneCallAdapter extends SipCall {
    private static final String TAG = "LinphoneCallAdapter";
    private Call linphoneCall;
    
    public LinphoneCallAdapter(Call call) {
        this.linphoneCall = call;
    }
    
    @Override
    public void answer() {
        try {
            linphoneCall.accept();
            Log.i(TAG, "已接听来电");
        } catch (Exception e) {
            Log.e(TAG, "接听来电失败", e);
        }
    }
    
    @Override
    public void hangup() {
        try {
            linphoneCall.terminate();
            Log.i(TAG, "已挂断电话");
        } catch (Exception e) {
            Log.e(TAG, "挂断电话失败", e);
        }
    }
} 