package com.example.myapplicationooo;

public class FeatureModel {
    private String title;
    private String description;
    private int iconRes;
    private boolean isPremium; // true if login required

    public FeatureModel(String title, String description, int iconRes, boolean isPremium) {
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
        this.isPremium = isPremium;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getIconRes() { return iconRes; }
    public boolean isPremium() { return isPremium; }
}
