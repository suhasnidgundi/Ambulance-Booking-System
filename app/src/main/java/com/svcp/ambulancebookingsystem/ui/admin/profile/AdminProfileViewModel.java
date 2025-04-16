package com.svcp.ambulancebookingsystem.ui.admin.profile;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.svcp.ambulancebookingsystem.data.model.Admin;
import com.svcp.ambulancebookingsystem.ui.admin.AdminViewModel;

import java.util.HashMap;
import java.util.Map;

public class AdminProfileViewModel extends AndroidViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final SharedPreferences sharedPreferences;
    
    private final MutableLiveData<Admin> adminData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> profileUpdateStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> passwordUpdateStatus = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public AdminProfileViewModel(@NonNull Application application) {
        super(application);
        sharedPreferences = application.getSharedPreferences(AdminViewModel.PREF_NAME, Context.MODE_PRIVATE);
    }

    public void loadAdminData() {
        String adminId = sharedPreferences.getString(AdminViewModel.PREF_ADMIN_ID, null);
        if (adminId == null) {
            error.setValue("Admin session not found");
            return;
        }

        db.collection("admins").document(adminId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Admin admin = documentSnapshot.toObject(Admin.class);
                        if (admin != null) {
                            admin.setId(documentSnapshot.getId());
                            adminData.setValue(admin);
                        }
                    } else {
                        error.setValue("Admin profile not found");
                    }
                })
                .addOnFailureListener(e -> error.setValue("Error loading profile: " + e.getMessage()));
    }

    public void updateAdminProfile(String name, String phone) {
        String adminId = sharedPreferences.getString(AdminViewModel.PREF_ADMIN_ID, null);
        if (adminId == null) {
            error.setValue("Admin session not found");
            return;
        }

        DocumentReference adminRef = db.collection("admins").document(adminId);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        
        adminRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update shared preferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(AdminViewModel.PREF_ADMIN_NAME, name);
                    editor.apply();
                    
                    // Update local data
                    Admin currentAdmin = adminData.getValue();
                    if (currentAdmin != null) {
                        currentAdmin.setName(name);
                        currentAdmin.setPhone(phone);
                        adminData.setValue(currentAdmin);
                    }
                    
                    profileUpdateStatus.setValue(true);
                })
                .addOnFailureListener(e -> error.setValue("Failed to update profile: " + e.getMessage()));
    }

    public void updatePassword(String currentPassword, String newPassword) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            error.setValue("User not authenticated");
            return;
        }
        
        // First reauthenticate the user
        String email = user.getEmail();
        if (email == null) {
            error.setValue("Email not found for current user");
            return;
        }
        
        auth.signInWithEmailAndPassword(email, currentPassword)
                .addOnSuccessListener(authResult -> {
                    // Now update the password
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid -> {
                                passwordUpdateStatus.setValue(true);
                            })
                            .addOnFailureListener(e -> {
                                error.setValue("Failed to update password: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    error.setValue("Current password is incorrect");
                });
    }

    public LiveData<Admin> getAdminData() {
        return adminData;
    }

    public LiveData<Boolean> getProfileUpdateStatus() {
        return profileUpdateStatus;
    }

    public LiveData<Boolean> getPasswordUpdateStatus() {
        return passwordUpdateStatus;
    }

    public LiveData<String> getError() {
        return error;
    }
}