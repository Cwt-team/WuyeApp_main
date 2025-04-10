package com.example.wuyeapp.model;

/**
 * 人脸上传响应类
 */
public class FaceUploadResponse {
    private boolean success;
    private String message;
    private FaceInfo data;

    public FaceUploadResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FaceInfo getData() {
        return data;
    }

    public void setData(FaceInfo data) {
        this.data = data;
    }

    /**
     * 人脸信息内部类
     */
    public static class FaceInfo {
        private long id;
        private String imageUrl;
        private int status; // 0-待审核, 1-已通过, 2-未通过

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
} 