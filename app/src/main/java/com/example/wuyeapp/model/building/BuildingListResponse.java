package com.example.wuyeapp.model.building;
import com.example.wuyeapp.model.base.BaseResponse;
import java.util.List;

public class BuildingListResponse extends BaseResponse {
    private List<Building> buildings;
    public List<Building> getBuildings() { return buildings; }
    public void setBuildings(List<Building> buildings) { this.buildings = buildings; }
    public static class Building {
        private int id;
        private String buildingNumber;
        private String name;
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getBuildingNumber() { return buildingNumber; }
        public void setBuildingNumber(String buildingNumber) { this.buildingNumber = buildingNumber; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
} 