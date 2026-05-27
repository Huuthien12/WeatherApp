package com.example.myapplicationooo;

import retrofit2.Callback;

public class AQIRepository {
    private final AQIInterface api;
    private final String API_KEY = "22b3362b92ebbde69c2e8145c14d2da2";

    public AQIRepository() {
        api = WeatherApiClient.getClient().create(AQIInterface.class);
    }

    public void fetchAQI(double lat, double lon, Callback<AQIResponse> callback) {
        api.getAirQuality(lat, lon, API_KEY).enqueue(callback);
    }
}
