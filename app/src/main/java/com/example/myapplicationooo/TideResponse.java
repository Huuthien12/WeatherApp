package com.example.myapplicationooo;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TideResponse {
    @SerializedName("hourly")
    private Hourly hourly;

    public Hourly getHourly() { return hourly; }

    public static class Hourly {
        @SerializedName("time")
        private List<String> time;
        @SerializedName("height")
        private List<Double> height;

        public List<String> getTime() { return time; }
        public List<Double> getHeight() { return height; }
    }
}
