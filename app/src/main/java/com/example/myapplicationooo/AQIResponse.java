package com.example.myapplicationooo;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AQIResponse {
    @SerializedName("list")
    private List<AQIItem> list;

    public List<AQIItem> getList() { return list; }

    public static class AQIItem {
        @SerializedName("main")
        private Main main;
        @SerializedName("components")
        private Components components;

        public Main getMain() { return main; }
        public Components getComponents() { return components; }
    }

    public static class Main {
        @SerializedName("aqi")
        private int aqi; // 1: Good, 2: Fair, 3: Moderate, 4: Poor, 5: Very Poor
        public int getAqi() { return aqi; }
    }

    public static class Components {
        @SerializedName("co") private double co;
        @SerializedName("no2") private double no2;
        @SerializedName("o3") private double o3;
        @SerializedName("so2") private double so2;
        @SerializedName("pm2_5") private double pm25;
        @SerializedName("pm10") private double pm10;

        public double getCo() { return co; }
        public double getNo2() { return no2; }
        public double getO3() { return o3; }
        public double getSo2() { return so2; }
        public double getPm25() { return pm25; }
        public double getPm10() { return pm10; }
    }
}
