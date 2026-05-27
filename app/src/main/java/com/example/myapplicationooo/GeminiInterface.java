package com.example.myapplicationooo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiInterface {
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    Call<GeminiResponse> getResponse(@Query("key") String apiKey, @Body GeminiRequest request);
}
