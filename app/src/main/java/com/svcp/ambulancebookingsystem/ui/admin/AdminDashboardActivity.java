package com.svcp.ambulancebookingsystem.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.svcp.ambulancebookingsystem.R;
import com.google.android.material.navigation.NavigationView;
import com.svcp.ambulancebookingsystem.ui.admin.bookings.ManageBookingsActivity;
import com.svcp.ambulancebookingsystem.ui.admin.profile.AdminProfileActivity;
import com.svcp.ambulancebookingsystem.ui.admin.users.ManageUsersActivity;
import com.svcp.ambulancebookingsystem.ui.auth.LoginActivity;

public class AdminDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AdminViewModel adminViewModel;
    private DrawerLayout drawerLayout;
    private TextView tvActiveBookings, tvTotalBookings, tvTotalAmbulances, tvTotalDrivers, tvTotalUsers, tvTotalRevenue;
    private RecyclerView rvRecentBookings;
    private TextView tvNoBookings;
    private Button btnViewAllBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize ViewModel
        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        // Setup toolbar and drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set admin info in nav header
        View headerView = navigationView.getHeaderView(0);
        TextView tvAdminName = headerView.findViewById(R.id.tvAdminName);
        TextView tvAdminEmail = headerView.findViewById(R.id.tvAdminEmail);
        tvAdminName.setText(adminViewModel.getName());
        tvAdminEmail.setText(adminViewModel.getEmail());

        // Initialize dashboard statistics views - Updated to match IDs in the XML layout
        tvActiveBookings = findViewById(R.id.tv_active_bookings);
        tvTotalBookings = findViewById(R.id.tv_total_bookings);
        tvTotalAmbulances = findViewById(R.id.tv_total_ambulances);
        tvTotalDrivers = findViewById(R.id.tv_total_drivers);
        tvTotalUsers = findViewById(R.id.tv_total_users);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);

        // Initialize RecyclerView for recent bookings
        rvRecentBookings = findViewById(R.id.rv_recent_bookings);
        tvNoBookings = findViewById(R.id.tv_no_bookings);
        btnViewAllBookings = findViewById(R.id.btn_view_all_bookings);

        // Setup RecyclerView
        setupRecentBookingsRecyclerView();

        // Setup ViewAll button
        btnViewAllBookings.setOnClickListener(v -> navigateToManageBookings());

        // Load dashboard data
        loadDashboardData();
    }

    private void setupRecentBookingsRecyclerView() {
        rvRecentBookings.setLayoutManager(new LinearLayoutManager(this));
        // You would typically set an adapter here
        // RecentBookingsAdapter adapter = new RecentBookingsAdapter(this);
        // rvRecentBookings.setAdapter(adapter);
    }

    private void loadDashboardData() {
        // Show loading indicators
        showLoading(true);

        // Fetch dashboard summary
        adminViewModel.getDashboardSummary();

        // Observe dashboard data changes
        adminViewModel.getDashboardSummaryData().observe(this, dashboardSummary -> {
            if (dashboardSummary != null) {
                updateDashboardUI();
                updateRecentBookings();
                showLoading(false);
            }
        });
    }

    private void updateDashboardUI() {
        tvActiveBookings.setText(String.valueOf(adminViewModel.getActiveBookings()));
        tvTotalBookings.setText(String.valueOf(adminViewModel.getTotalBookings()));
        tvTotalAmbulances.setText(String.valueOf(adminViewModel.getTotalAmbulances()));
        tvTotalDrivers.setText(String.valueOf(adminViewModel.getTotalDrivers()));
        tvTotalUsers.setText(String.valueOf(adminViewModel.getTotalUsers()));
        // Set total revenue
        tvTotalRevenue.setText("₹" + adminViewModel.getTotalRevenue());
    }

    private void updateRecentBookings() {
        // Check if recent bookings are available
        if (adminViewModel.getRecentBookings() != null && !adminViewModel.getRecentBookings().isEmpty()) {
            rvRecentBookings.setVisibility(View.VISIBLE);
            tvNoBookings.setVisibility(View.GONE);

            // Update your RecyclerView adapter here
            // adapter.setBookings(adminViewModel.getRecentBookings());
        } else {
            rvRecentBookings.setVisibility(View.GONE);
            tvNoBookings.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean isLoading) {
        // Since there's no progressBar in the XML layout, we might need to add one
        // or implement a different loading indicator
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            // Already on dashboard
        } else if (id == R.id.nav_manage_users) {
            navigateToManageUsers();
        } else if (id == R.id.nav_manage_drivers) {
            navigateToManageDrivers();
        } else if (id == R.id.nav_manage_ambulances) {
            navigateToManageAmbulances();
        } else if (id == R.id.nav_manage_bookings) {
            navigateToManageBookings();
        } else if (id == R.id.nav_reports) {
            navigateToReports();
        } else if (id == R.id.nav_profile) {
            navigateToProfile();
        } else if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void navigateToManageUsers() {
        Intent intent = new Intent(this, ManageUsersActivity.class);
        startActivity(intent);
    }

    private void navigateToManageDrivers() {
        // Intent intent = new Intent(this, ManageDriversActivity.class);
        // startActivity(intent);
        Toast.makeText(this, "Navigate to Manage Drivers", Toast.LENGTH_SHORT).show();
    }

    private void navigateToManageAmbulances() {
        // Intent intent = new Intent(this, ManageAmbulancesActivity.class);
        // startActivity(intent);
        Toast.makeText(this, "Navigate to Manage Ambulances", Toast.LENGTH_SHORT).show();
    }

    private void navigateToManageBookings() {
        Intent intent = new Intent(this, ManageBookingsActivity.class);
        startActivity(intent);
    }

    private void navigateToReports() {
        // Intent intent = new Intent(this, ReportsActivity.class);
        // startActivity(intent);
        Toast.makeText(this, "Navigate to Reports", Toast.LENGTH_SHORT).show();
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, AdminProfileActivity.class);
        startActivity(intent);
    }

    private void logout() {
        adminViewModel.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}