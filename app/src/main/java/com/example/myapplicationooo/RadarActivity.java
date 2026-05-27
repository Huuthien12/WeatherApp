package com.example.myapplicationooo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;

import java.util.Locale;

public class RadarActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private MapLibreMap map;
    private RadarViewModel viewModel;
    private final MarineMapLayerManager layerManager = new MarineMapLayerManager();
    
    private View cardMarineInfo;
    private TextView tvWaveHeight, tvSeaTemp, tvMarineWind, tvTideInfo, tvSafetyAlert, tvDetails;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Khởi tạo MapLibre
        MapLibre.getInstance(this);
        
        setContentView(R.layout.activity_radar);

        viewModel = new ViewModelProvider(this).get(RadarViewModel.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        mapView = findViewById(R.id.mapView);
        
        // Ánh xạ UI
        cardMarineInfo = findViewById(R.id.cardMarineInfo);
        tvWaveHeight = findViewById(R.id.tvWaveHeight);
        tvSeaTemp = findViewById(R.id.tvSeaTemp);
        tvMarineWind = findViewById(R.id.tvMarineWind);
        tvTideInfo = findViewById(R.id.tvTideInfo);
        tvSafetyAlert = findViewById(R.id.tvSafetyAlert);
        tvDetails = findViewById(R.id.tvDetails);

        toolbar.setNavigationOnClickListener(v -> finish());
        
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        findViewById(R.id.fabLayers).setOnClickListener(v -> showLayerDialog());
        findViewById(R.id.fabMyLocation).setOnClickListener(v -> moveToMyLocation());

        setupObservers();
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap mapLibreMap) {
        this.map = mapLibreMap;

        // Thiết lập Style bản đồ (Dark mode phù hợp với Radar)
        mapLibreMap.setStyle(new Style.Builder().fromUri("https://demotiles.maplibre.org/style.json"), style -> {
            layerManager.applyLayer(style, "precipitation_new");
        });

        // Sự kiện chạm bản đồ để xem thông tin biển tại tọa độ đó
        mapLibreMap.addOnMapClickListener(point -> {
            viewModel.loadMarineInfo(point.getLatitude(), point.getLongitude());
            return true;
        });

        moveToMyLocation();
    }

    private void showLayerDialog() {
        String[] layers = {
            getString(R.string.twenty_four_hour_forecast) + " (Precipitation)",
            getString(R.string.wind_speed), 
            "Clouds", 
            "Sea Temperature", 
            "Sea Pressure"
        };
        String[] codes = {"precipitation_new", "wind_new", "clouds_new", "temp_new", "pressure_new"};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Map Layer")
                .setItems(layers, (dialog, which) -> {
                    if (map != null && map.getStyle() != null) {
                        layerManager.applyLayer(map.getStyle(), codes[which]);
                        Toast.makeText(this, "Layer: " + layers[which], Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void moveToMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }
        // Mặc định ra vùng Biển Đông nếu không có tọa độ GPS
        LatLng seaCenter = new LatLng(15.0, 114.0); 
        if (map != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(seaCenter, 5));
        }
    }

    private void setupObservers() {
        // Theo dõi dữ liệu biển
        viewModel.getMarineData().observe(this, data -> {
            if (data != null && data.getCurrent() != null) {
                cardMarineInfo.setVisibility(View.VISIBLE);
                MarineWeatherResponse.Current current = data.getCurrent();
                
                tvWaveHeight.setText(String.format(Locale.getDefault(), "%.1f m", current.getWaveHeight()));
                tvSeaTemp.setText(String.format(Locale.getDefault(), "%.1f°C", current.getSeaTemp()));
                tvMarineWind.setText(String.format(Locale.getDefault(), "%.1f km/h", current.getWindSpeed()));
                
                String details = String.format(Locale.getDefault(), 
                        "Wave Dir: %d° | Period: %.1fs | Wind Dir: %d°",
                        current.getWaveDirection(), current.getWavePeriod(), current.getWindDirection());
                tvDetails.setText(details);
            }
        });

        // Theo dõi dữ liệu thủy triều
        viewModel.getTideData().observe(this, data -> {
            if (data != null) {
                tvTideInfo.setText(viewModel.formatTideInfo(data));
            }
        });

        // Theo dõi cảnh báo an toàn
        viewModel.getSafetyAlert().observe(this, alert -> {
            if (alert != null) {
                tvSafetyAlert.setText(alert);
                if (alert.toLowerCase().contains("warning") || alert.toLowerCase().contains("extreme") 
                    || alert.toLowerCase().contains("cảnh báo") || alert.toLowerCase().contains("cực đoan")) {
                    tvSafetyAlert.setTextColor(Color.RED);
                } else {
                    tvSafetyAlert.setTextColor(Color.parseColor("#4CAF50")); // Green
                }
            }
        });
    }

    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onStop() { super.onStop(); mapView.onStop(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
}
