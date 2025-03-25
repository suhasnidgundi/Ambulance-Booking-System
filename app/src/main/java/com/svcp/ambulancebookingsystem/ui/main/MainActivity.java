package com.svcp.ambulancebookingsystem.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.databinding.ActivityMainBinding;
import com.svcp.ambulancebookingsystem.databinding.NavHeaderMainBinding;
import com.svcp.ambulancebookingsystem.ui.auth.LoginActivity;
import com.svcp.ambulancebookingsystem.ui.booking.BookingActivity;
import com.svcp.ambulancebookingsystem.ui.user.EmergencyContactsActivity;
import com.svcp.ambulancebookingsystem.ui.user.UserProfileActivity;
import com.svcp.ambulancebookingsystem.utils.Constants;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ActivityMainBinding binding;
    private NavHeaderMainBinding headerBinding;
    private MainViewModel viewModel;
    private ActionBarDrawerToggle drawerToggle;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupNavigationDrawer();
        setupNavController();
        getUserInfo();
        setupFCM();
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
        headerBinding = NavHeaderMainBinding.bind(binding.navView.getHeaderView(0));
    }

    private void setupNavController() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerLayout);
        } else {
            Log.e("MainActivity", "NavHostFragment is null. Check XML setup.");
        }
    }


    private void getUserInfo() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        userId = prefs.getString(Constants.PREF_USER_ID, "");
        String userEmail = prefs.getString(Constants.PREF_USER_EMAIL, "");

        if (userId.isEmpty()) {
            // User not logged in, go to login screen
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Load user data from Firestore
        viewModel.getUserData(userId).observe(this, user -> {
            if (user != null) {
                headerBinding.tvUserName.setText(user.getName());
                headerBinding.tvUserEmail.setText(user.getEmail());
            }
        });
    }

    private void setupFCM() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();
                viewModel.updateFCMToken(userId, token);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already in home, just close drawer
        } else if (id == R.id.nav_book_ambulance) {
            startActivity(new Intent(this, BookingActivity.class));
        } else if (id == R.id.nav_emergency_contacts) {
            startActivity(new Intent(this, EmergencyContactsActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, UserProfileActivity.class));
        } else if (id == R.id.nav_logout) {
            viewModel.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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