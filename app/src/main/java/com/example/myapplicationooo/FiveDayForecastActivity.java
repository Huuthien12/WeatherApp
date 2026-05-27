package com.example.myapplicationooo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class FiveDayForecastActivity extends AppCompatActivity {

    private String cityName;
    private double lat, lon;
    private boolean isCurrentLocation;
    private final String API_KEY = "22b3362b92ebbde69c2e8145c14d2da2";
    private RecyclerView rvForecast;
    private FiveDayForecastAdapter adapter;
    private List<FiveDayForecastAdapter.FiveDayItem> forecastList = new ArrayList<>();
    private TempCurveView tempCurveView;
    private TextView tvTitle;
    private String unit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_five_day_forecast);

        cityName = getIntent().getStringExtra("city");
        lat = getIntent().getDoubleExtra("lat", 0);
        lon = getIntent().getDoubleExtra("lon", 0);
        isCurrentLocation = getIntent().getBooleanExtra("is_current", false);

        if (cityName == null) cityName = "Hanoi";

        SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        unit = prefs.getString("temp_unit", "C");

        ImageButton btnBack = findViewById(R.id.btnBack);
        rvForecast = findViewById(R.id.rvFiveDayForecast);
        tempCurveView = findViewById(R.id.tempCurveView);
        tvTitle = findViewById(R.id.tvTitle);

        btnBack.setOnClickListener(v -> finish());

        rvForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new FiveDayForecastAdapter(forecastList);
        adapter.setUnit(unit);
        rvForecast.setAdapter(adapter);

        rvForecast.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int scrollX = recyclerView.computeHorizontalScrollOffset();
                tempCurveView.setScrollOffset(scrollX);
            }
        });

        getFiveDayForecast();
    }

    private void getFiveDayForecast() {
        new Thread(() -> {
            try {
                String urlString;
                if (isCurrentLocation && lat != 0 && lon != 0) {
                    urlString = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + API_KEY;
                } else {
                    urlString = "https://api.openweathermap.org/data/2.5/forecast?q=" + URLEncoder.encode(cityName, "UTF-8") + "&units=metric&appid=" + API_KEY;
                }

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == 200) {
                    ForecastResponse data = new Gson().fromJson(new InputStreamReader(conn.getInputStream()), ForecastResponse.class);
                    runOnUiThread(() -> processForecastData(data));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void processForecastData(ForecastResponse data) {
        if (data == null || data.getList() == null) return;

        if (data.getCity() != null) {
            tvTitle.setText(data.getCity().getName());
        }

        Map<String, FiveDayForecastAdapter.FiveDayItem> dailyMap = new LinkedHashMap<>();
        int timezoneOffset = data.getCity().getTimezone();
        TimeZone cityTimeZone = TimeZone.getTimeZone("GMT" + (timezoneOffset >= 0 ? "+" : "") + (timezoneOffset / 3600));

        SimpleDateFormat sdfKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdfKey.setTimeZone(cityTimeZone);
        
        SimpleDateFormat sdfDay = new SimpleDateFormat("EEE", Locale.getDefault());
        sdfDay.setTimeZone(cityTimeZone);
        
        SimpleDateFormat sdfDate = new SimpleDateFormat("M/dd", Locale.getDefault());
        sdfDate.setTimeZone(cityTimeZone);

        for (ForecastItem item : data.getList()) {
            Date date = new Date(item.getDt() * 1000);
            String key = sdfKey.format(date);
            // Use temp as base for min/max if temp_min/max are too interval-specific
            double currentTemp = item.getMain().getTemp();
            double tempMax = item.getMain().getTempMax();
            double tempMin = item.getMain().getTempMin();
            
            // Just to be sure, incorporate currentTemp
            tempMax = Math.max(tempMax, currentTemp);
            tempMin = Math.min(tempMin, currentTemp);

            if (!dailyMap.containsKey(key)) {
                String dayName = sdfDay.format(date);
                Calendar cal = Calendar.getInstance(cityTimeZone);
                cal.setTime(date);
                Calendar today = Calendar.getInstance(cityTimeZone);
                
                if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) && cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                    dayName = "Today";
                } else if (isTomorrow(cal, today)) {
                    dayName = "Tomorrow";
                }

                FiveDayForecastAdapter.FiveDayItem dailyItem = new FiveDayForecastAdapter.FiveDayItem(
                        dayName,
                        sdfDate.format(date),
                        tempMin,
                        tempMax,
                        item.getWeather().get(0).getIcon(),
                        (int)(Math.random() * 5 + 1) // Randomized wind force as example
                );
                dailyMap.put(key, dailyItem);
            } else {
                FiveDayForecastAdapter.FiveDayItem dailyItem = dailyMap.get(key);
                if (tempMin < dailyItem.getMinTemp()) {
                    dailyItem.setMinTemp(tempMin);
                }
                if (tempMax > dailyItem.getMaxTemp()) {
                    dailyItem.setMaxTemp(tempMax);
                }
            }
        }

        forecastList.clear();
        forecastList.addAll(dailyMap.values());
        
        List<Double> maxTemps = new ArrayList<>();
        List<Double> minTemps = new ArrayList<>();
        for (FiveDayForecastAdapter.FiveDayItem item : forecastList) {
            maxTemps.add(item.getMaxTemp());
            minTemps.add(item.getMinTemp());
        }

        int itemWidthPx = (int) (90 * getResources().getDisplayMetrics().density);
        tempCurveView.setData(maxTemps, minTemps, itemWidthPx);
        
        adapter.notifyDataSetChanged();
    }
    
    private boolean isTomorrow(Calendar cal, Calendar today) {
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        return cal.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR) && cal.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR);
    }
}
