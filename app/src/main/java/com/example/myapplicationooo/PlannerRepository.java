package com.example.myapplicationooo;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlannerRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final String API_KEY = "22b3362b92ebbde69c2e8145c14d2da2";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Query getPlans() {
        if (auth.getUid() == null) return null;
        return db.collection("planners")
                .whereEqualTo("userId", auth.getUid())
                .orderBy("planTime", Query.Direction.ASCENDING);
    }

    public void addPlan(PlanModel plan, OnPlanActionListener listener) {
        executor.execute(() -> {
            analyzeWeatherForPlan(plan);
            db.collection("planners").add(plan)
                    .addOnSuccessListener(documentReference -> {
                        plan.setId(documentReference.getId());
                        documentReference.update("id", plan.getId());
                        listener.onSuccess();
                    })
                    .addOnFailureListener(listener::onFailure);
        });
    }

    public void deletePlan(String id, OnPlanActionListener listener) {
        db.collection("planners").document(id).delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    private void analyzeWeatherForPlan(PlanModel plan) {
        try {
            String encodedCity = URLEncoder.encode(plan.getCityName(), StandardCharsets.UTF_8.name());
            URL url = new URL("https://api.openweathermap.org/data/2.5/forecast?q=" + encodedCity + "&units=metric&appid=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == 200) {
                ForecastResponse data = new Gson().fromJson(new InputStreamReader(conn.getInputStream()), ForecastResponse.class);
                
                long planTimeSecs = plan.getPlanTime().getSeconds();
                ForecastItem closest = null;
                long minDiff = Long.MAX_VALUE;

                for (ForecastItem item : data.getList()) {
                    long diff = Math.abs(item.getDt() - planTimeSecs);
                    if (diff < minDiff) {
                        minDiff = diff;
                        closest = item;
                    }
                }

                if (closest != null) {
                    plan.setTemp(closest.getMain().getTemp());
                    plan.setWeatherIcon(closest.getWeather().get(0).getIcon());
                    String main = closest.getWeather().get(0).getMain().toLowerCase();

                    if (main.contains("rain") || main.contains("thunder") || main.contains("snow")) {
                        plan.setWeatherStatus("Rain/Alert Warning");
                        plan.setStatusType(1);
                        plan.setRecommendation("Dự báo có thời tiết xấu. Bạn nên cân nhắc dời lịch hoặc chuẩn bị vật dụng cần thiết.");
                    } else if (closest.getMain().getTemp() > 35) {
                        plan.setWeatherStatus("Extreme Heat");
                        plan.setStatusType(2);
                        plan.setRecommendation("Trời rất nóng. Hãy chú ý sức khỏe và bù nước đầy đủ.");
                    } else {
                        plan.setWeatherStatus("Good Weather");
                        plan.setStatusType(0);
                        plan.setRecommendation("Thời tiết rất đẹp cho kế hoạch của bạn. Chúc bạn vui vẻ!");
                    }
                }
            }
        } catch (Exception e) {
            Log.e("PlannerRepo", "Weather analysis failed: " + e.getMessage());
            plan.setWeatherStatus("Unknown");
        }
    }

    public interface OnPlanActionListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}
