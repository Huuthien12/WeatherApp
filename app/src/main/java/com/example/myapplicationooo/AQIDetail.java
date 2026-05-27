package com.example.myapplicationooo;

public class AQIDetail {
    private String name;
    private String value;
    private String unit;

    public AQIDetail(String name, String value, String unit) {
        this.name = name;
        this.value = value;
        this.unit = unit;
    }

    public String getName() { return name; }
    public String getValue() { return value; }
    public String getUnit() { return unit; }
}
