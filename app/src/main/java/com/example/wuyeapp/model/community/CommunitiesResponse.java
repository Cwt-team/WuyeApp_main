package com.example.wuyeapp.model.community;

import com.example.wuyeapp.model.base.BaseResponse;

import java.util.List;

/**
 * 社区列表响应类
 */
public class CommunitiesResponse extends BaseResponse {
    private List<Community> communities;
    
    public CommunitiesResponse() {
        super();
    }
    
    public List<Community> getCommunities() {
        return communities;
    }
    
    public void setCommunities(List<Community> communities) {
        this.communities = communities;
    }
} 