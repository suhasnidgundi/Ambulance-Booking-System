package com.svcp.ambulancebookingsystem.data.repository;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.svcp.ambulancebookingsystem.data.model.User;
import com.svcp.ambulancebookingsystem.data.remote.FirebaseAuthSource;

public class AuthRepository {
    private FirebaseAuthSource authSource;
    private MutableLiveData<User> currentUser = new MutableLiveData<>();

    public AuthRepository() {
        authSource = new FirebaseAuthSource();
    }

    public LiveData<User> loginUser(String email, String password) {
        return authSource.loginUser(email, password);
    }

    public LiveData<User> registerUser(String name, String email, String password, String phone) {
        return authSource.registerUser(name, email, password, phone);
    }

    public LiveData<User> googleSignIn(String idToken) {
        return authSource.googleSignIn(idToken);
    }

    public void logout() {
        authSource.logout();
        currentUser.setValue(null);
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return authSource.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return authSource.isUserLoggedIn();
    }
}