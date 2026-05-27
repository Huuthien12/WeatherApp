package com.example.myapplicationooo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenMeteoTideInterface {
    // Lưu ý: Open-Meteo cung cấp dữ liệu mực nước biển/thủy triều qua endpoint này
    @GET("v1/forecast")
    Call<TideResponse> getTideData(
            @Query("latitude") double lat,
            @Query("longitude") double lon,
            @Query("hourly") String params,
            @Query("timezone") String timezone
    );
}
