package com.example.myapplicationooo;

import com.google.firebase.Timestamp;

public class TripPlan {
    private String id;
    private String userId;
    private String destination;
    private String activityType; 
    private String userPrompt;
    private Timestamp createdAt;
    private String aiRecommendation;
    private String status; 
    private int comfortScore;    // 1-10
    private int suitabilityScore; // 1-10

    public TripPlan() {}

    public TripPlan(String userId, String destination, String activityType, String userPrompt) {
        this.userId = userId;
        this.destination = destination;
        this.activityType = activityType;
        this.userPrompt = userPrompt;
        this.createdAt = Timestamp.now();
        this.status = "Pending";
        this.comfortScore = 0;
        this.suitabilityScore = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public String getUserPrompt() { return userPrompt; }
    public void setUserPrompt(String userPrompt) { this.userPrompt = userPrompt; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getAiRecommendation() { return aiRecommendation; }
    public void setAiRecommendation(String aiRecommendation) { this.aiRecommendation = aiRecommendation; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getComfortScore() { return comfortScore; }
    public void setComfortScore(int comfortScore) { this.comfortScore = comfortScore; }
    public int getSuitabilityScore() { return suitabilityScore; }
    public void setSuitabilityScore(int suitabilityScore) { this.suitabilityScore = suitabilityScore; }
}
