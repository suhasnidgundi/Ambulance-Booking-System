package com.svcp.ambulancebookingsystem.ui.admin;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.svcp.ambulancebookingsystem.utils.Constants;

import static android.content.Context.MODE_PRIVATE;

public class AdminViewModel extends AndroidViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SharedPreferences preferences;

    public AdminViewModel(@NonNull Application application) {
        super(application);
        preferences = application.getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
    }

    public boolean isAdminLoggedIn() {
        String adminId = preferences.getString(Constants.PREF_ADMIN_ID, "");
        return !adminId.isEmpty();
    }

    public String getAdminId() {
        return preferences.getString(Constants.PREF_ADMIN_ID, "");
    }

    public LiveData<Admin> getAdminData(String adminId) {
        MutableLiveData<Admin> adminLiveData = new MutableLiveData<>();

        db.collection("admins")
                .document(adminId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Admin admin = documentSnapshot.toObject(Admin.class);
                    adminLiveData.setValue(admin);
                })
                .addOnFailureListener(e -> adminLiveData.setValue(null));

        return adminLiveData;
    }

    public void logoutAdmin() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.PREF_ADMIN_ID);
        editor.remove(Constants.PREF_ADMIN_EMAIL);
        editor.apply();
    }

    public LiveData<Integer> getTotalUsersCount() {
        MutableLiveData<Integer> countLiveData = new MutableLiveData<>();

        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        countLiveData.setValue(queryDocumentSnapshots.size()))
                .addOnFailureListener(e -> countLiveData.setValue(0));

        return countLiveData;
    }

    public LiveData<Integer> getTotalDriversCount() {
        MutableLiveData<Integer> countLiveData = new MutableLiveData<>();

        db.collection("drivers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        countLiveData.setValue(queryDocumentSnapshots.size()))
                .addOnFailureListener(e -> countLiveData.setValue(0));

        return countLiveData;
    }

    public LiveData<Integer> getTotalAmbulancesCount() {
        MutableLiveData<Integer> countLiveData = new MutableLiveData<>();

        db.collection("ambulances")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        countLiveData.setValue(queryDocumentSnapshots.size()))
                .addOnFailureListener(e -> countLiveData.setValue(0));

        return countLiveData;
    }

    public LiveData<Integer> getTotalBookingsCount() {
        MutableLiveData<Integer> countLiveData = new MutableLiveData<>();

        db.collection("bookings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        countLiveData.setValue(queryDocumentSnapshots.size()))
                .addOnFailureListener(e -> countLiveData.setValue(0));

        return countLiveData;
    }

    public LiveData<Integer> getPendingBookingsCount() {
        MutableLiveData<Integer> countLiveData = new MutableLiveData<>();

        db.collection("bookings")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        countLiveData.setValue(queryDocumentSnapshots.size()))
                .addOnFailureListener(e -> countLiveData.setValue(0));

        return countLiveData;
    }
}