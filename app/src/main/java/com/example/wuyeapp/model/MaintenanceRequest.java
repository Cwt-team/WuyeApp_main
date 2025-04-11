package com.example.wuyeapp.model;

public class MaintenanceRequest {
    private String title;
    private String description;
    private String type;
    private String priority;

    public MaintenanceRequest(String title, String description, String type, String priority) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.priority = priority;
    }

    // Getters and Setters
}
