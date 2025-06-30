package com.example.wuyeapp.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.example.wuyeapp.model.user.OwnerInfo;
import com.example.wuyeapp.model.shop.ShopAuthResponse;
import com.example.wuyeapp.utils.LogUtil;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "WuyeAppPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_AUTH_TOKEN = "authToken";
    private static final String KEY_SHOP_TOKEN = "shopToken";
    private static final String KEY_SHOP_TOKEN_EXPIRY = "shopTokenExpiry";
    private static final String KEY_SHOP_USER_ROLE = "shopUserRole";
    private static final String KEY_SHOP_ID = "shopId";
    private static final String KEY_OWNER_ID = "ownerId";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ACCOUNT = "account";
    private static final String KEY_COMMUNITY_ID = "communityId";
    private static final String KEY_HOUSE_ID = "houseId";
    private static final String KEY_AVATAR = "avatar";

    private static SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void createLoginSession(OwnerInfo ownerInfo, String authToken) {
        Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_AUTH_TOKEN, authToken);
        editor.putString(KEY_OWNER_ID, String.valueOf(ownerInfo.getId()));
        editor.putString(KEY_NAME, ownerInfo.getName());
        editor.putString(KEY_PHONE, ownerInfo.getPhone());
        editor.putString(KEY_EMAIL, ownerInfo.getEmail());
        editor.putString(KEY_ACCOUNT, ownerInfo.getAccount());
        editor.putString(KEY_COMMUNITY_ID, ownerInfo.getCommunityId());
        editor.putString(KEY_HOUSE_ID, ownerInfo.getHouseId());
        editor.putString(KEY_AVATAR, ownerInfo.getAvatar());
        editor.apply();
    }

    public void createShopLoginSession(ShopAuthResponse response) {
        if (response == null || response.getToken() == null || response.getToken().isEmpty()) {
            LogUtil.e(TAG, "创建商城会话失败：无效的认证响应");
            return;
        }

        Editor editor = prefs.edit();
        editor.putString(KEY_SHOP_TOKEN, response.getToken());
        
        // 设置过期时间为23.5小时后（比24小时少30分钟，留出刷新时间）
        long expiryTime = System.currentTimeMillis() + (23 * 60 + 30) * 60 * 1000;
        editor.putLong(KEY_SHOP_TOKEN_EXPIRY, expiryTime);
        
        editor.putString(KEY_SHOP_USER_ROLE, response.getUserRole());
        if (response.getShopId() != null) {
            editor.putInt(KEY_SHOP_ID, response.getShopId());
        }
        editor.apply();
        
        LogUtil.i(TAG, "商城登录会话已创建，token将在23.5小时后过期");
    }

    public OwnerInfo getOwnerInfo() {
        if (!isLoggedIn()) {
            return null;
        }

        OwnerInfo ownerInfo = new OwnerInfo();
        ownerInfo.setId(Long.parseLong(prefs.getString(KEY_OWNER_ID, "0")));
        ownerInfo.setName(prefs.getString(KEY_NAME, null));
        ownerInfo.setPhone(prefs.getString(KEY_PHONE, null));
        ownerInfo.setEmail(prefs.getString(KEY_EMAIL, null));
        ownerInfo.setAccount(prefs.getString(KEY_ACCOUNT, null));
        ownerInfo.setCommunityId(prefs.getString(KEY_COMMUNITY_ID, null));
        ownerInfo.setHouseId(prefs.getString(KEY_HOUSE_ID, null));
        ownerInfo.setAvatar(prefs.getString(KEY_AVATAR, null));
        return ownerInfo;
    }

    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, "");
    }

    public String getShopToken() {
        String token = prefs.getString(KEY_SHOP_TOKEN, "");
        if (token.isEmpty() || isShopTokenExpired()) {
            LogUtil.w(TAG, "商城token为空或已过期");
            return "";
        }
        return token;
    }

    public int getShopId() {
        return prefs.getInt(KEY_SHOP_ID, -1);
    }

    public String getShopUserRole() {
        return prefs.getString(KEY_SHOP_USER_ROLE, "");
    }

    public boolean isShopTokenNearExpiry() {
        if (isShopTokenExpired()) {
            return true;
        }
        
        long expiryTime = prefs.getLong(KEY_SHOP_TOKEN_EXPIRY, 0);
        long currentTime = System.currentTimeMillis();
        // 设置为30分钟提前刷新
        long thirtyMinutes = 30 * 60 * 1000;
        boolean isNearExpiry = (expiryTime - currentTime) <= thirtyMinutes;
        
        if (isNearExpiry) {
            LogUtil.i(TAG, "商城token即将在30分钟内过期");
        }
        
        return isNearExpiry;
    }

    public boolean isShopTokenExpired() {
        long expiryTime = prefs.getLong(KEY_SHOP_TOKEN_EXPIRY, 0);
        boolean isExpired = System.currentTimeMillis() >= expiryTime;
        if (isExpired) {
            LogUtil.w(TAG, "商城token已过期");
            // 清除过期的token
            logoutShop();
        }
        return isExpired;
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isShopLoggedIn() {
        String token = getShopToken();
        if (token.isEmpty()) {
            LogUtil.d(TAG, "商城未登录：token为空");
            return false;
        }
        
        if (isShopTokenExpired()) {
            LogUtil.d(TAG, "商城未登录：token已过期");
            return false;
        }
        
        return true;
    }

    public void logout() {
        Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        LogUtil.i(TAG, "用户已登出，所有会话数据已清除");
    }

    public void logoutShop() {
        Editor editor = prefs.edit();
        editor.remove(KEY_SHOP_TOKEN);
        editor.remove(KEY_SHOP_TOKEN_EXPIRY);
        editor.remove(KEY_SHOP_USER_ROLE);
        editor.remove(KEY_SHOP_ID);
        editor.apply();
        LogUtil.i(TAG, "商城用户已登出，商城会话数据已清除");
    }
} 
