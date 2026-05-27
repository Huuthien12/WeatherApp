package com.example.myapplicationooo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {
    private final AuthManager authManager = AuthManager.getInstance();
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AuthViewModel() {
        userLiveData.setValue(authManager.getCurrentUser());
    }

    public LiveData<FirebaseUser> getUserLiveData() { return userLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void login(String email, String password) {
        isLoading.setValue(true);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        userLiveData.setValue(FirebaseAuth.getInstance().getCurrentUser());
                    } else {
                        errorMessage.setValue(task.getException() != null ? task.getException().getMessage() : "Login Failed");
                    }
                });
    }

    public void logout() {
        authManager.logout();
        userLiveData.setValue(null);
    }
}
