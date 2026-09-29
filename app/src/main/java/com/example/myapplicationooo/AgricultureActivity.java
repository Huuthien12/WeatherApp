package com.example.myapplicationooo;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgricultureActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private MapLibreMap map;
    private final AgricultureRepository repository = new AgricultureRepository();

    private View cardAgricultureInfo;
    private TextView tvSelectedLocation, tvTemperature, tvHumidity, tvRain,
            tvWind, tvAgricultureAssessment, tvAgricultureRecommendation;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapLibre.getInstance(this);
        setContentView(R.layout.activity_agriculture);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        mapView = findViewById(R.id.mapView);
        cardAgricultureInfo = findViewById(R.id.cardAgricultureInfo);
        tvSelectedLocation = findViewById(R.id.tvSelectedLocation);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvRain = findViewById(R.id.tvRain);
        tvWind = findViewById(R.id.tvWind);
        tvAgricultureAssessment = findViewById(R.id.tvAgricultureAssessment);
        tvAgricultureRecommendation = findViewById(R.id.tvAgricultureRecommendation);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap mapLibreMap) {
        map = mapLibreMap;
        map.setStyle(new Style.Builder().fromUri("https://demotiles.maplibre.org/style.json"));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(11.9404, 108.4583), 9));

        map.addOnMapClickListener(point -> {
            loadAgricultureWeather(point.getLatitude(), point.getLongitude());
            return true;
        });
    }

    private void loadAgricultureWeather(double lat, double lon) {
        tvSelectedLocation.setText(String.format(Locale.getDefault(),
                "Đang tải: %.4f, %.4f", lat, lon));
        cardAgricultureInfo.setVisibility(View.VISIBLE);

        repository.fetch(lat, lon, new Callback<AgricultureWeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<AgricultureWeatherResponse> call,
                                   @NonNull Response<AgricultureWeatherResponse> response) {
                AgricultureWeatherResponse body = response.body();
                if (!response.isSuccessful() || body == null || body.getCurrent() == null) {
                    showLoadError();
                    return;
                }

                AgricultureWeatherResponse.Current current = body.getCurrent();
                tvSelectedLocation.setText(String.format(Locale.getDefault(),
                        "Tọa độ: %.4f, %.4f", lat, lon));
                tvTemperature.setText(String.format(Locale.getDefault(), "%.1f°C", current.getTemperature()));
                tvHumidity.setText(String.format(Locale.getDefault(), "%d%%", current.getHumidity()));
                tvRain.setText(String.format(Locale.getDefault(), "%.1f mm", current.getPrecipitation()));
                tvWind.setText(String.format(Locale.getDefault(), "%.1f km/h", current.getWindSpeed()));

                updateAgricultureAdvice(current);
            }

            @Override
            public void onFailure(@NonNull Call<AgricultureWeatherResponse> call, @NonNull Throwable t) {
                showLoadError();
            }
        });
    }

    private void updateAgricultureAdvice(AgricultureWeatherResponse.Current current) {
        String assessment;
        String recommendation;

        if (current.getPrecipitation() > 0 || current.getRain() > 0) {
            assessment = "Khu vực đang có mưa hoặc lượng mưa ghi nhận được.";
            recommendation = "Không nên phun thuốc. Hạn chế tưới bổ sung và theo dõi thoát nước cho cây trồng.";
        } else if (current.getWindSpeed() >= 20) {
            assessment = "Gió khá mạnh, có thể ảnh hưởng đến hoạt động ngoài đồng.";
            recommendation = "Không nên phun thuốc khi gió mạnh; kiểm tra giàn, cây non và vật tư ngoài trời.";
        } else if (current.getHumidity() >= 85) {
            assessment = "Độ ẩm không khí cao, điều kiện có thể thuận lợi cho nấm bệnh phát triển.";
            recommendation = "Theo dõi nấm bệnh và thông thoáng khu vực trồng; cân nhắc thời điểm phun phù hợp.";
        } else if (current.getTemperature() >= 33) {
            assessment = "Nhiệt độ cao có thể làm cây mất nước nhanh.";
            recommendation = "Ưu tiên tưới vào sáng sớm hoặc chiều mát và theo dõi dấu hiệu thiếu nước.";
        } else {
            assessment = "Điều kiện khí tượng hiện tại tương đối ổn định cho hoạt động canh tác.";
            recommendation = "Có thể thực hiện công việc ngoài đồng; tiếp tục theo dõi thời tiết trước khi tưới hoặc phun thuốc.";
        }

        tvAgricultureAssessment.setText(assessment);
        tvAgricultureRecommendation.setText(recommendation);
    }

    private void showLoadError() {
        Toast.makeText(this, "Không thể tải dữ liệu thời tiết cho vị trí này.", Toast.LENGTH_SHORT).show();
        tvAgricultureAssessment.setText("Chưa có dữ liệu để đánh giá.");
        tvAgricultureRecommendation.setText("Hãy chọn lại vị trí hoặc kiểm tra kết nối mạng.");
    }

    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { mapView.onPause(); super.onPause(); }
    @Override protected void onStop() { mapView.onStop(); super.onStop(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
}
