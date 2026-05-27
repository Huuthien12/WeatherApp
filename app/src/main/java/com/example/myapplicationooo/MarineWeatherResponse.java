package com.example.myapplicationooo;

import com.google.gson.annotations.SerializedName;

public class MarineWeatherResponse {
    @SerializedName("current")
    private Current current;

    public Current getCurrent() { return current; }

    public static class Current {
        @SerializedName("wave_height")
        private double waveHeight;
        @SerializedName("wave_direction")
        private int waveDirection;
        @SerializedName("wave_period")
        private double wavePeriod;
        @SerializedName("sea_surface_temperature")
        private double seaTemp;
        @SerializedName("wind_speed_10m")
        private double windSpeed;
        @SerializedName("wind_direction_10m")
        private int windDirection;

        public double getWaveHeight() { return waveHeight; }
        public int getWaveDirection() { return waveDirection; }
        public double getWavePeriod() { return wavePeriod; }
        public double getSeaTemp() { return seaTemp; }
        public double getWindSpeed() { return windSpeed; }
        public int getWindDirection() { return windDirection; }
    }
}
