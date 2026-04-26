package com.example.myapplicationooo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class WeatherFragment extends Fragment {

    private String cityName;
    private boolean isCurrentLocation;
    private final String API_KEY = "22b3362b92ebbde69c2e8145c14d2da2";

    private TextView tvCity, tvTemp, tvDesc, tvMinMax, tvWindSpeed, tvWindDir, tvHum, tvPress, tvUV, tvUVIndex, tvFeels, tvSunset;
    private ProgressBar pbUV, pbHum, pbFeels, pbPress, pbSunset;
    private ImageView imgIcon, imgCurrentLoc, imgCompassArrow;
    private View viewCityDot;
    private RecyclerView rvForecast;
    private LinearLayout layoutDailyRows;
    private Button btn5DayForecast;
    private SharedPreferences prefs;

    public static WeatherFragment newInstance(String cityName, boolean isCurrentLocation) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putString("city", cityName);
        args.putBoolean("is_current", isCurrentLocation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cityName = getArguments().getString("city");
            isCurrentLocation = getArguments().getBoolean("is_current");
        }
        prefs = getActivity().getSharedPreferences("WeatherPrefs", android.content.Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        tvCity = view.findViewById(R.id.tvCityName);
        tvTemp = view.findViewById(R.id.tvTemperature);
        tvDesc = view.findViewById(R.id.tvDescription);
        tvMinMax = view.findViewById(R.id.tvTempMinMax);
        tvWindSpeed = view.findViewById(R.id.tvWindSpeed);
        tvWindDir = view.findViewById(R.id.tvWindDirection);
        tvHum = view.findViewById(R.id.tvHumidity);
        tvPress = view.findViewById(R.id.tvPressure);
        tvUV = view.findViewById(R.id.tvUV);
        tvUVIndex = view.findViewById(R.id.tvUVIndex);
        tvFeels = view.findViewById(R.id.tvFeelsLike);
        tvSunset = view.findViewById(R.id.tvSunset);

        pbUV = view.findViewById(R.id.pbUV);
        pbHum = view.findViewById(R.id.pbHumidity);
        pbFeels = view.findViewById(R.id.pbFeels);
        pbPress = view.findViewById(R.id.pbPressure);
        pbSunset = view.findViewById(R.id.pbSunset);

        imgIcon = view.findViewById(R.id.imgWeatherIcon);
        imgCurrentLoc = view.findViewById(R.id.imgCurrentLocationMarker);
        imgCompassArrow = view.findViewById(R.id.imgCompassArrow);
        viewCityDot = view.findViewById(R.id.viewCityDot);
        rvForecast = view.findViewById(R.id.rvForecast);
        layoutDailyRows = view.findViewById(R.id.layoutDailyRows);
        btn5DayForecast = view.findViewById(R.id.btn5DayForecast);

        rvForecast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        if (cityName != null) tvCity.setText(cityName);

        if (isCurrentLocation) {
            imgCurrentLoc.setVisibility(View.VISIBLE);
            viewCityDot.setVisibility(View.GONE);
        } else {
            imgCurrentLoc.setVisibility(View.GONE);
            viewCityDot.setVisibility(View.VISIBLE);
        }

        btn5DayForecast.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FiveDayForecastActivity.class);
            intent.putExtra("city", cityName);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data to respect potential unit changes
        getWeatherData(cityName);
    }

    private void getWeatherData(String city) {
        if (city == null) return;
        new Thread(() -> {
            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=" + API_KEY);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == 200) {
                    WeatherResponse data = new Gson().fromJson(new InputStreamReader(conn.getInputStream()), WeatherResponse.class);
                    if (isAdded()) getActivity().runOnUiThread(() -> updateWeatherUI(data));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        new Thread(() -> {
            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&units=metric&appid=" + API_KEY);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == 200) {
                    ForecastResponse data = new Gson().fromJson(new InputStreamReader(conn.getInputStream()), ForecastResponse.class);
                    if (isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            String unit = prefs.getString("temp_unit", "C");
                            ForecastAdapter adapter = new ForecastAdapter(data.getList(), unit);
                            rvForecast.setAdapter(adapter);
                            updateMinMaxFromForecast(data);
                            updateDailyForecast(data);
                        });
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void updateDailyForecast(ForecastResponse data) {
        if (data.getList() == null || layoutDailyRows == null) return;
        layoutDailyRows.removeAllViews();
        Map<String, DailyTemp> dailyMap = new LinkedHashMap<>();
        SimpleDateFormat sdfDay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfName = new SimpleDateFormat("EEEE", Locale.getDefault());
        for (ForecastItem item : data.getList()) {
            Date date = new Date(item.getDt() * 1000);
            String dayKey = sdfDay.format(date);
            double temp = item.getMain().getTemp();
            if (!dailyMap.containsKey(dayKey)) {
                DailyTemp dt = new DailyTemp();
                dt.min = temp; dt.max = temp; dt.icon = item.getWeather().get(0).getIcon();
                Calendar cal = Calendar.getInstance(); cal.setTime(date);
                Calendar today = Calendar.getInstance();
                if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) dt.name = "Today";
                else if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) + 1) dt.name = "Tomorrow";
                else dt.name = sdfName.format(date);
                dailyMap.put(dayKey, dt);
            } else {
                DailyTemp dt = dailyMap.get(dayKey);
                if (temp < dt.min) dt.min = temp;
                if (temp > dt.max) dt.max = temp;
            }
        }
        int count = 0;
        String unit = prefs.getString("temp_unit", "C");
        for (DailyTemp dt : dailyMap.values()) {
            if (count >= 3) break;
            View row = getLayoutInflater().inflate(R.layout.item_daily_forecast, layoutDailyRows, false);
            ((TextView)row.findViewById(R.id.tvDayName)).setText(dt.name);
            ((TextView)row.findViewById(R.id.tvDailyMin)).setText(formatTemp(dt.min, unit));
            ((TextView)row.findViewById(R.id.tvDailyMax)).setText(formatTemp(dt.max, unit));
            ImageView icon = row.findViewById(R.id.imgDailyIcon);
            Glide.with(this).load("https://openweathermap.org/img/wn/" + dt.icon + ".png").into(icon);
            layoutDailyRows.addView(row);
            count++;
        }
    }

    private String formatTemp(double tempC, String unit) {
        if (unit.equals("F")) {
            return String.format(Locale.getDefault(), "%.0f°", (tempC * 9/5) + 32);
        }
        return String.format(Locale.getDefault(), "%.0f°", tempC);
    }

    private static class DailyTemp { String name; double min, max; String icon; }

    private void updateWeatherUI(WeatherResponse data) {
        String unit = prefs.getString("temp_unit", "C");
        tvCity.setText(data.getName());
        tvTemp.setText(formatTemp(data.getMain().getTemp(), unit));
        tvDesc.setText(data.getWeather().get(0).getMain());
        
        Glide.with(this).load("https://openweathermap.org/img/wn/" + data.getWeather().get(0).getIcon() + "@2x.png").into(imgIcon);

        int deg = data.getWind().getDeg();
        tvWindDir.setText(getWindDirection(deg));
        tvWindSpeed.setText(String.format(Locale.getDefault(), "%.1f", data.getWind().getSpeed() * 3.6));

        if (imgCompassArrow != null) imgCompassArrow.setRotation(deg);
        
        tvHum.setText(data.getMain().getHumidity() + "%");
        pbHum.setProgress(data.getMain().getHumidity());
        tvPress.setText(String.valueOf(data.getMain().getPressure()));
        int pressProgress = Math.max(0, Math.min(100, (data.getMain().getPressure() - 950)));
        pbPress.setProgress(pressProgress);
        
        tvFeels.setText(formatTemp(data.getMain().getFeelsLike(), unit));
        int feelsProgress = (int) data.getMain().getFeelsLike() + 10;
        pbFeels.setProgress(Math.max(0, Math.min(50, feelsProgress)));

        int uv = getSimulatedUV(data.getWeather().get(0).getMain());
        tvUV.setText(getUVText(uv));
        tvUVIndex.setText(String.valueOf(uv));
        pbUV.setProgress(uv);

        long now = System.currentTimeMillis() / 1000;
        long sunrise = data.getSys().getSunrise();
        long sunset = data.getSys().getSunset();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvSunset.setText(sdf.format(new Date(sunset * 1000)));

        if (pbSunset != null) {
            if (now < sunrise) pbSunset.setProgress(0);
            else if (now > sunset) pbSunset.setProgress(100);
            else pbSunset.setProgress((int) (((float)(now - sunrise) / (sunset - sunrise)) * 100));
        }
    }

    private String getWindDirection(int deg) {
        if (deg >= 337.5 || deg < 22.5) return "North";
        if (deg >= 22.5 && deg < 67.5) return "Northeast";
        if (deg >= 67.5 && deg < 112.5) return "East";
        if (deg >= 112.5 && deg < 157.5) return "Southeast";
        if (deg >= 157.5 && deg < 202.5) return "South";
        if (deg >= 202.5 && deg < 247.5) return "Southwest";
        if (deg >= 247.5 && deg < 292.5) return "West";
        if (deg >= 292.5 && deg < 337.5) return "Northwest";
        return "North";
    }

    private void updateMinMaxFromForecast(ForecastResponse data) {
        if (data.getList() == null || data.getList().isEmpty()) return;
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (int i = 0; i < Math.min(data.getList().size(), 8); i++) {
            double temp = data.getList().get(i).getMain().getTemp();
            if (temp < min) min = temp;
            if (temp > max) max = temp;
        }
        String unit = prefs.getString("temp_unit", "C");
        String text = formatTemp(max, unit) + "/" + formatTemp(min, unit);
        if (isAdded()) getActivity().runOnUiThread(() -> tvMinMax.setText(text));
    }

    private int getSimulatedUV(String weather) {
        if (weather.equalsIgnoreCase("Clear")) return 6;
        if (weather.equalsIgnoreCase("Clouds")) return 3;
        return 1;
    }

    private String getUVText(int uv) {
        if (uv <= 2) return "Low"; if (uv <= 5) return "Moderate";
        if (uv <= 7) return "High"; return "Very High";
    }
}
