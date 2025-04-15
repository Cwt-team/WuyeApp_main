package com.example.wuyeapp.model.maintenance;

/**
 * 维修服务数据模型
 */
public class Maintenance {
    private Long id;
    private String type; // 维修类型：公共设施、个人住所等
    private String status; // 维修状态
    
    // 构造函数
    public Maintenance() {
    }
    
    public Maintenance(Long id, String type, String status) {
        this.id = id;
        this.type = type;
        this.status = status;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}