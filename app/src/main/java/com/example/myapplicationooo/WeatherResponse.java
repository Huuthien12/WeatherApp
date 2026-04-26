package com.example.myapplicationooo;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
    @SerializedName("main")
    private Main main;

    @SerializedName("weather")
    private List<Weather> weather;

    @SerializedName("wind")
    private Wind wind;

    @SerializedName("sys")
    private Sys sys;

    @SerializedName("visibility")
    private int visibility;

    @SerializedName("name")
    private String name;

    public Main getMain() { return main; }
    public List<Weather> getWeather() { return weather; }
    public Wind getWind() { return wind; }
    public Sys getSys() { return sys; }
    public int getVisibility() { return visibility; }
    public String getName() { return name; }

    public static class Main {
        @SerializedName("temp")
        private double temp;
        @SerializedName("feels_like")
        private double feels_like;
        @SerializedName("temp_min")
        private double temp_min;
        @SerializedName("temp_max")
        private double temp_max;
        @SerializedName("pressure")
        private int pressure;
        @SerializedName("humidity")
        private int humidity;

        public double getTemp() { return temp; }
        public double getFeelsLike() { return feels_like; }
        public double getTempMin() { return temp_min; }
        public double getTempMax() { return temp_max; }
        public int getPressure() { return pressure; }
        public int getHumidity() { return humidity; }
    }

    public static class Weather {
        @SerializedName("description")
        private String description;
        @SerializedName("icon")
        private String icon;
        @SerializedName("main")
        private String main;

        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        public String getMain() { return main; }
    }

    public static class Wind {
        @SerializedName("speed")
        private double speed;
        @SerializedName("deg")
        private int deg;

        public double getSpeed() { return speed; }
        public int getDeg() { return deg; }
    }

    public static class Sys {
        @SerializedName("sunrise")
        private long sunrise;
        @SerializedName("sunset")
        private long sunset;

        public long getSunrise() { return sunrise; }
        public long getSunset() { return sunset; }
    }
}
