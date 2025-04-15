package com.example.wuyeapp.model.maintenance;

import com.example.wuyeapp.model.base.BaseResponse;
import java.util.List;

public class MaintenanceListResponse extends BaseResponse<MaintenanceListResponse.MaintenanceListData> {
    
    public static class MaintenanceListData {
        private List<MaintenanceRequest> items;
        private int total;
        
        public List<MaintenanceRequest> getItems() {
            return items;
        }
        
        public void setItems(List<MaintenanceRequest> items) {
            this.items = items;
        }
        
        public int getTotal() {
            return total;
        }
        
        public void setTotal(int total) {
            this.total = total;
        }
    }
} 