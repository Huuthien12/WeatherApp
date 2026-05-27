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
import androidx.appcompat.app.AlertDialog;
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
import java.util.TimeZone;

public class WeatherFragment extends Fragment {

    private String cityName;
    private boolean isCurrentLocation;
    private double lat = 0.0, lon = 0.0;
    private final String API_KEY = "22b3362b92ebbde69c2e8145c14d2da2";

    private TextView tvCity, tvTemp, tvDesc, tvMinMax, tvWindSpeed, tvWindDir, tvHum, tvPress, tvUV, tvUVIndex, tvFeels, tvSunset;
    private ProgressBar pbUV, pbHum, pbFeels, pbPress, pbSunset;
    private ImageView imgIcon, imgCurrentLoc, imgCompassArrow;
    private View viewCityDot, layoutUV, layoutHumidity;
    private RecyclerView rvForecast;
    private LinearLayout layoutDailyRows;
    private Button btn5DayForecast;
    private SharedPreferences prefs;

    public static WeatherFragment newInstance(String cityName, boolean isCurrentLocation) {
        return newInstance(cityName, isCurrentLocation, 0, 0);
    }

    public static WeatherFragment newInstance(String cityName, boolean isCurrentLocation, double lat, double lon) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putString("city", cityName);
        args.putBoolean("is_current", isCurrentLocation);
        args.putDouble("lat", lat);
        args.putDouble("lon", lon);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cityName = getArguments().getString("city");
            isCurrentLocation = getArguments().getBoolean("is_current");
            lat = getArguments().getDouble("lat");
            lon = getArguments().getDouble("lon");
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

