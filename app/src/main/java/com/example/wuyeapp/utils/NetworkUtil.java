package com.example.wuyeapp.utils;

import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * 网络工具类，提供与网络相关的工具方法
 */
public class NetworkUtil {
    private static final String TAG = "NetworkUtil";

    /**
     * 获取本地IP地址
     * @return 本地IP地址，如果获取失败则返回null
     */
    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(':') == -1) {
                        String ipAddress = address.getHostAddress();
                        Log.d(TAG, "IP地址: " + ipAddress + " 网络接口: " + networkInterface.getDisplayName());
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "获取本地IP地址失败", e);
        }
        return null;
    }

    /**
     * 判断IP地址是否为内网地址
     * 内网IP范围：
     * 10.0.0.0 - 10.255.255.255
     * 172.16.0.0 - 172.31.255.255
     * 192.168.0.0 - 192.168.255.255
     * 
     * @param ipAddress IP地址
     * @return 如果是内网地址则返回true，否则返回false
     */
    public static boolean isPrivateIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return false;
        }

        try {
            // 检查是否是有效的IPv4地址格式
            Pattern pattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
            if (!pattern.matcher(ipAddress).matches()) {
                return false;
            }

            // 解析IP地址
            String[] parts = ipAddress.split("\\.");
            int firstOctet = Integer.parseInt(parts[0]);
            int secondOctet = Integer.parseInt(parts[1]);

            // 检查是否是内网IP地址
            // 10.0.0.0 - 10.255.255.255
            if (firstOctet == 10) {
                return true;
            }
            
            // 172.16.0.0 - 172.31.255.255
            if (firstOctet == 172 && (secondOctet >= 16 && secondOctet <= 31)) {
                return true;
            }
            
            // 192.168.0.0 - 192.168.255.255
            if (firstOctet == 192 && secondOctet == 168) {
                return true;
            }
            
            return false;
        } catch (Exception e) {
            Log.e(TAG, "检查IP地址类型失败", e);
            return false;
        }
    }
} 