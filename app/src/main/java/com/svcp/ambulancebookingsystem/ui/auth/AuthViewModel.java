package com.svcp.ambulancebookingsystem.ui.auth;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.svcp.ambulancebookingsystem.data.model.User;
import com.svcp.ambulancebookingsystem.data.repository.AuthRepository;
import com.svcp.ambulancebookingsystem.utils.Constants;


public class AuthViewModel extends AndroidViewModel {
    private AuthRepository authRepository;
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository();
    }

    public LiveData<User> loginUser(String email, String password) {
        isLoading.setValue(true);

        if (email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Email and password cannot be empty");
            isLoading.setValue(false);
            return new MutableLiveData<>(null);
        }

        LiveData<User> result = authRepository.loginUser(email, password);

        // Observe the result to update loading state
        observeOnce(result, user -> {
            isLoading.setValue(false);
            if (user == null) {
                errorMessage.setValue("Login failed. Please check your credentials.");
            } else {
                // Save user data to SharedPreferences
                saveUserToPrefs(user);
            }
        });

        return result;
    }

    public LiveData<User> registerUser(String name, String email, String password, String confirmPassword, String phone) {
        isLoading.setValue(true);

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            errorMessage.setValue("All fields are required");
            isLoading.setValue(false);
            return new MutableLiveData<>(null);
        }

        if (!password.equals(confirmPassword)) {
            errorMessage.setValue("Passwords do not match");
            isLoading.setValue(false);
            return new MutableLiveData<>(null);
        }

        if (password.length() < 6) {
            errorMessage.setValue("Password should be at least 6 characters");
            isLoading.setValue(false);
            return new MutableLiveData<>(null);
        }

        LiveData<User> result = authRepository.registerUser(name, email, password, phone);

        // Observe the result to update loading state
        observeOnce(result, user -> {
            isLoading.setValue(false);
            if (user == null) {
                errorMessage.setValue("Registration failed. Please try again.");
            } else {
                // Save user data to SharedPreferences
                saveUserToPrefs(user);
            }
        });

        return result;
    }

    public LiveData<User> googleSignIn(String idToken) {
        isLoading.setValue(true);

        LiveData<User> result = authRepository.googleSignIn(idToken);

        // Observe the result to update loading state
        observeOnce(result, user -> {
            isLoading.setValue(false);
            if (user == null) {
                errorMessage.setValue("Google Sign-in failed. Please try again.");
            } else {
                // Save user data to SharedPreferences
                saveUserToPrefs(user);
            }
        });

        return result;
    }

    public void logout() {
        authRepository.logout();
        // Clear SharedPreferences
        SharedPreferences prefs = getApplication().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public boolean isUserLoggedIn() {
        return authRepository.isUserLoggedIn();
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    private void saveUserToPrefs(User user) {
        SharedPreferences prefs = getApplication().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREF_USER_ID, user.getUserId());
        editor.putString(Constants.PREF_USER_EMAIL, user.getEmail());
        editor.putBoolean(Constants.PREF_IS_ADMIN, user.isAdmin());
        editor.apply();
    }

    // Helper method to observe LiveData once
    private <T> void observeOnce(LiveData<T> liveData, OnObservedListener<T> listener) {
        liveData.observeForever(new androidx.lifecycle.Observer<T>() {
            @Override
            public void onChanged(T t) {
                listener.onObserved(t);
                liveData.removeObserver(this);
            }
        });
    }

    private interface OnObservedListener<T> {
        void onObserved(T t);
    }
}