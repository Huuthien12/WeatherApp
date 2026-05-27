package com.example.myapplicationooo;

import java.util.List;

public class TripWeatherAnalysis {
    private ForecastResponse forecast;
    private AQIResponse aqi;
    private MarineWeatherResponse marine;
    private String activityType;

    public TripWeatherAnalysis(ForecastResponse forecast, AQIResponse aqi, MarineWeatherResponse marine, String activityType) {
        this.forecast = forecast;
        this.aqi = aqi;
        this.marine = marine;
        this.activityType = activityType;
    }

    // Getters
    public ForecastResponse getForecast() { return forecast; }
    public AQIResponse getAqi() { return aqi; }
    public MarineWeatherResponse getMarine() { return marine; }
    public String getActivityType() { return activityType; }
}
