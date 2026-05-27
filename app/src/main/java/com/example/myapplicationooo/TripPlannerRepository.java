package com.example.myapplicationooo;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Response;

public class TripPlannerRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final String GEMINI_KEY = "AIzaSyBSmq8WK9acGudS-M9dFwaCA9V_o3_6bVk";

    private final String API_KEY = "22b3362b92ebbde69c2e8145c14d2da2";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public Query getMyPlans(String userId) {

        return db.collection("trip_plans")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    public void generatePlan(
            TripPlan plan,
            OnPlanGeneratedListener listener
    ) {

        executor.execute(() -> {

            try {

                // =========================
                // 1. WEATHER FORECAST
                // =========================

                String encodedCity =
                        URLEncoder.encode(
                                plan.getDestination(),
                                StandardCharsets.UTF_8.name()
                        );

                String weatherUrl =
                        "https://api.openweathermap.org/data/2.5/forecast?q="
                                + encodedCity
                                + "&units=metric&appid="
                                + API_KEY;

                HttpURLConnection conn =
                        (HttpURLConnection)
                                new URL(weatherUrl).openConnection();

                ForecastResponse forecast = null;

                if (conn.getResponseCode() == 200) {

                    forecast = new Gson().fromJson(
                            new InputStreamReader(conn.getInputStream()),
                            ForecastResponse.class
                    );
                }

                conn.disconnect();

                if (forecast == null
                        || forecast.getCity() == null) {

                    listener.onError(
                            "Không tìm thấy dữ liệu thời tiết."
                    );

                    return;
                }

                double lat =
                        forecast.getCity()
                                .getCoord()
                                .getLat();

                double lon =
                        forecast.getCity()
                                .getCoord()
                                .getLon();

                // =========================
                // 2. EXTRA DATA
                // =========================

                StringBuilder extraContext =
                        new StringBuilder();

                String activity =
                        plan.getActivityType()
                                .toLowerCase();

                // ===== AQI =====

                if (activity.contains("running")
                        || activity.contains("sports")) {

                    String aqiUrl =
                            "https://api.openweathermap.org/data/2.5/air_pollution?lat="
                                    + lat
                                    + "&lon="
                                    + lon
                                    + "&appid="
                                    + API_KEY;

                    HttpURLConnection aqiConn =
                            (HttpURLConnection)
                                    new URL(aqiUrl)
                                            .openConnection();

                    if (aqiConn.getResponseCode() == 200) {

                        AQIResponse aqiData =
                                new Gson().fromJson(
                                        new InputStreamReader(
                                                aqiConn.getInputStream()
                                        ),
                                        AQIResponse.class
                                );

                        if (aqiData != null
                                && aqiData.getList() != null
                                && !aqiData.getList().isEmpty()) {

                            int aqi =
                                    aqiData.getList()
                                            .get(0)
                                            .getMain()
                                            .getAqi();

                            extraContext.append(
                                    "\nAQI: "
                                            + aqi
                            );
                        }
                    }

                    aqiConn.disconnect();
                }

                // ===== MARINE =====

                if (activity.contains("beach")) {

                    String marineUrl =
                            "https://marine-api.open-meteo.com/v1/marine?latitude="
                                    + lat
                                    + "&longitude="
                                    + lon
                                    + "&current=wave_height,sea_surface_temperature";

                    HttpURLConnection marineConn =
                            (HttpURLConnection)
                                    new URL(marineUrl)
                                            .openConnection();

                    if (marineConn.getResponseCode() == 200) {

                        MarineWeatherResponse marineData =
                                new Gson().fromJson(
                                        new InputStreamReader(
                                                marineConn.getInputStream()
                                        ),
                                        MarineWeatherResponse.class
                                );

                        if (marineData != null
                                && marineData.getCurrent() != null) {

                            extraContext.append(
                                    "\nWave Height: "
                                            + marineData.getCurrent()
                                            .getWaveHeight()
                                            + "m"
                            );

                            extraContext.append(
                                    "\nSea Temp: "
                                            + marineData.getCurrent()
                                            .getSeaTemp()
                                            + "°C"
                            );
                        }
                    }

                    marineConn.disconnect();
                }

                // =========================
                // 3. SHORT FORECAST
                // =========================

                StringBuilder shortForecast =
                        new StringBuilder();

                List<ForecastItem> forecastList =
                        forecast.getList();

                int limit =
                        Math.min(
                                forecastList.size(),
                                6
                        );

                for (int i = 0; i < limit; i++) {

                    ForecastItem item =
                            forecastList.get(i);

                    shortForecast.append(
                            "\nTime: "
                                    + item.getDtTxt()
                    );

                    shortForecast.append(
                            "\nTemp: "
                                    + item.getMain().getTemp()
                                    + "°C"
                    );

                    shortForecast.append(
                            "\nHumidity: "
                                    + item.getMain().getHumidity()
                                    + "%"
                    );

                    shortForecast.append(
                            "\nWeather: "
                                    + item.getWeather()
                                    .get(0)
                                    .getDescription()
                    );

                    shortForecast.append("\n");
                }

                // =========================
                // 4. GEMINI PROMPT
                // =========================

                String prompt =
                        "Bạn là AI tư vấn du lịch thời tiết.\n\n"

                                + "Địa điểm: "
                                + plan.getDestination()

                                + "\nHoạt động: "
                                + plan.getActivityType()

                                + "\nGhi chú người dùng: "
                                + plan.getUserPrompt()

                                + "\n\nDữ liệu thời tiết:"
                                + shortForecast

                                + "\n\nThông tin thêm:"
                                + extraContext

                                + "\n\nHãy trả lời bằng tiếng Việt:"
                                + "\n1. Thời điểm đẹp nhất"
                                + "\n2. Điểm phù hợp 1-10"
                                + "\n3. Trang phục"
                                + "\n4. Cảnh báo"
                                + "\n5. Tổng kết ngắn";

                Log.d("PROMPT_SIZE",
                        String.valueOf(prompt.length()));

                // =========================
                // 5. GEMINI API
                // =========================

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://generativelanguage.googleapis.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                GeminiInterface api =
                        retrofit.create(GeminiInterface.class);

                retrofit2.Call<GeminiResponse> call =
                        api.getResponse(
                                GEMINI_KEY,
                                new GeminiRequest(prompt)
                        );

                Response<GeminiResponse> response =
                        call.execute();

                // =========================
                // 6. HANDLE RESPONSE
                // =========================

                if (response.isSuccessful()
                        && response.body() != null) {

                    String aiText =
                            response.body()
                                    .getResponseText();

                    plan.setAiRecommendation(aiText);

                    plan.setStatus("Completed");

                    if (AuthManager.getInstance()
                            .isLoggedIn()) {

                        db.collection("trip_plans")
                                .add(plan)
                                .addOnSuccessListener(doc -> {

                                    plan.setId(doc.getId());

                                    doc.update(
                                            "id",
                                            plan.getId()
                                    );

                                    listener.onSuccess(plan);
                                })
                                .addOnFailureListener(e -> {
                                    listener.onError(
                                            e.getMessage()
                                    );
                                });

                    } else {

                        listener.onSuccess(plan);
                    }

                } else {

                    String error = "Gemini API Error";

                    try {

                        if (response.errorBody() != null) {

                            error += "\n"
                                    + response.errorBody()
                                    .string();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    listener.onError(error);
                }

            } catch (Exception e) {

                e.printStackTrace();

                listener.onError(
                        "Error: " + e.getMessage()
                );
            }
        });
    }

    // =========================
    // DELETE PLAN
    // =========================

    public void deletePlan(
            String id,
            OnPlanActionListener listener
    ) {

        db.collection("trip_plans")
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid ->
                        listener.onSuccess()
                )
                .addOnFailureListener(
                        listener::onFailure
                );
    }

    // =========================
    // INTERFACES
    // =========================

    public interface OnPlanGeneratedListener {

        void onSuccess(TripPlan plan);

        void onError(String error);
    }

    public interface OnPlanActionListener {

        void onSuccess();

        void onFailure(Exception e);
    }
}