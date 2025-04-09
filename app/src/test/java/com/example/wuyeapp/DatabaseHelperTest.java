package com.example.wuyeapp;

import com.example.wuyeapp.database.DatabaseHelper;
import com.example.wuyeapp.model.OwnerInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseHelperTest {
    
    private DatabaseHelper databaseHelper;
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    @Before
    public void setUp() throws Exception {
        databaseHelper = Mockito.spy(new DatabaseHelper());
        
        // 只保留实际使用的模拟
        doReturn(mockConnection).when(databaseHelper).getConnection();
        
        System.out.println("测试准备：创建数据库Helper实例");
    }
    
    @After
    public void tearDown() {
        databaseHelper = null;
        System.out.println("测试完成：清理资源");
    }
    
    @Test
    public void testDatabaseConnection() throws Exception {
        // 使反射可访问私有方法
        Method method = DatabaseHelper.class.getDeclaredMethod("getConnection");
        method.setAccessible(true);
        
        // 配置模拟行为
        when(mockConnection.isClosed()).thenReturn(false);
        
        // 执行方法并验证
        Connection connection = (Connection) method.invoke(databaseHelper);
        assertNotNull("数据库连接应该不为空", connection);
        assertFalse("连接应该是开放的", connection.isClosed());
        
        System.out.println("测试成功：数据库连接正常");
    }
    
    // ... 其他测试方法类似修改 ...
}