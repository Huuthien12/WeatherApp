package com.example.myapplicationooo;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "weather_cache")
public class WeatherCacheEntity {
    @PrimaryKey
    @NonNull
    private String cityName;
    private String jsonResponse;
    private long lastUpdated;

    public WeatherCacheEntity(@NonNull String cityName, String jsonResponse, long lastUpdated) {
        this.cityName = cityName;
        this.jsonResponse = jsonResponse;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    @NonNull public String getCityName() { return cityName; }
    public String getJsonResponse() { return jsonResponse; }
    public long getLastUpdated() { return lastUpdated; }
}