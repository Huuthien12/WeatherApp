package com.example.myapplicationooo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class TripPlannerViewModel extends ViewModel {
    private final TripPlannerRepository repository = new TripPlannerRepository();
    private final MutableLiveData<List<TripPlan>> plans = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<TripPlan>> getPlans() {
        return plans;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchPlans(String userId) {
        if (userId == null) return;
        isLoading.setValue(true);
        repository.getMyPlans(userId).addSnapshotListener((value, error) -> {
            isLoading.setValue(false);
            if (error != null) {
                errorMessage.setValue(error.getMessage());
                return;
            }
            if (value != null) {
                plans.setValue(value.toObjects(TripPlan.class));
            }
        });
    }

    public void createNewPlan(String userId, String destination, String activityType, String prompt) {
        isLoading.setValue(true);
        TripPlan plan = new TripPlan(userId, destination, activityType, prompt);
        repository.generatePlan(plan, new TripPlannerRepository.OnPlanGeneratedListener() {
            @Override
            public void onSuccess(TripPlan plan) {
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    public void deletePlan(String id) {
        repository.deletePlan(id, new TripPlannerRepository.OnPlanActionListener() {
            @Override
            public void onSuccess() {
                // List updates via snapshot listener
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue(e.getMessage());
            }
        });
    }
}
