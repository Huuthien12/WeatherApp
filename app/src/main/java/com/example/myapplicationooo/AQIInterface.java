package com.example.myapplicationooo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AQIInterface {
    @GET("data/2.5/air_pollution")
    Call<AQIResponse> getAirQuality(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey
    );
}
