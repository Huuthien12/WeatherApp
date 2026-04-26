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

    public WeatherPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void setCities(List<String> cities, String currentLocName) {
        this.currentLocName = currentLocName;
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
        return WeatherFragment.newInstance(cityName, isCurrent);
    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }
}
