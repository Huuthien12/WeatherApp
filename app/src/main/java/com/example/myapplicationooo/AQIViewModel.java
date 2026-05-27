package com.example.myapplicationooo;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AQIViewModel extends AndroidViewModel {
    private final AQIRepository repository = new AQIRepository();
    private final MutableLiveData<AQIResponse> aqiData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AQIViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<AQIResponse> getAqiData() {
        return aqiData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadAQI(double lat, double lon) {
        isLoading.setValue(true);
        repository.fetchAQI(lat, lon, new Callback<AQIResponse>() {
            @Override
            public void onResponse(@NonNull Call<AQIResponse> call, @NonNull Response<AQIResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    aqiData.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to load AQI data");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AQIResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t.getMessage());
            }
        });
    }
}
