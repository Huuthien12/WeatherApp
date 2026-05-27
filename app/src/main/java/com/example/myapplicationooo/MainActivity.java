package com.example.myapplicationooo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ImageView imgBackground;
    private ViewPager2 viewPager;
    private WeatherPagerAdapter pagerAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActivityResultLauncher<Intent> searchLauncher;
    private FusedLocationProviderClient fusedLocationClient;
    private String currentCityName = null;
    private double currentLat = 0, currentLon = 0;
    private final String API_KEY = "22b3362b92ebbde69c2e8145c14d2da2";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        imgBackground = findViewById(R.id.imgBackground);
        viewPager = findViewById(R.id.viewPagerWeather);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        
        ImageButton btnAddCity = findViewById(R.id.btnAddCity);
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        ImageButton btnRadar = findViewById(R.id.btnRadar);
        ImageButton btnChatAI = findViewById(R.id.btnChatAI);
        ImageButton btnAQI = findViewById(R.id.btnAQI);

        pagerAdapter = new WeatherPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Mặc định nạp ảnh nền mặt trời để tránh màn hình đen ban đầu
        imgBackground.setImageResource(R.drawable.sun);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateBackgroundForCity(pagerAdapter.getCityAt(position));
            }
        });

        swipeRefreshLayout.setOnRefreshListener(this::getCurrentLocationAndLoadCities);

        checkPermissions();
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

        // AI Assistant - Login Required
        if (btnChatAI != null) {
            btnChatAI.setOnClickListener(v -> {
                LoginRequiredHelper.checkAndProceed(this, () -> {
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    String cityName = pagerAdapter.getCityAt(viewPager.getCurrentItem());
                    intent.putExtra("weather_context", "City: " + cityName);
                    startActivity(intent);
                });
            });
        }

        // Radar - Free for all
        if (btnRadar != null) {
            btnRadar.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RadarActivity.class);
                startActivity(intent);
            });
        }

        // AQI - Free for all
        if (btnAQI != null) {
            btnAQI.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AQIActivity.class);
                intent.putExtra("lat", currentLat);
                intent.putExtra("lon", currentLon);
                startActivity(intent);
            });
        }

        scheduleWeatherAlerts();
    }

    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), 100);
        }
    }

    private void scheduleWeatherAlerts() {
        if (AuthManager.getInstance().isGuest()) return;
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest weatherWorkRequest = new PeriodicWorkRequest.Builder(WeatherWorker.class, 1, TimeUnit.HOURS).setConstraints(constraints).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("WeatherAlertWork", ExistingPeriodicWorkPolicy.KEEP, weatherWorkRequest);
    }

    private void updateBackgroundForCity(String cityName) {
        if (cityName == null) return;
        new Thread(() -> {
            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&units=metric&appid=" + API_KEY);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == 200) {
                    WeatherResponse data = new Gson().fromJson(new InputStreamReader(conn.getInputStream()), WeatherResponse.class);
                    String weatherMain = "";
                    if (data != null && data.getWeather() != null && !data.getWeather().isEmpty()) {
                        weatherMain = data.getWeather().get(0).getMain().toLowerCase();
                    }
                    String finalWeatherMain = weatherMain;
                    runOnUiThread(() -> {
                        int bgResId = R.drawable.sun; 
                        if (finalWeatherMain.contains("snow")) {
                            bgResId = R.drawable.snow;
                        } else if (finalWeatherMain.contains("cloud")) {
                            bgResId = R.drawable.cloud;
                        } else if (finalWeatherMain.contains("rain") || finalWeatherMain.contains("drizzle") || finalWeatherMain.contains("thunderstorm")) {
                            bgResId = R.drawable.rain;
                        } else if (finalWeatherMain.contains("fog") || finalWeatherMain.contains("mist") || finalWeatherMain.contains("haze")) {
                            bgResId = R.drawable.fog;
                        } else if (finalWeatherMain.contains("clear")) {
                            bgResId = R.drawable.sun;
                        }
                        
                        Glide.with(MainActivity.this).load(bgResId).centerCrop().into(imgBackground);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating background: " + e.getMessage());
            }
        }).start();
    }

    private void getCurrentLocationAndLoadCities() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            loadFavoriteCities();
            return;
        }

        swipeRefreshLayout.setRefreshing(true);

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(true)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdates(1)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    android.location.Location location = locationResult.getLastLocation();
                    currentLat = location.getLatitude();
                    currentLon = location.getLongitude();
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            currentCityName = address.getLocality();
                            if (currentCityName == null) currentCityName = address.getSubAdminArea();
                            if (currentCityName == null) currentCityName = address.getAdminArea();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Geocoder error: " + e.getMessage());
                    }
                }
                loadFavoriteCities();
            }
        }, Looper.getMainLooper());
    }

    private void updateAndScrollToCity(String cityName) {
        if (AuthManager.getInstance().isGuest()) {
            Toast.makeText(this, "Please login to save cities", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;
        FirebaseFirestore.getInstance().collection("users").document(userId).get().addOnSuccessListener(ds -> {
            if (ds.exists()) {
                List<String> cities = getFavoriteCities(ds.get("favoriteCities"));
                List<String> displayCities = new ArrayList<>();
                if (currentCityName != null) displayCities.add(currentCityName);
                for (String c : cities) if (!displayCities.contains(c)) displayCities.add(c);
                if (!displayCities.contains(cityName)) {
                    int pos = (currentCityName != null) ? 1 : 0;
                    displayCities.add(pos, cityName);
                }
                pagerAdapter.setCities(displayCities, currentCityName, currentLat, currentLon);
                int index = displayCities.indexOf(cityName);
                if (index != -1) {
                    viewPager.setCurrentItem(index, false);
                    updateBackgroundForCity(displayCities.get(index));
                }
            }
        });
    }

    private void loadFavoriteCities() {
        if (AuthManager.getInstance().isGuest()) {
            List<String> displayCities = new ArrayList<>();
            if (currentCityName != null) displayCities.add(currentCityName);
            else displayCities.add("Hanoi");
            
            pagerAdapter.setCities(displayCities, currentCityName, currentLat, currentLon);
            viewPager.setCurrentItem(0, false);
            updateBackgroundForCity(displayCities.get(0)); // Fix initial black background
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;
        FirebaseFirestore.getInstance().collection("users").document(userId).get().addOnSuccessListener(ds -> {
            List<String> displayCities = new ArrayList<>();
            if (currentCityName != null) displayCities.add(currentCityName);
            if (ds.exists()) {
                List<String> cities = getFavoriteCities(ds.get("favoriteCities"));
                for (String c : cities) if (!displayCities.contains(c)) displayCities.add(c);
            }
            if (displayCities.isEmpty()) displayCities.add("Hanoi");
            pagerAdapter.setCities(displayCities, currentCityName, currentLat, currentLon);
            viewPager.setCurrentItem(0, false);
            updateBackgroundForCity(displayCities.get(0)); // Fix initial black background
            swipeRefreshLayout.setRefreshing(false);
        }).addOnFailureListener(e -> {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
        });
    }

    @SuppressWarnings("unchecked")
    private List<String> getFavoriteCities(Object data) {
        if (data instanceof List) {
            return (List<String>) data;
        }
        return new ArrayList<>();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocationAndLoadCities();
                    break;
                }
            }
        }
    }
}
