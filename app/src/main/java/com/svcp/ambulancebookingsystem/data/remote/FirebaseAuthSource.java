package com.svcp.ambulancebookingsystem.data.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.svcp.ambulancebookingsystem.data.model.User;
import com.svcp.ambulancebookingsystem.utils.Constants;

public class FirebaseAuthSource {
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public FirebaseAuthSource() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<User> loginUser(String email, String password) {
        MutableLiveData<User> userMutableLiveData = new MutableLiveData<>();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            fetchUserProfile(firebaseUser.getUid(), userMutableLiveData);
                        }
                    } else {
                        userMutableLiveData.setValue(null);
                    }
                });

        return userMutableLiveData;
    }

    public LiveData<User> registerUser(String name, String email, String password, String phone) {
        MutableLiveData<User> userMutableLiveData = new MutableLiveData<>();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            createUserProfile(firebaseUser.getUid(), name, email, phone, userMutableLiveData);
                        }
                    } else {
                        userMutableLiveData.setValue(null);
                    }
                });

        return userMutableLiveData;
    }

    public LiveData<User> googleSignIn(String idToken) {
        MutableLiveData<User> userMutableLiveData = new MutableLiveData<>();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Check if the user exists in Firestore
                            db.collection(Constants.USERS_COLLECTION).document(firebaseUser.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            User user = documentSnapshot.toObject(User.class);
                                            userMutableLiveData.setValue(user);
                                        } else {
                                            // Create new user profile from Google account info
                                            String name = firebaseUser.getDisplayName();
                                            String email = firebaseUser.getEmail();
                                            String phone = firebaseUser.getPhoneNumber() != null ?
                                                    firebaseUser.getPhoneNumber() : "";
                                            createUserProfile(firebaseUser.getUid(), name, email, phone, userMutableLiveData);
                                        }
                                    })
                                    .addOnFailureListener(e -> userMutableLiveData.setValue(null));
                        }
                    } else {
                        userMutableLiveData.setValue(null);
                    }
                });

        return userMutableLiveData;
    }

    private void createUserProfile(String userId, String name, String email, String phone, MutableLiveData<User> userMutableLiveData) {
        User user = new User(userId, name, email, phone, "");
        db.collection(Constants.USERS_COLLECTION).document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> userMutableLiveData.setValue(user))
                .addOnFailureListener(e -> userMutableLiveData.setValue(null));
    }

    private void fetchUserProfile(String userId, MutableLiveData<User> userMutableLiveData) {
        db.collection(Constants.USERS_COLLECTION).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        userMutableLiveData.setValue(user);
                    } else {
                        userMutableLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> userMutableLiveData.setValue(null));
    }

    public void logout() {
        auth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }
}