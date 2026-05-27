package com.example.myapplicationooo;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastResponse {
    @SerializedName("list")
    private List<ForecastItem> list;
    @SerializedName("city")
    private City city;

    public List<ForecastItem> getList() { return list; }
    public City getCity() { return city; }

    public static class City {
        @SerializedName("name")
        private String name;
        @SerializedName("timezone")
        private int timezone;
        @SerializedName("coord")
        private Coord coord;

        public String getName() { return name; }
        public int getTimezone() { return timezone; }
        public Coord getCoord() { return coord; }
    }

    public static class Coord {
        @SerializedName("lat")
        private double lat;
        @SerializedName("lon")
        private double lon;
        public double getLat() { return lat; }
        public double getLon() { return lon; }
    }
}