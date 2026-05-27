package com.example.myapplicationooo;

import com.google.firebase.Timestamp;

public class PlanModel {
    private String id;
    private String userId;
    private String title;
    private String cityName;
    private Timestamp planTime;
    private String weatherStatus;
    private String weatherIcon;
    private double temp;
    private String recommendation;
    private int statusType; // 0: Good, 1: Rain/Alert, 2: Others

    public PlanModel() {
        // Required for Firestore
    }

    public PlanModel(String userId, String title, String cityName, Timestamp planTime) {
        this.userId = userId;
        this.title = title;
        this.cityName = cityName;
        this.planTime = planTime;
        this.weatherStatus = "Checking...";
        this.recommendation = "";
        this.statusType = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public Timestamp getPlanTime() { return planTime; }
    public void setPlanTime(Timestamp planTime) { this.planTime = planTime; }
    public String getWeatherStatus() { return weatherStatus; }
    public void setWeatherStatus(String weatherStatus) { this.weatherStatus = weatherStatus; }
    public String getWeatherIcon() { return weatherIcon; }
    public void setWeatherIcon(String weatherIcon) { this.weatherIcon = weatherIcon; }
    public double getTemp() { return temp; }
    public void setTemp(double temp) { this.temp = temp; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public int getStatusType() { return statusType; }
    public void setStatusType(int statusType) { this.statusType = statusType; }
}
