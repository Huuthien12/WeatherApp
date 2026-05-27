package com.example.myapplicationooo;

import java.util.List;

public class NotificationRule {
    
    public static class NotificationData {
        public String title;
        public String message;

        public NotificationData(String title, String message) {
            this.title = title;
            this.message = message;
        }
    }

    public static NotificationData checkRain(ForecastResponse forecast, String cityName) {
        if (forecast == null || forecast.getList() == null) return null;
        
        long currentTime = System.currentTimeMillis() / 1000;
        long targetTime = currentTime + (3 * 3600); // Next 3 hours

        for (ForecastItem item : forecast.getList()) {
            if (item.getDt() >= currentTime && item.getDt() <= targetTime + 1800) {
                if (item.getWeather() != null && !item.getWeather().isEmpty()) {
                    String main = item.getWeather().get(0).getMain().toLowerCase();
                    String desc = item.getWeather().get(0).getDescription();
                    if (main.contains("rain") || main.contains("thunder") || main.contains("snow")) {
                        return new NotificationData("Weather Warning", 
                            "Rain expected in the next 3 hours at " + cityName + ": " + desc);
                    }
                }
            }
        }
        return null;
    }

    public static NotificationData checkUV(WeatherResponse current) {
        // Simulated UV logic based on condition since OpenWeather free might not have it in basic call
        // In a real app, you'd use the UV index field from OneCall API or similar
        String condition = current.getWeather().get(0).getMain().toLowerCase();
        if (condition.contains("clear")) {
            return new NotificationData("UV Warning", 
                "UV Index is high today. Wear sunscreen and protect your skin!");
        }
        return null;
    }

    public static NotificationData checkWind(WeatherResponse current) {
        if (current.getWind() != null && current.getWind().getSpeed() * 3.6 > 30) {
            return new NotificationData("Wind Alert", 
                "Strong winds detected (" + String.format("%.1f", current.getWind().getSpeed() * 3.6) + " km/h). Be careful!");
        }
        return null;
    }

    public static NotificationData checkGoodWeather(WeatherResponse current) {
        double temp = current.getMain().getTemp();
        String condition = current.getWeather().get(0).getMain().toLowerCase();
        if (temp >= 20 && temp <= 28 && condition.contains("clear")) {
            return new NotificationData("Good Weather", 
                "The weather is beautiful! Perfect for outdoor activities.");
        }
        return null;
    }
}
