package com.example.myapplicationooo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView imgBackground;
    private ViewPager2 viewPager;
    private WeatherPagerAdapter pagerAdapter;
    private ActivityResultLauncher<Intent> searchLauncher;
    private FusedLocationProviderClient fusedLocationClient;
    private String currentCityName = null;
    private final String API_KEY = "22b3362b92ebbde69c2e8145c14d2da2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        progressBar = findViewById(R.id.progressBar);
        imgBackground = findViewById(R.id.imgBackground);
        viewPager = findViewById(R.id.viewPagerWeather);
        ImageButton btnAddCity = findViewById(R.id.btnAddCity);
        ImageButton btnSettings = findViewById(R.id.btnSettings);

        pagerAdapter = new WeatherPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Lắng nghe sự kiện đổi trang để cập nhật ảnh nền tương ứng
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateBackgroundForCity(pagerAdapter.getCityAt(position));
            }
        });

        getCurrentLocationAndLoadCities();

        searchLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String selectedCity = result.getData().getStringExtra("selected_city");
                        if (selectedCity != null) updateAndScrollToCity(selectedCity);
                        else getCurrentLocationAndLoadCities();
                    } else getCurrentLocationAndLoadCities();
                }
        );

        btnAddCity.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            searchLauncher.launch(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void updateBackgroundForCity(String cityName) {
        if (cityName == null) return;
        new Thread(() -> {
            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&units=metric&appid=" + API_KEY);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == 200) {
                    WeatherResponse data = new Gson().fromJson(new InputStreamReader(conn.getInputStream()), WeatherResponse.class);
                    String weatherMain = data.getWeather().get(0).getMain().toLowerCase();
                    
                    runOnUiThread(() -> {
                        int bgResId = R.drawable.sun; // Mặc định
                        if (weatherMain.contains("cloud")) bgResId = R.drawable.cloud;
                        else if (weatherMain.contains("rain") || weatherMain.contains("drizzle") || weatherMain.contains("thunderstorm")) bgResId = R.drawable.rain;
                        else if (weatherMain.contains("fog") || weatherMain.contains("mist") || weatherMain.contains("haze")) bgResId = R.drawable.fog;
                        else if (weatherMain.contains("clear")) bgResId = R.drawable.sun;
                        
                        Glide.with(this).load(bgResId).centerCrop().into(imgBackground);
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void getCurrentLocationAndLoadCities() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            loadFavoriteCities();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        currentCityName = address.getLocality();
                        if (currentCityName == null) currentCityName = address.getSubAdminArea();
                        if (currentCityName == null) currentCityName = address.getAdminArea();
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
            loadFavoriteCities();
        });
    }

    private void updateAndScrollToCity(String cityName) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;
        FirebaseFirestore.getInstance().collection("users").document(userId).get().addOnSuccessListener(ds -> {
            if (ds.exists()) {
                List<String> cities = (List<String>) ds.get("favoriteCities");
                List<String> displayCities = new ArrayList<>();
                if (currentCityName != null) displayCities.add(currentCityName);
                if (cities != null) for (String c : cities) if (!displayCities.contains(c)) displayCities.add(c);
                if (!displayCities.contains(cityName)) {
                    int pos = (currentCityName != null) ? 1 : 0;
                    displayCities.add(pos, cityName);
                }
                pagerAdapter.setCities(displayCities, currentCityName);
                int index = displayCities.indexOf(cityName);
                if (index != -1) viewPager.setCurrentItem(index, false);
            }
        });
    }

    private void loadFavoriteCities() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;
        FirebaseFirestore.getInstance().collection("users").document(userId).get().addOnSuccessListener(ds -> {
            List<String> displayCities = new ArrayList<>();
            if (currentCityName != null) displayCities.add(currentCityName);
            if (ds.exists()) {
                List<String> cities = (List<String>) ds.get("favoriteCities");
                if (cities != null) for (String c : cities) if (!displayCities.contains(c)) displayCities.add(c);
            }
            if (displayCities.isEmpty()) displayCities.add("Hanoi");
            pagerAdapter.setCities(displayCities, currentCityName);
            viewPager.setCurrentItem(0, false);
        });
    }
}
