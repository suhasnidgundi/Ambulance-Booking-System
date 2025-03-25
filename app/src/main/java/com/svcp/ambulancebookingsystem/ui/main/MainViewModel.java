package com.svcp.ambulancebookingsystem.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.svcp.ambulancebookingsystem.data.model.User;

public class MainViewModel extends ViewModel {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final MutableLiveData<User> userData = new MutableLiveData<>();

    public MainViewModel() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<User> getUserData(String userId) {
        loadUserData(userId);
        return userData;
    }

    private void loadUserData(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        userData.setValue(user);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    userData.setValue(null);
                });
    }

    public void updateFCMToken(String userId, String token) {
        if (userId != null && !userId.isEmpty()) {
            db.collection("users").document(userId)
                    .update("fcmToken", token)
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        }
    }

    public void logout() {
        auth.signOut();
    }
}