package com.example.myapplicationooo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface DeepSeekInterface {
    // Cập nhật endpoint v1 chuẩn để tránh lỗi 404
    @POST("v1/chat/completions")
    Call<DeepSeekResponse> getResponse(
        @Header("Authorization") String authToken,
        @Body DeepSeekRequest request
    );
}
