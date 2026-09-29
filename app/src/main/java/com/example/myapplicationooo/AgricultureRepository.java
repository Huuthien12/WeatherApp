package com.example.myapplicationooo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AgricultureRepository {
    private final OpenMeteoAgricultureInterface api;

    public AgricultureRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(OpenMeteoAgricultureInterface.class);
    }

    public void fetch(double lat, double lon, Callback<AgricultureWeatherResponse> callback) {
        String current = "temperature_2m,relative_humidity_2m,precipitation,rain,wind_speed_10m";
        Call<AgricultureWeatherResponse> call = api.getAgricultureWeather(lat, lon, current, "auto");
        call.enqueue(callback);
    }
}
