package com.example.myapplicationooo;
import java.util.List;

public class ForecastItem {
    private long dt; // Thời gian dạng timestamp
    private MainData main;
    private List<WeatherItem> weather;

    public long getDt() { return dt; }
    public MainData getMain() { return main; }
    public List<WeatherItem> getWeather() { return weather; }

    public static class MainData {
        private double temp;
        public double getTemp() { return temp; }
    }

    public static class WeatherItem {
        private String icon;
        public String getIcon() { return icon; }
    }
}