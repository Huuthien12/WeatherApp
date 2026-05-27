package com.example.myapplicationooo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WeatherWorker extends Worker {

    private static final String TAG = "WeatherWorker";
    private static final String API_KEY = "22b3362b92ebbde69c2e8145c14d2da2";
    private final NotificationHelper notificationHelper;
    private final SharedPreferences prefs;

    public WeatherWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.notificationHelper = new NotificationHelper(context);
        this.prefs = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!prefs.getBoolean("smart_notifications", true)) {
            return Result.success();
        }

        String city = getInputData().getString("city");
        if (city == null) city = "Hanoi";

        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.name());
            
            // Fetch Current Weather
            URL currentUrl = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + encodedCity + "&units=metric&appid=" + API_KEY);
            HttpURLConnection currentConn = (HttpURLConnection) currentUrl.openConnection();
            WeatherResponse currentData = null;
            if (currentConn.getResponseCode() == 200) {
                currentData = new Gson().fromJson(new InputStreamReader(currentConn.getInputStream()), WeatherResponse.class);
            }

            // Fetch Forecast
            URL forecastUrl = new URL("https://api.openweathermap.org/data/2.5/forecast?q=" + encodedCity + "&units=metric&appid=" + API_KEY);
            HttpURLConnection forecastConn = (HttpURLConnection) forecastUrl.openConnection();
            ForecastResponse forecastData = null;
            if (forecastConn.getResponseCode() == 200) {
                forecastData = new Gson().fromJson(new InputStreamReader(forecastConn.getInputStream()), ForecastResponse.class);
            }

            if (currentData != null && forecastData != null) {
                processSmartAlerts(currentData, forecastData, city);
                return Result.success();
            }

        } catch (Exception e) {
            Log.e(TAG, "Worker failed: " + e.getMessage());
        }

        return Result.retry();
    }

    private void processSmartAlerts(WeatherResponse current, ForecastResponse forecast, String cityName) {
        // 1. Check for Rain
        NotificationRule.NotificationData rainAlert = NotificationRule.checkRain(forecast, cityName);
        if (rainAlert != null) {
            notificationHelper.showNotification(rainAlert.title, rainAlert.message);
            return; // Show only the most critical notification
        }

        // 2. Check for Wind
        NotificationRule.NotificationData windAlert = NotificationRule.checkWind(current);
        if (windAlert != null) {
            notificationHelper.showNotification(windAlert.title, windAlert.message);
            return;
        }

        // 3. Check for UV
        NotificationRule.NotificationData uvAlert = NotificationRule.checkUV(current);
        if (uvAlert != null) {
            notificationHelper.showNotification(uvAlert.title, uvAlert.message);
            return;
        }

        // 4. Check for Good Weather (Maybe only once a day?)
        NotificationRule.NotificationData goodWeather = NotificationRule.checkGoodWeather(current);
        if (goodWeather != null) {
            notificationHelper.showNotification(goodWeather.title, goodWeather.message);
        }
    }
}
