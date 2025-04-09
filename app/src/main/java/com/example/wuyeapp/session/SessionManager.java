package com.example.wuyeapp.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.wuyeapp.model.OwnerInfo;
import com.example.wuyeapp.utils.LogUtil;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "WuyeAppSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_OWNER_ID = "ownerId";
    private static final String KEY_OWNER_NAME = "ownerName";
    private static final String KEY_OWNER_PHONE = "ownerPhone";
    private static final String KEY_OWNER_ACCOUNT = "ownerAccount";
    
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    
    // 单例模式实例
    private static SessionManager instance;
    
    // 构造函数
    private SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    
    // 获取单例实例
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }
    
    // 保存登录状态和用户信息
    public void createLoginSession(OwnerInfo owner) {
        LogUtil.i(TAG + " 创建登录会话: " + owner.getName());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_OWNER_ID, owner.getId());
        editor.putString(KEY_OWNER_NAME, owner.getName());
        editor.putString(KEY_OWNER_PHONE, owner.getPhoneNumber());
        editor.putString(KEY_OWNER_ACCOUNT, owner.getAccount());
        editor.commit();
    }
    
    // 检查用户是否登录
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    // 获取用户信息
    public OwnerInfo getOwnerInfo() {
        if (!isLoggedIn()) {
            return null;
        }
        
        OwnerInfo owner = new OwnerInfo();
        owner.setId(pref.getLong(KEY_OWNER_ID, 0));
        owner.setName(pref.getString(KEY_OWNER_NAME, ""));
        owner.setPhoneNumber(pref.getString(KEY_OWNER_PHONE, ""));
        owner.setAccount(pref.getString(KEY_OWNER_ACCOUNT, ""));
        
        return owner;
    }
    
    // 注销登录
    public void logout() {
        LogUtil.i(TAG + " 退出登录");
        editor.clear();
        editor.commit();
    }
} 