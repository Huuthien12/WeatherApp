package com.example.myapplicationooo;

import com.google.gson.annotations.SerializedName;

public class AgricultureWeatherResponse {
    @SerializedName("current")
    private Current current;

    public Current getCurrent() { return current; }

    public static class Current {
        @SerializedName("temperature_2m") private double temperature;
        @SerializedName("relative_humidity_2m") private int humidity;
        @SerializedName("precipitation") private double precipitation;
        @SerializedName("rain") private double rain;
        @SerializedName("wind_speed_10m") private double windSpeed;

        public double getTemperature() { return temperature; }
        public int getHumidity() { return humidity; }
        public double getPrecipitation() { return precipitation; }
        public double getRain() { return rain; }
        public double getWindSpeed() { return windSpeed; }
    }
}
