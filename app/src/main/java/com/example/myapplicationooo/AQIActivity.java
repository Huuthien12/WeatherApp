package com.example.myapplicationooo;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class AQIActivity extends AppCompatActivity {

    private AQIViewModel viewModel;
    private TextView tvValue, tvStatus, tvMask, tvActivity;
    private LinearLayout layoutBg;
    private AQIAdapter adapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aqi);

        viewModel = new ViewModelProvider(this).get(AQIViewModel.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        tvValue = findViewById(R.id.tvAQIValue);
        tvStatus = findViewById(R.id.tvAQIStatus);
        tvMask = findViewById(R.id.tvMaskAdvice);
        tvActivity = findViewById(R.id.tvActivityAdvice);
        layoutBg = findViewById(R.id.layoutAQIBg);
        RecyclerView rvPollutants = findViewById(R.id.rvPollutants);
        View pbLoading = findViewById(R.id.pbLoading);

        toolbar.setNavigationOnClickListener(v -> finish());

        rvPollutants.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new AQIAdapter();
        rvPollutants.setAdapter(adapter);

        double lat = getIntent().getDoubleExtra("lat", 0);
        double lon = getIntent().getDoubleExtra("lon", 0);

        viewModel.getAqiData().observe(this, response -> {
            if (response != null && response.getList() != null && !response.getList().isEmpty()) {
                updateUI(response.getList().get(0));
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadAQI(lat, lon);
    }

    private void updateUI(AQIResponse.AQIItem item) {
        int aqi = item.getMain().getAqi();
        tvValue.setText(String.valueOf(aqi));

        int color;
        String status, mask, activity;

        switch (aqi) {
            case 1:
                color = Color.parseColor("#4CAF50"); // Good
                status = getString(R.string.aqi_good);
                mask = getString(R.string.mask_advice_none);
                activity = getString(R.string.activity_advice_good);
                break;
            case 2:
                color = Color.parseColor("#8BC34A"); // Fair
                status = getString(R.string.aqi_fair);
                mask = getString(R.string.mask_advice_none);
                activity = getString(R.string.activity_advice_good);
                break;
            case 3:
                color = Color.parseColor("#FFC107"); // Moderate
                status = getString(R.string.aqi_moderate);
                mask = getString(R.string.mask_advice_sensitive);
                activity = "Avoid prolonged outdoor exertion.";
                break;
            case 4:
                color = Color.parseColor("#FF9800"); // Poor
                status = getString(R.string.aqi_poor);
                mask = getString(R.string.mask_advice_required);
                activity = getString(R.string.activity_advice_bad);
                break;
            default:
                color = Color.parseColor("#F44336"); // Very Poor
                status = getString(R.string.aqi_very_poor);
                mask = getString(R.string.mask_advice_required);
                activity = getString(R.string.activity_advice_bad);
                break;
        }

        layoutBg.setBackgroundColor(color);
        tvStatus.setText(status);
        tvMask.setText(mask);
        tvActivity.setText(activity);

        // Details
        List<AQIDetail> details = new ArrayList<>();
        AQIResponse.Components comp = item.getComponents();
        if (comp != null) {
            details.add(new AQIDetail("PM2.5", String.valueOf(comp.getPm25()), "μg/m³"));
            details.add(new AQIDetail("PM10", String.valueOf(comp.getPm10()), "μg/m³"));
            details.add(new AQIDetail("CO", String.valueOf(comp.getCo()), "μg/m³"));
            details.add(new AQIDetail("NO2", String.valueOf(comp.getNo2()), "μg/m³"));
            details.add(new AQIDetail("O3", String.valueOf(comp.getO3()), "μg/m³"));
            details.add(new AQIDetail("SO2", String.valueOf(comp.getSo2()), "μg/m³"));
        }
        adapter.setDetails(details);
    }
}
