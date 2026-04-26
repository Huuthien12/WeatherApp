package com.example.myapplicationooo;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FiveDayForecastActivity extends AppCompatActivity {

    private String cityName;
    private final String API_KEY = "22b3362b92ebbde69c2e8145c14d2da2";
    private RecyclerView rvForecast;
    private FiveDayForecastAdapter adapter;
    private List<FiveDayForecastAdapter.FiveDayItem> forecastList = new ArrayList<>();
    private TempCurveView tempCurveView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_five_day_forecast);

        cityName = getIntent().getStringExtra("city");
        if (cityName == null) cityName = "Hanoi";

        ImageButton btnBack = findViewById(R.id.btnBack);
        rvForecast = findViewById(R.id.rvFiveDayForecast);
        tempCurveView = findViewById(R.id.tempCurveView);

        btnBack.setOnClickListener(v -> finish());

        rvForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new FiveDayForecastAdapter(forecastList);
        rvForecast.setAdapter(adapter);

        getFiveDayForecast();
    }

    private void getFiveDayForecast() {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/forecast?q=" + cityName + "&units=metric&appid=" + API_KEY);
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

        Map<String, FiveDayForecastAdapter.FiveDayItem> dailyMap = new LinkedHashMap<>();
        SimpleDateFormat sdfKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfDay = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat sdfDate = new SimpleDateFormat("M/dd", Locale.getDefault());

        List<Double> maxTemps = new ArrayList<>();
        List<Double> minTemps = new ArrayList<>();

        for (ForecastItem item : data.getList()) {
            Date date = new Date(item.getDt() * 1000);
            String key = sdfKey.format(date);
            double temp = item.getMain().getTemp();

            if (!dailyMap.containsKey(key)) {
                String dayName = sdfDay.format(date);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                Calendar today = Calendar.getInstance();
                if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) dayName = "Today";
                else if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) + 1) dayName = "Tomorrow";

                FiveDayForecastAdapter.FiveDayItem dailyItem = new FiveDayForecastAdapter.FiveDayItem(
                        dayName,
                        sdfDate.format(date),
                        temp,
                        temp,
                        item.getWeather().get(0).getIcon(),
                        (int)(item.getDt() % 3 + 1)
                );
                dailyMap.put(key, dailyItem);
            } else {
                FiveDayForecastAdapter.FiveDayItem dailyItem = dailyMap.get(key);
                if (temp < dailyItem.getMinTemp()) {
                    dailyItem.setMinTemp(temp);
                }
                if (temp > dailyItem.getMaxTemp()) {
                    dailyItem.setMaxTemp(temp);
                }
            }
        }

        forecastList.clear();
        forecastList.addAll(dailyMap.values());
        
        for (FiveDayForecastAdapter.FiveDayItem item : forecastList) {
            maxTemps.add(item.getMaxTemp());
            minTemps.add(item.getMinTemp());
        }

        // Gửi dữ liệu vào View biểu đồ. Chiều rộng item là 90dp = ~270px (tính tương đối)
        tempCurveView.setData(maxTemps, minTemps, (int) (90 * getResources().getDisplayMetrics().density));
        
        adapter.notifyDataSetChanged();
    }
}
