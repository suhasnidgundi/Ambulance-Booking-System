package com.svcp.ambulancebookingsystem.ui.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.svcp.ambulancebookingsystem.data.model.User;
import com.svcp.ambulancebookingsystem.utils.Constants;

public class UserViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final MutableLiveData<User> userData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public UserViewModel() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        loadUserData();
    }

    private void loadUserData() {
        String userId = auth.getCurrentUser().getUid();
        db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    userData.setValue(user);
                })
                .addOnFailureListener(e -> errorMessage.setValue("Failed to load user data: " + e.getMessage()));
    }

    public LiveData<User> getUserData() {
        return userData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void updateUserProfile(User updatedUser) {
        String userId = auth.getCurrentUser().getUid();
        db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .set(updatedUser)
                .addOnSuccessListener(aVoid -> userData.setValue(updatedUser))
                .addOnFailureListener(e -> errorMessage.setValue("Failed to update profile: " + e.getMessage()));
    }
}
