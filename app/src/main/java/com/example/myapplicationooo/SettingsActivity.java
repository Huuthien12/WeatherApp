package com.example.myapplicationooo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvCurrentTempUnit, tvCurrentLanguage, tvUserStatus;
    private SharedPreferences prefs;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        
        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnLogout = findViewById(R.id.btnLogout);
        RelativeLayout layoutTempUnit = findViewById(R.id.layoutTempUnit);
        LinearLayout layoutLanguage = findViewById(R.id.layoutLanguage);
        LinearLayout layoutNotificationSettings = findViewById(R.id.layoutNotificationSettings);
        
        // CÁC MỤC MỚI THÊM
        View layoutTripPlanner = findViewById(R.id.layoutTripPlanner);
        View layoutChatAI = findViewById(R.id.layoutChatAI);
        
        tvCurrentTempUnit = findViewById(R.id.tvCurrentTempUnit);
        tvCurrentLanguage = findViewById(R.id.tvCurrentLanguage);
        tvUserStatus = findViewById(R.id.tvTitle);

        // Hiển thị trạng thái User
        if (AuthManager.getInstance().isGuest()) {
            tvUserStatus.setText(R.string.welcome_guest);
            btnLogout.setText(R.string.login_now);
        } else {
            String email = AuthManager.getInstance().getCurrentUser().getEmail();
            tvUserStatus.setText(email != null ? email : getString(R.string.settings));
            btnLogout.setText(R.string.logout);
        }

        String currentUnit = prefs.getString("temp_unit", "C");
        tvCurrentTempUnit.setText("°" + currentUnit);

        String currentLang = LocaleHelper.getLanguage(this);
        tvCurrentLanguage.setText(currentLang.equals("vi") ? R.string.vietnamese : R.string.english);

        btnBack.setOnClickListener(v -> finish());

        layoutTempUnit.setOnClickListener(v -> {
            String newUnit = tvCurrentTempUnit.getText().toString().contains("C") ? "F" : "C";
            prefs.edit().putString("temp_unit", newUnit).apply();
            tvCurrentTempUnit.setText("°" + newUnit);
        });

        layoutLanguage.setOnClickListener(v -> showLanguageDialog());

        // Mở Trip Planner (Yêu cầu login)
        if (layoutTripPlanner != null) {
            layoutTripPlanner.setOnClickListener(v -> {
                LoginRequiredHelper.checkAndProceed(this, TripPlannerActivity.class);
            });
        }

        // Mở AI Chat (Yêu cầu login)
        if (layoutChatAI != null) {
            layoutChatAI.setOnClickListener(v -> {
                LoginRequiredHelper.checkAndProceed(this, ChatActivity.class);
            });
        }

        if (layoutNotificationSettings != null) {
            layoutNotificationSettings.setOnClickListener(v -> {
                LoginRequiredHelper.checkAndProceed(this, NotificationSettingsActivity.class);
            });
        }

        btnLogout.setOnClickListener(v -> {
            if (AuthManager.getInstance().isGuest()) {
                startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                finish();
            } else {
                showLogoutConfirmation();
            }
        });
    }

    private void showLanguageDialog() {
        String[] languages = {getString(R.string.english), getString(R.string.vietnamese)};
        int checkedItem = LocaleHelper.getLanguage(this).equals("vi") ? 1 : 0;

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.language)
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    String selectedLang = (which == 1) ? "vi" : "en";
                    if (!selectedLang.equals(LocaleHelper.getLanguage(this))) {
                        LocaleHelper.setLocale(this, selectedLang);
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.confirm_logout)
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    AuthManager.getInstance().logout();
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
