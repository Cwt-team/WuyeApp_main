package com.example.wuyeapp.sip;

import android.util.Log;

import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;

public class SipCall extends Call {
    private static final String TAG = "SipCall";
    
    public SipCall(SipAccount account) {
        super(account);
    }
    
    @Override
    public void onCallState(OnCallStateParam prm) {
        try {
            CallInfo info = getInfo();
            Log.i(TAG, "通话状态: " + info.getStateText());
            
            // 如果通话已经连接，则设置媒体
            if (info.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                Log.i(TAG, "通话已建立");
            } else if (info.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                Log.i(TAG, "通话已结束");
            }
        } catch (Exception e) {
            Log.e(TAG, "处理通话状态失败", e);
        }
    }
    
    @Override
    public void onCallMediaState(OnCallMediaStateParam prm) {
        try {
            CallInfo info = getInfo();
            
            // 遍历媒体
            for (int i = 0; i < info.getMedia().size(); i++) {
                CallMediaInfo mediaInfo = info.getMedia().get(i);
                
                // 音频媒体处理
                if (mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO && 
                    getMedia(i) != null) {
                    AudioMedia audioMedia = AudioMedia.typecastFromMedia(getMedia(i));
                    
                    // 连接音频设备
                    try {
                        // 获取当前音频设备
                        AudioMedia audioDevice = org.pjsip.pjsua2.Endpoint.instance().audDevManager().getPlaybackDevMedia();
                        AudioMedia captureDevice = org.pjsip.pjsua2.Endpoint.instance().audDevManager().getCaptureDevMedia();
                        
                        // 连接麦克风到远程
                        captureDevice.startTransmit(audioMedia);
                        
                        // 连接远程到扬声器
                        audioMedia.startTransmit(audioDevice);
                        
                        Log.i(TAG, "音频媒体已连接");
                    } catch (Exception e) {
                        Log.e(TAG, "音频媒体连接失败", e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "处理媒体状态失败", e);
        }
    }
    
    // 接听来电
    public void answer() {
        try {
            CallOpParam param = new CallOpParam();
            param.setStatusCode(pjsua_status_code.PJSIP_SC_OK);
            answer(param);
            Log.i(TAG, "已接听来电");
        } catch (Exception e) {
            Log.e(TAG, "接听来电失败", e);
        }
    }
    
    // 挂断电话
    public void hangup() {
        try {
            CallOpParam param = new CallOpParam();
            param.setStatusCode(pjsua_status_code.PJSIP_SC_DECLINE);
            hangup(param);
            Log.i(TAG, "已挂断电话");
        } catch (Exception e) {
            Log.e(TAG, "挂断电话失败", e);
        }
    }
}
