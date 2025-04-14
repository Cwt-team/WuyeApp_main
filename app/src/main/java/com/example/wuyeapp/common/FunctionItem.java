package com.example.wuyeapp.common;

public class FunctionItem {
    private String name;
    private int iconResId;
    private boolean selected;
    private boolean isHomeApp;

    public FunctionItem(String name, int iconResId, boolean selected) {
        this.name = name;
        this.iconResId = iconResId;
        this.selected = selected;
        this.isHomeApp = selected;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        this.isHomeApp = selected;
    }

    public boolean isHomeApp() {
        return isHomeApp;
    }

    public void setHomeApp(boolean homeApp) {
        this.isHomeApp = homeApp;
    }
} 