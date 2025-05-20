package com.example.wuyeapp.model.unit;
import com.example.wuyeapp.model.base.BaseResponse;
import java.util.List;

public class UnitListResponse extends BaseResponse {
    private List<Unit> units;
    public List<Unit> getUnits() { return units; }
    public void setUnits(List<Unit> units) { this.units = units; }
    public static class Unit {
        private int id;
        private String unitNumber;
        private String name;
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getUnitNumber() { return unitNumber; }
        public void setUnitNumber(String unitNumber) { this.unitNumber = unitNumber; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
} 