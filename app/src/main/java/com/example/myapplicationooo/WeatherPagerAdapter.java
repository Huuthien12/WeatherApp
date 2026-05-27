package com.example.myapplicationooo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class WeatherPagerAdapter extends FragmentStateAdapter {
    private final List<String> cityList = new ArrayList<>();
    private String currentLocName = null;
    private double currentLat = 0, currentLon = 0;

    public WeatherPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void setCities(List<String> cities, String currentLocName, double lat, double lon) {
        this.currentLocName = currentLocName;
        this.currentLat = lat;
        this.currentLon = lon;
        cityList.clear();
        cityList.addAll(cities);
        notifyDataSetChanged();
    }

    public String getCityAt(int position) {
        if (position >= 0 && position < cityList.size()) {
            return cityList.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String cityName = cityList.get(position);
        boolean isCurrent = cityName.equals(currentLocName);
        if (isCurrent) {
            return WeatherFragment.newInstance(cityName, true, currentLat, currentLon);
        }
        return WeatherFragment.newInstance(cityName, false);
    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }

    @Override
    public long getItemId(int position) {
        return cityList.get(position).hashCode();
    }

    @Override
    public boolean containsItem(long itemId) {
        for (String city : cityList) {
            if (city.hashCode() == itemId) return true;
        }
        return false;
    }
}
