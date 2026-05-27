package com.example.myapplicationooo;

/**
 * Tiện ích hỗ trợ thay đổi icon thời tiết mặc định của API 
 * sang bộ icon PNG chất lượng cao trong thư mục drawable.
 */
public class WeatherIconHelper {
    public static int getWeatherIcon(String iconCode) {
        if (iconCode == null) return R.drawable.iconsun;
        
        switch (iconCode) {
            case "01d": case "01n": return R.drawable.ic_sun;
            case "02d": case "02n":
            case "03d": case "03n":
            case "04d": case "04n": return R.drawable.ic_cloud;
            case "09d": case "09n":
            case "10d": case "10n":
            case "11d": case "11n": return R.drawable.ic_rain;
            case "13d": case "13n": return R.drawable.ic_snow;
            case "50d": case "50n": return R.drawable.ic_fog;
            default: return R.drawable.iconsun;
        }
    }
}
