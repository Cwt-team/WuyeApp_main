package com.example.wuyeapp.sip;

import android.util.Log;

/**
 * SipCall抽象类，定义了通用的通话操作方法
 */
public abstract class SipCall {
    // 接听来电
    public abstract void answer();
    
    // 挂断电话
    public abstract void hangup();
}
