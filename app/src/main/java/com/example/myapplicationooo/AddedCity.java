package com.example.myapplicationooo;

public class AddedCity {
    private String name;
    private double temp;
    private String description;
    private double minTemp;
    private double maxTemp;
    private boolean isCurrentLocation;
    private boolean isSelected;

    public AddedCity(String name) {
        this.name = name;
        this.isCurrentLocation = false;
        this.isSelected = false;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTemp() { return temp; }
    public void setTemp(double temp) { this.temp = temp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getMinTemp() { return minTemp; }
    public void setMinTemp(double minTemp) { this.minTemp = minTemp; }

    public double getMaxTemp() { return maxTemp; }
    public void setMaxTemp(double maxTemp) { this.maxTemp = maxTemp; }

    public boolean isCurrentLocation() { return isCurrentLocation; }
    public void setCurrentLocation(boolean currentLocation) { isCurrentLocation = currentLocation; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
