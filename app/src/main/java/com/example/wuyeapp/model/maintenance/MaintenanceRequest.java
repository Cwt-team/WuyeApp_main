package com.example.wuyeapp.model.maintenance;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class MaintenanceRequest {
    private Long id;
    private String requestNumber;
    
    @SerializedName("community_id")
    private Integer communityId;
    
    @SerializedName("house_id")
    private Integer houseId;
    
    @SerializedName("reporter_name")
    private String reporterName;
    
    @SerializedName("reporter_phone")
    private String reporterPhone;
    
    private String title;
    private String description;
    private String type;
    private String priority;
    
    @SerializedName("expected_time")
    private Date expectedTime;
    
    private String images;
    private String status;
    
    @SerializedName("report_time")
    private Date reportTime;
    
    // 处理人姓名
    private String handlerName;
    
    // 处理人电话
    private String handlerPhone;
    
    // 处理时间
    @SerializedName("process_time")
    private Date processTime;
    
    // 完成时间
    @SerializedName("complete_time")
    private Date completeTime;
    
    // 评价分数
    @SerializedName("evaluation_score")
    private Integer evaluationScore;
    
    // 评价内容
    @SerializedName("evaluation_content")
    private String evaluationContent;
    
    // 评价时间
    @SerializedName("evaluation_time")
    private Date evaluationTime;
    
    // 添加更完整的构造函数
    public MaintenanceRequest(String title, String description, String type, String priority, 
                             Integer communityId, Integer houseId, String reporterName, String reporterPhone) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.priority = priority;
        this.communityId = communityId;
        this.houseId = houseId;
        this.reporterName = reporterName;
        this.reporterPhone = reporterPhone;
    }
    
    // 添加所有必要的getter和setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getRequestNumber() {
        return requestNumber;
    }
    
    public void setRequestNumber(String requestNumber) {
        this.requestNumber = requestNumber;
    }
    
    public Integer getCommunityId() {
        return communityId;
    }
    
    public void setCommunityId(Integer communityId) {
        this.communityId = communityId;
    }
    
    public Integer getHouseId() {
        return houseId;
    }
    
    public void setHouseId(Integer houseId) {
        this.houseId = houseId;
    }
    
    public String getReporterName() {
        return reporterName;
    }
    
    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }
    
    public String getReporterPhone() {
        return reporterPhone;
    }
    
    public void setReporterPhone(String reporterPhone) {
        this.reporterPhone = reporterPhone;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public Date getExpectedTime() {
        return expectedTime;
    }
    
    public void setExpectedTime(Date expectedTime) {
        this.expectedTime = expectedTime;
    }
    
    public String getImages() {
        return images;
    }
    
    public void setImages(String images) {
        this.images = images;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Date getReportTime() {
        return reportTime;
    }
    
    public void setReportTime(Date reportTime) {
        this.reportTime = reportTime;
    }
    
    // 处理人姓名
    public String getHandlerName() {
        return handlerName;
    }
    
    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }
    
    // 处理人电话
    public String getHandlerPhone() {
        return handlerPhone;
    }
    
    public void setHandlerPhone(String handlerPhone) {
        this.handlerPhone = handlerPhone;
    }
    
    // 处理时间
    public Date getProcessTime() {
        return processTime;
    }
    
    public void setProcessTime(Date processTime) {
        this.processTime = processTime;
    }
    
    // 完成时间
    public Date getCompleteTime() {
        return completeTime;
    }
    
    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }
    
    // 评价分数
    public Integer getEvaluationScore() {
        return evaluationScore;
    }
    
    public void setEvaluationScore(Integer evaluationScore) {
        this.evaluationScore = evaluationScore;
    }
    
    // 评价内容
    public String getEvaluationContent() {
        return evaluationContent;
    }
    
    public void setEvaluationContent(String evaluationContent) {
        this.evaluationContent = evaluationContent;
    }
    
    // 评价时间
    public Date getEvaluationTime() {
        return evaluationTime;
    }
    
    public void setEvaluationTime(Date evaluationTime) {
        this.evaluationTime = evaluationTime;
    }
}
