package com.example.wuyeapp.model;

/**
 * 人脸上传响应类
 */
public class FaceUploadResponse extends BaseResponse {
    private FaceInfo faceInfo;

    public FaceUploadResponse() {
        super();
    }
    
    public FaceInfo getFaceInfo() {
        return faceInfo;
    }
    
    public void setFaceInfo(FaceInfo faceInfo) {
        this.faceInfo = faceInfo;
        setData(faceInfo); // 设置父类的data字段
    }
    
    // 为了兼容现有代码，保留getData方法，但返回更具体的类型
    @Override
    public FaceInfo getData() {
        return faceInfo;
    }
    
    @Override
    public void setData(Object data) {
        super.setData(data);
        if (data instanceof FaceInfo) {
            this.faceInfo = (FaceInfo) data;
        }
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