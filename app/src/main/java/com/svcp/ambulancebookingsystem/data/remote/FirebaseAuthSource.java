package com.svcp.ambulancebookingsystem.data.remote;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;


import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.svcp.ambulancebookingsystem.data.model.User;
import com.svcp.ambulancebookingsystem.utils.Constants;

public class FirebaseAuthSource {
    private static final String TAG = "FirebaseAuthSource";
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    public FirebaseAuthSource() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public MutableLiveData<User> loginUser(String email, String password) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        String userId = task.getResult().getUser().getUid();

                        firestore.collection(Constants.USERS_COLLECTION)
                                .document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        User user = documentSnapshot.toObject(User.class);
                                        userLiveData.setValue(user);
                                    } else {
                                        // Create a basic user if not exists
                                        FirebaseUser firebaseUser = task.getResult().getUser();
                                        User newUser = new User(
                                                userId,
                                                firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User",
                                                firebaseUser.getEmail(),
                                                "",
                                                ""
                                        );

                                        firestore.collection(Constants.USERS_COLLECTION)
                                                .document(userId)
                                                .set(newUser)
                                                .addOnSuccessListener(aVoid -> userLiveData.setValue(newUser))
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error creating user document", e);
                                                    userLiveData.setValue(null);
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error getting user document", e);
                                    userLiveData.setValue(null);
                                });
                    } else {
                        Log.e(TAG, "Login failed", task.getException());
                        userLiveData.setValue(null);
                    }
                });

        return userLiveData;
    }

    public MutableLiveData<User> registerUser(String name, String email, String password, String phone) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        String userId = task.getResult().getUser().getUid();

                        User newUser = new User(userId, name, email, phone, "");

                        firestore.collection(Constants.USERS_COLLECTION)
                                .document(userId)
                                .set(newUser)
                                .addOnSuccessListener(aVoid -> userLiveData.setValue(newUser))
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error creating user document", e);
                                    userLiveData.setValue(null);
                                });
                    } else {
                        Log.e(TAG, "Registration failed", task.getException());
                        userLiveData.setValue(null);
                    }
                });

        return userLiveData;
    }

    public MutableLiveData<User> googleSignIn(String idToken) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        String userId = firebaseUser.getUid();

                        firestore.collection(Constants.USERS_COLLECTION)
                                .document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        User user = documentSnapshot.toObject(User.class);
                                        userLiveData.setValue(user);
                                    } else {
                                        // Create a new user
                                        User newUser = new User(
                                                userId,
                                                firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User",
                                                firebaseUser.getEmail(),
                                                firebaseUser.getPhoneNumber() != null ? firebaseUser.getPhoneNumber() : "",
                                                ""
                                        );

                                        firestore.collection(Constants.USERS_COLLECTION)
                                                .document(userId)
                                                .set(newUser)
                                                .addOnSuccessListener(aVoid -> userLiveData.setValue(newUser))
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error creating user document", e);
                                                    userLiveData.setValue(null);
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error getting user document", e);
                                    userLiveData.setValue(null);
                                });
                    } else {
                        Log.e(TAG, "Google sign-in failed", task.getException());
                        userLiveData.setValue(null);
                    }
                });

        return userLiveData;
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
}