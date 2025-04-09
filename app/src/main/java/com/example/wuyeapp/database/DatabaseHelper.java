package com.example.wuyeapp.database;

import android.os.StrictMode;
import android.util.Log;

import com.example.wuyeapp.model.OwnerInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    // 数据库连接信息
    private static final String DB_URL = "jdbc:mysql://10.0.2.2:3326/Wuye";
    private static final String USER = "root";
    private static final String PASS = "123456";
    private static final String TAG = "DatabaseHelper";

    public interface ConnectionCallback {
        void onConnectionSuccess(Connection connection);
        void onConnectionFailed(SQLException exception);
    }

    // 获取数据库连接
    public Connection getConnection() throws SQLException {
        Connection connection = null;
        try {
            // 更强力地设置 StrictMode
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitNetwork()  // 允许网络操作
                        .permitDiskReads()
                        .permitDiskWrites()
                        .build();
                StrictMode.setThreadPolicy(policy);
                
                // 同时设置 VmPolicy
                StrictMode.VmPolicy vmPolicy = new StrictMode.VmPolicy.Builder()
                        .detectAll()  // 检测所有问题但不惩罚
                        .penaltyLog() // 仅记录日志，不崩溃
                        .build();
                StrictMode.setVmPolicy(vmPolicy);
            }
            
            // 使用5.1.x版本的驱动类名
            Class.forName("com.mysql.jdbc.Driver");
            
            // 针对5.1.x驱动修改连接参数
            String url = "jdbc:mysql://10.10.57.182:3326/Wuye?useSSL=false" +
                    "&connectTimeout=30000&socketTimeout=60000" +
                    "&useUnicode=true&characterEncoding=utf8" +
                    "&allowPublicKeyRetrieval=true";
            String user = "root";
            String password = "123456";
            
            Log.i(TAG, "尝试连接数据库: " + url);
            DriverManager.setLoginTimeout(10); // 设置登录超时时间为10秒
            connection = DriverManager.getConnection(url, user, password);
            Log.i(TAG, "数据库连接创建成功");
            return connection;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MySQL JDBC驱动未找到: " + e.getMessage(), e);
            e.printStackTrace();
            throw new SQLException("MySQL JDBC驱动未找到", e);
        } catch (SQLException e) {
            Log.e(TAG, "数据库连接失败: " + e.getMessage() +
                  "\nSQL状态: " + e.getSQLState() + 
                  "\n错误代码: " + e.getErrorCode(), e);
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "发生未知错误: " + e.getMessage(), e);
            e.printStackTrace();
            throw new SQLException("连接数据库时发生未知错误", e);
        }
    }

    public void getConnectionAsync(final ConnectionCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection conn = getConnection();
                    if (callback != null) {
                        callback.onConnectionSuccess(conn);
                    }
                } catch (SQLException e) {
                    Log.e(TAG, "异步获取数据库连接失败: " + e.getMessage(), e);
                    if (callback != null) {
                        callback.onConnectionFailed(e);
                    }
                }
            }
        }).start();
    }

    // 通过账号和密码查询业主
    public OwnerInfo getOwnerByAccountAndPassword(String account, String password) {
        OwnerInfo owner = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT id, name, phone_number, account FROM owner_info WHERE account = ? AND password = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, account);
            stmt.setString(2, password);
            rs = stmt.executeQuery();

            if (rs.next()) {
                owner = new OwnerInfo(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("phone_number"),
                        rs.getString("account")
                );
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error querying owner: " + e.getMessage(), e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return owner;
    }

    // 通过手机号和密码查询业主
    public OwnerInfo getOwnerByPhoneAndPassword(String phone, String password) {
        OwnerInfo owner = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT id, name, phone_number, account FROM owner_info WHERE phone_number = ? AND password = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, phone);
            stmt.setString(2, password);
            rs = stmt.executeQuery();

            if (rs.next()) {
                owner = new OwnerInfo(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("phone_number"),
                        rs.getString("account")
                );
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error querying owner: " + e.getMessage(), e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return owner;
    }

    // 通过手机号查询业主
    public OwnerInfo getOwnerByPhone(String phone) {
        OwnerInfo owner = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT id, name, phone_number, account FROM owner_info WHERE phone_number = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, phone);
            rs = stmt.executeQuery();

            if (rs.next()) {
                owner = new OwnerInfo(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("phone_number"),
                    rs.getString("account")
                );
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error querying owner: " + e.getMessage(), e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return owner;
    }

    // 关闭数据库资源
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            Log.e(TAG, "Error closing database resources", e);
        }
    }

    // 测试数据库连接
    public boolean testConnection() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        boolean isConnected = false;
        
        try {
            Log.i(TAG, "开始测试数据库连接...");
            conn = getConnection();
            
            if (conn != null && !conn.isClosed()) {
                Log.i(TAG, "数据库连接成功！数据库产品: " + conn.getMetaData().getDatabaseProductName() 
                    + ", 版本: " + conn.getMetaData().getDatabaseProductVersion());
                
                // 执行简单查询验证连接
                stmt = conn.createStatement();
                Log.i(TAG, "执行测试查询...");
                rs = stmt.executeQuery("SELECT 1");
                
                if (rs.next()) {
                    Log.i(TAG, "数据库查询测试成功！结果: " + rs.getInt(1));
                    isConnected = true;
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "数据库连接测试失败: " + e.getMessage() 
                + "\nSQL状态: " + e.getSQLState() 
                + "\n错误代码: " + e.getErrorCode(), e);
            
            // 获取所有链接的异常
            SQLException nextEx = e.getNextException();
            while (nextEx != null) {
                Log.e(TAG, "链接的SQL异常: " + nextEx.getMessage()
                    + "\nSQL状态: " + nextEx.getSQLState()
                    + "\n错误代码: " + nextEx.getErrorCode());
                nextEx = nextEx.getNextException();
            }
            
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "发生未知错误: " + e.getClass().getName() + ": " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            try {
                Log.i(TAG, "关闭数据库资源...");
                closeResources(conn, stmt, rs);
            } catch (Exception e) {
                Log.e(TAG, "关闭资源时出错: " + e.getMessage(), e);
            }
        }
        
        Log.i(TAG, "数据库连接测试结果: " + (isConnected ? "成功" : "失败"));
        return isConnected;
    }
} 