package com.example.myapplicationooo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenMeteoMarineInterface {
    @GET("v1/marine")
    Call<MarineWeatherResponse> getMarineWeather(
            @Query("latitude") double lat,
            @Query("longitude") double lon,
            @Query("current") String params,
            @Query("timezone") String timezone
    );
}
