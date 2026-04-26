package com.example.myapplicationooo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvCurrentTempUnit;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        
        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnLogout = findViewById(R.id.btnLogout);
        RelativeLayout layoutTempUnit = findViewById(R.id.layoutTempUnit);
        tvCurrentTempUnit = findViewById(R.id.tvCurrentTempUnit);

        // Hiển thị đơn vị hiện tại
        String currentUnit = prefs.getString("temp_unit", "C");
        tvCurrentTempUnit.setText("°" + currentUnit);

        btnBack.setOnClickListener(v -> finish());

        layoutTempUnit.setOnClickListener(v -> {
            String newUnit = tvCurrentTempUnit.getText().toString().contains("C") ? "F" : "C";
            prefs.edit().putString("temp_unit", newUnit).apply();
            tvCurrentTempUnit.setText("°" + newUnit);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
