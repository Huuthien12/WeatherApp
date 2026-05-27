package com.example.myapplicationooo;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE cityName = :city LIMIT 1")
    WeatherCacheEntity getCachedWeather(String city);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWeather(WeatherCacheEntity weather);
}