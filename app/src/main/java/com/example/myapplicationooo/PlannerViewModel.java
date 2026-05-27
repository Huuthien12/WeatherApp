package com.example.myapplicationooo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import javax.annotation.Nullable;

public class PlannerViewModel extends ViewModel {
    private final PlannerRepository repository = new PlannerRepository();
    private final MutableLiveData<List<PlanModel>> plans = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<PlanModel>> getPlans() {
        return plans;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchPlans() {
        Query query = repository.getPlans();
        if (query == null) return;

        isLoading.setValue(true);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                isLoading.setValue(false);
                if (error != null) {
                    errorMessage.setValue(error.getMessage());
                    return;
                }
                if (value != null) {
                    plans.setValue(value.toObjects(PlanModel.class));
                }
            }
        });
    }

    public void addPlan(String title, String city, Timestamp time) {
        isLoading.setValue(true);
        PlanModel newPlan = new PlanModel(com.google.firebase.auth.FirebaseAuth.getInstance().getUid(), title, city, time);
        repository.addPlan(newPlan, new PlannerRepository.OnPlanActionListener() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void deletePlan(String id) {
        repository.deletePlan(id, new PlannerRepository.OnPlanActionListener() {
            @Override
            public void onSuccess() {
                // List will update via snapshot listener
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue(e.getMessage());
            }
        });
    }
}
