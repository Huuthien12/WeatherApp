package com.example.myapplicationooo;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

public class NotificationSettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private MaterialSwitch switchMainNotify, switchRain, switchUV, switchWind;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        
        switchMainNotify = findViewById(R.id.switchMainNotify);
        switchRain = findViewById(R.id.switchRain);
        switchUV = findViewById(R.id.switchUV);
        switchWind = findViewById(R.id.switchWind);

        toolbar.setNavigationOnClickListener(v -> finish());

        // Load saved states
        switchMainNotify.setChecked(prefs.getBoolean("smart_notifications", true));
        switchRain.setChecked(prefs.getBoolean("notify_rain", true));
        switchUV.setChecked(prefs.getBoolean("notify_uv", true));
        switchWind.setChecked(prefs.getBoolean("notify_wind", true));

        switchMainNotify.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkNotificationPermission();
            } else {
                prefs.edit().putBoolean("smart_notifications", false).apply();
            }
        });

        switchRain.setOnCheckedChangeListener((v, isChecked) -> prefs.edit().putBoolean("notify_rain", isChecked).apply());
        switchUV.setOnCheckedChangeListener((v, isChecked) -> prefs.edit().putBoolean("notify_uv", isChecked).apply());
        switchWind.setOnCheckedChangeListener((v, isChecked) -> prefs.edit().putBoolean("notify_wind", isChecked).apply());
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                prefs.edit().putBoolean("smart_notifications", true).apply();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            prefs.edit().putBoolean("smart_notifications", true).apply();
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    prefs.edit().putBoolean("smart_notifications", true).apply();
                } else {
                    switchMainNotify.setChecked(false);
                    Toast.makeText(this, R.string.notification_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });
}
