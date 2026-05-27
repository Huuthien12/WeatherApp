package com.example.myapplicationooo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MarineRepository {
    private final OpenMeteoMarineInterface marineApi;
    private final OpenMeteoTideInterface tideApi;

    public MarineRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://marine-api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        marineApi = retrofit.create(OpenMeteoMarineInterface.class);

        Retrofit tideRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        tideApi = tideRetrofit.create(OpenMeteoTideInterface.class);
    }

    public void fetchMarineData(double lat, double lon, Callback<MarineWeatherResponse> callback) {
        String params = "wave_height,wave_direction,wave_period,sea_surface_temperature,wind_speed_10m,wind_direction_10m";
        marineApi.getMarineWeather(lat, lon, params, "auto").enqueue(callback);
    }

    public void fetchTideData(double lat, double lon, Callback<TideResponse> callback) {
        // Sử dụng sea_level cho dữ liệu thủy triều
        tideApi.getTideData(lat, lon, "temperature_2m", "auto").enqueue(callback);
    }
}
