package com.example.myapplicationooo;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RadarViewModel extends AndroidViewModel {
    private final MarineRepository repository = new MarineRepository();
    private final MutableLiveData<MarineWeatherResponse> marineData = new MutableLiveData<>();
    private final MutableLiveData<TideResponse> tideData = new MutableLiveData<>();
    private final MutableLiveData<String> safetyAlert = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public RadarViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<MarineWeatherResponse> getMarineData() { return marineData; }
    public LiveData<TideResponse> getTideData() { return tideData; }
    public LiveData<String> getSafetyAlert() { return safetyAlert; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void loadMarineInfo(double lat, double lon) {
        isLoading.setValue(true);
        
        repository.fetchMarineData(lat, lon, new Callback<MarineWeatherResponse>() {
            @Override
            public void onResponse(Call<MarineWeatherResponse> call, Response<MarineWeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    marineData.setValue(response.body());
                    analyzeSafety(response.body());
                } else {
                    marineData.setValue(null);
                    safetyAlert.setValue("No marine data");
                }
                checkLoadingStatus();
            }

            @Override
            public void onFailure(Call<MarineWeatherResponse> call, Throwable t) {
                marineData.setValue(null);
                checkLoadingStatus();
            }
        });

        repository.fetchTideData(lat, lon, new Callback<TideResponse>() {
            @Override
            public void onResponse(Call<TideResponse> call, Response<TideResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tideData.setValue(response.body());
                } else {
                    tideData.setValue(null);
                }
                checkLoadingStatus();
            }

            @Override
            public void onFailure(Call<TideResponse> call, Throwable t) {
                tideData.setValue(null);
                checkLoadingStatus();
            }
        });
    }

    private void checkLoadingStatus() {
        isLoading.setValue(false);
    }

    private void analyzeSafety(MarineWeatherResponse data) {
        if (data == null || data.getCurrent() == null) {
            safetyAlert.setValue("No marine data");
            return;
        }

        double wave = data.getCurrent().getWaveHeight();
        double wind = data.getCurrent().getWindSpeed();

        String alert;
        if (wave > 4.0 || wind > 50) {
            alert = getApplication().getString(R.string.extreme_sea_warning);
        } else if (wave > 2.5) {
            alert = getApplication().getString(R.string.high_wave_warning);
        } else if (wind > 30) {
            alert = getApplication().getString(R.string.gale_warning);
        } else {
            alert = getApplication().getString(R.string.safe_conditions);
        }
        safetyAlert.setValue(alert);
    }

    public String formatTideInfo(TideResponse response) {
        if (response == null || response.getHourly() == null 
                || response.getHourly().getHeight() == null 
                || response.getHourly().getHeight().isEmpty()) {
            return "N/A";
        }

        try {
            double currentHeight = response.getHourly().getHeight().get(0);
            return String.format(Locale.getDefault(), "%.2f m", currentHeight);
        } catch (Exception e) {
            return "N/A";
        }
    }
}
