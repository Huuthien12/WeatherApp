package com.example.myapplicationooo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ForecastItem {

    @SerializedName("dt")
    private long dt;

    @SerializedName("dt_txt")
    private String dtTxt;

    @SerializedName("main")
    private MainData main;

    @SerializedName("weather")
    private List<WeatherItem> weather;

    public long getDt() {
        return dt;
    }

    public String getDtTxt() {
        return dtTxt;
    }

    public MainData getMain() {
        return main;
    }

    public List<WeatherItem> getWeather() {
        return weather;
    }

    // =========================
    // MAIN DATA
    // =========================

    public static class MainData {

        @SerializedName("temp")
        private double temp;

        @SerializedName("temp_min")
        private double tempMin;

        @SerializedName("temp_max")
        private double tempMax;

        @SerializedName("humidity")
        private int humidity;

        public double getTemp() {
            return temp;
        }

        public double getTempMin() {
            return tempMin;
        }

        public double getTempMax() {
            return tempMax;
        }

        public int getHumidity() {
            return humidity;
        }
    }

    // =========================
    // WEATHER DATA
    // =========================

    public static class WeatherItem {

        @SerializedName("main")
        private String main;

        @SerializedName("description")
        private String description;

        @SerializedName("icon")
        private String icon;

        public String getMain() {
            return main;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }
    }
}