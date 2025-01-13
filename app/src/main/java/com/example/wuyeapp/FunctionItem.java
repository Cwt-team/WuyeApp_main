package com.example.wuyeapp;

public class FunctionItem {
    private String name;
    private int iconResId;
    private boolean selected;

    public FunctionItem(String name, int iconResId, boolean selected) {
        this.name = name;
        this.iconResId = iconResId;
        this.selected = selected;
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
    }
} 