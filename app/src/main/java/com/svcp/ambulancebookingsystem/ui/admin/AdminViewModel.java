package com.svcp.ambulancebookingsystem.ui.admin;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.svcp.ambulancebookingsystem.data.model.*;
import com.svcp.ambulancebookingsystem.data.repository.AdminRepository;

import java.util.ArrayList;
import java.util.List;

public class AdminViewModel extends AndroidViewModel {
    // SharedPreferences constants
    public static final String PREF_NAME = "AdminPreferences";
    public static final String PREF_ADMIN_ID = "admin_id";
    public static final String PREF_ADMIN_NAME = "admin_name";
    public static final String PREF_ADMIN_EMAIL = "admin_email";

    private final AdminRepository adminRepository;
    private final SharedPreferences sharedPreferences;
    private final MutableLiveData<DashboardSummary> dashboardSummaryLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Booking>> recentBookingsLiveData = new MutableLiveData<>();

    public AdminViewModel(@NonNull Application application) {
        super(application);
        adminRepository = new AdminRepository();
        sharedPreferences = application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        recentBookingsLiveData.setValue(new ArrayList<>()); // Initialize with empty list
    }

    public void login(String email, String password, AdminLoginCallback callback) {
        adminRepository.login(email, password, new AdminRepository.LoginCallback() {
            @Override
            public void onSuccess(Admin admin) {
                saveAdminSession(admin);
                callback.onSuccess();
            }

            @Override
            public void onFailure(String message) {
                callback.onFailure(message);
            }
        });
    }

    public void logout() {
        // Clear shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Sign out from Firebase Auth
        FirebaseAuth.getInstance().signOut();
    }

    private void saveAdminSession(Admin admin) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_ADMIN_ID, admin.getId());
        editor.putString(PREF_ADMIN_NAME, admin.getName());
        editor.putString(PREF_ADMIN_EMAIL, admin.getEmail());
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getString(PREF_ADMIN_ID, null) != null;
    }

    public String getId() {
        return sharedPreferences.getString(PREF_ADMIN_ID, "");
    }

    public String getName() {
        return sharedPreferences.getString(PREF_ADMIN_NAME, "");
    }

    public String getEmail() {
        return sharedPreferences.getString(PREF_ADMIN_EMAIL, "");
    }

    public void getDashboardSummary() {
        adminRepository.getDashboardSummary(new AdminRepository.DashboardSummaryCallback() {
            @Override
            public void onSuccess(DashboardSummary dashboardSummary) {
                dashboardSummaryLiveData.postValue(dashboardSummary);

                // Fetch recent bookings
                fetchRecentBookings();
            }

            @Override
            public void onFailure(String message) {
                // Handle error
            }
        });
    }

    private void fetchRecentBookings() {
        adminRepository.getRecentBookings(5, new AdminRepository.BookingsCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                recentBookingsLiveData.postValue(bookings);
            }

            @Override
            public void onFailure(String message) {
                // Handle error
            }
        });
    }

    public LiveData<DashboardSummary> getDashboardSummaryData() {
        return dashboardSummaryLiveData;
    }

    public LiveData<List<Booking>> getRecentBookingsData() {
        return recentBookingsLiveData;
    }

    public List<Booking> getRecentBookings() {
        return recentBookingsLiveData.getValue();
    }

    public int getActiveBookings() {
        DashboardSummary summary = dashboardSummaryLiveData.getValue();
        return summary != null ? summary.getActiveBookings() : 0;
    }

    public int getTotalBookings() {
        DashboardSummary summary = dashboardSummaryLiveData.getValue();
        return summary != null ? summary.getTotalBookings() : 0;
    }

    public int getTotalAmbulances() {
        DashboardSummary summary = dashboardSummaryLiveData.getValue();
        return summary != null ? summary.getTotalAmbulances() : 0;
    }

    public int getTotalDrivers() {
        DashboardSummary summary = dashboardSummaryLiveData.getValue();
        return summary != null ? summary.getTotalDrivers() : 0;
    }

    public int getTotalUsers() {
        DashboardSummary summary = dashboardSummaryLiveData.getValue();
        return summary != null ? summary.getTotalUsers() : 0;
    }

    public double getTotalRevenue() {
        DashboardSummary summary = dashboardSummaryLiveData.getValue();
        return summary != null ? summary.getTotalRevenue() : 0.0;
    }

    public interface AdminLoginCallback {
        void onSuccess();
        void onFailure(String message);
    }
}