        layoutUV = view.findViewById(R.id.layoutUV);
        layoutHumidity = view.findViewById(R.id.layoutHumidity);

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
            intent.putExtra("lat", lat);
            intent.putExtra("lon", lon);
            intent.putExtra("is_current", isCurrentLocation);
            startActivity(intent);
        });

        layoutHumidity.setOnClickListener(v -> showHumidityAdvice());
        layoutUV.setOnClickListener(v -> showUVAdvice());

        return view;
    }

    private void showHumidityAdvice() {
        String humStr = tvHum.getText().toString().replace("%", "");
        try {
            int humidity = Integer.parseInt(humStr);
            String title, advice;
            if (humidity < 30) {
                title = getString(R.string.aqi_very_poor);
                advice = "Dễ gây khô da, kích ứng niêm mạc mũi. Nên dùng máy bù ẩm.";
            } else if (humidity <= 60) {
                title = getString(R.string.aqi_good);
                advice = "Mức độ an toàn nhất cho sức khỏe và giảm thiểu vi khuẩn.";
            } else if (humidity <= 80) {
                title = getString(R.string.aqi_moderate);
                advice = "Bắt đầu cảm thấy oi bức. Người bị xoang hoặc dị ứng cần lưu ý.";
            } else {
                title = getString(R.string.aqi_poor);
                advice = "Nguy cơ nấm mốc phát triển mạnh. Người bệnh hen suyễn, xương khớp dễ bị đau nhức.";
            }

            new AlertDialog.Builder(getContext())
                    .setTitle("Cảnh báo sức khỏe & Lời khuyên")
                    .setMessage("Độ ẩm: " + humidity + "% (" + title + ")\n\n" + advice)
                    .setPositiveButton("Đã hiểu", null)
                    .show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showUVAdvice() {
        String uvStr = tvUVIndex.getText().toString();
        try {
            int uv = Integer.parseInt(uvStr);
            String level, advice;
            if (uv <= 2) {
                level = getString(R.string.aqi_good);
                advice = "An toàn. Bạn có thể thoải mái hoạt động ngoài trời mà không cần bảo vệ nhiều.";
            } else if (uv <= 5) {
                level = getString(R.string.aqi_moderate);
                advice = "Có nguy cơ tổn thương da nếu tiếp xúc lâu. Nên ở trong bóng mát vào buổi trưa.";
            } else if (uv <= 7) {
                level = getString(R.string.aqi_poor);
                advice = getString(R.string.warn_uv);
            } else if (uv <= 10) {
                level = getString(R.string.aqi_very_poor);
                advice = "Rất nguy hiểm. Da có thể bị bỏng chỉ sau 15-20 phút. Tránh ra ngoài từ 11h sáng đến 4h chiều.";
            } else {
                level = "Nguy hại";
                advice = "Cực kỳ nguy hiểm. Nguy cơ ung thư da và đục thủy tinh thể rất cao. Bắt buộc mặc đồ bảo hộ.";
            }

            new AlertDialog.Builder(getContext())
                    .setTitle("Cảnh báo sức khỏe & Hành động")
                    .setMessage("Chỉ số UV: " + uv + " (" + level + ")\n\n" + advice)
                    .setPositiveButton("Đã hiểu", null)
                    .show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void onResume() {
        super.onResume();
        getWeatherData();
    }

    private void getWeatherData() {
        new Thread(() -> {
            try {
                String urlString;
                if (isCurrentLocation && lat != 0 && lon != 0) {
                    urlString = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + API_KEY;
                } else {
                    urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&units=metric&appid=" + API_KEY;
                }
                
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == 200) {
                    WeatherResponse data = new Gson().fromJson(new InputStreamReader(conn.getInputStream()), WeatherResponse.class);
                    if (isAdded()) getActivity().runOnUiThread(() -> updateWeatherUI(data));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        new Thread(() -> {
            try {
                String urlString;
                if (isCurrentLocation && lat != 0 && lon != 0) {
                    urlString = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + API_KEY;
                } else {
                    urlString = "https://api.openweathermap.org/data/2.5/forecast?q=" + cityName + "&units=metric&appid=" + API_KEY;
                }

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == 200) {
                    ForecastResponse data = new Gson().fromJson(new InputStreamReader(conn.getInputStream()), ForecastResponse.class);
                    if (isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            String unit = prefs.getString("temp_unit", "C");
                            ForecastAdapter adapter = new ForecastAdapter(data.getList(), unit, data.getCity().getTimezone());
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
        
        int timezoneOffset = data.getCity().getTimezone();
        SimpleDateFormat sdfDay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdfDay.setTimeZone(TimeZone.getTimeZone("GMT" + (timezoneOffset >= 0 ? "+" : "") + (timezoneOffset / 3600)));
        
        SimpleDateFormat sdfName = new SimpleDateFormat("EEEE", Locale.getDefault());
        sdfName.setTimeZone(TimeZone.getTimeZone("GMT" + (timezoneOffset >= 0 ? "+" : "") + (timezoneOffset / 3600)));

        for (ForecastItem item : data.getList()) {
            Date date = new Date(item.getDt() * 1000);
            String dayKey = sdfDay.format(date);
            double temp = item.getMain().getTemp();
            if (!dailyMap.containsKey(dayKey)) {
                DailyTemp dt = new DailyTemp();
                dt.min = temp; dt.max = temp; dt.icon = item.getWeather().get(0).getIcon();
                
                Calendar cal = Calendar.getInstance(sdfName.getTimeZone());
                cal.setTime(date);
                Calendar today = Calendar.getInstance(sdfName.getTimeZone());
                
                if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) && cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) dt.name = "Today";
                else if (cal.get(Calendar.DAY_OF_YEAR) == (today.get(Calendar.DAY_OF_YEAR) + 1) && cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) dt.name = "Tomorrow";
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
            
            // SỬ DỤNG ICON MỚI CHO DỄ NHÌN
            int iconRes = WeatherIconHelper.getWeatherIcon(dt.icon);
            Glide.with(this).load(iconRes).into(icon);
            
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
        if (isCurrentLocation) {
            tvCity.setText(data.getName());
        }
        tvTemp.setText(formatTemp(data.getMain().getTemp(), unit));
        tvDesc.setText(data.getWeather().get(0).getMain());
        
        // SỬ DỤNG ICON MỚI CHO DỄ NHÌN
        int iconRes = WeatherIconHelper.getWeatherIcon(data.getWeather().get(0).getIcon());
        Glide.with(this).load(iconRes).into(imgIcon);

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

        int timezoneOffset = data.getTimezone();
        long now = (System.currentTimeMillis() / 1000);
        long sunset = data.getSys().getSunset();
        
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + (timezoneOffset >= 0 ? "+" : "") + (timezoneOffset / 3600)));
        tvSunset.setText(sdf.format(new Date(sunset * 1000)));

        if (pbSunset != null) {
            long sunrise = data.getSys().getSunrise();
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
