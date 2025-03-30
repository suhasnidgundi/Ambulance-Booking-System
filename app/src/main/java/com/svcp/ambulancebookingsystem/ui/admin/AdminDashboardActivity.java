package com.svcp.ambulancebookingsystem.ui.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.databinding.ActivityAdminDashboardBinding;
import com.svcp.ambulancebookingsystem.databinding.NavHeaderAdminBinding;
import com.svcp.ambulancebookingsystem.ui.admin.ambulances.ManageAmbulancesActivity;
import com.svcp.ambulancebookingsystem.ui.admin.bookings.AdminBookingsActivity;
import com.svcp.ambulancebookingsystem.ui.admin.drivers.ManageDriversActivity;
import com.svcp.ambulancebookingsystem.ui.admin.reports.AdminReportsActivity;
import com.svcp.ambulancebookingsystem.ui.admin.users.ManageUsersActivity;
import com.svcp.ambulancebookingsystem.ui.auth.LoginActivity;
import com.svcp.ambulancebookingsystem.utils.Constants;

public class AdminDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ActivityAdminDashboardBinding binding;
    private NavHeaderAdminBinding headerBinding;
    private AdminViewModel viewModel;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        setupNavigationDrawer();
        setupDashboardCards();
        getAdminInfo();
    }

    private void setupNavigationDrawer() {
        setSupportActionBar(binding.toolbar);
        drawerToggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        binding.navView.setNavigationItemSelectedListener(this);

        // Setup header view
        headerBinding = NavHeaderAdminBinding.bind(binding.navView.getHeaderView(0));
    }

    private void setupDashboardCards() {
        // Setup click listeners for dashboard cards
        binding.cardManageUsers.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageUsersActivity.class));
        });

        binding.cardManageDrivers.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageDriversActivity.class));
        });

        binding.cardManageAmbulances.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageAmbulancesActivity.class));
        });

        binding.cardManageBookings.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminBookingsActivity.class));
        });

        binding.cardReports.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminReportsActivity.class));
        });
    }

    private void getAdminInfo() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        String adminId = prefs.getString(Constants.PREF_USER_ID, "");
        String adminEmail = prefs.getString(Constants.PREF_USER_EMAIL, "");

        if (adminId.isEmpty()) {
            // Admin not logged in, go to login screen
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Load admin data from Firestore
        viewModel.getAdminData(adminId).observe(this, admin -> {
            if (admin != null) {
                headerBinding.tvAdminName.setText(admin.getName());
                headerBinding.tvAdminEmail.setText(admin.getEmail());

                // Update dashboard summary info
                viewModel.getDashboardSummary().observe(this, summary -> {
                    if (summary != null) {
                        binding.tvTotalUsers.setText(String.valueOf(summary.getTotalUsers()));
                        binding.tvTotalDrivers.setText(String.valueOf(summary.getTotalDrivers()));
                        binding.tvTotalAmbulances.setText(String.valueOf(summary.getTotalAmbulances()));
                        binding.tvTotalBookings.setText(String.valueOf(summary.getTotalBookings()));
                        binding.tvActiveBookings.setText(String.valueOf(summary.getActiveBookings()));
                    }
                });
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation item clicks
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            // Already on dashboard, do nothing
        } else if (id == R.id.nav_manage_users) {
            startActivity(new Intent(this, ManageUsersActivity.class));
        } else if (id == R.id.nav_manage_drivers) {
            startActivity(new Intent(this, ManageDriversActivity.class));
        } else if (id == R.id.nav_manage_ambulances) {
            startActivity(new Intent(this, ManageAmbulancesActivity.class));
        } else if (id == R.id.nav_manage_bookings) {
            startActivity(new Intent(this, AdminBookingsActivity.class));
        } else if (id == R.id.nav_reports) {
            startActivity(new Intent(this, AdminReportsActivity.class));
        } else if (id == R.id.nav_logout) {
            logoutAdmin();
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logoutAdmin() {
        viewModel.logout();

        // Clear shared preferences
        SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // Go to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}