package com.svcp.ambulancebookingsystem.ui.admin.bookings;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.data.model.Booking;

public class ManageBookingsActivity extends AppCompatActivity implements BookingAdapter.BookingActionListener {

    private ManageBookingsViewModel viewModel;
    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_bookings);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Manage Bookings");

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewBookings);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        tabLayout = findViewById(R.id.tabLayout);

        // Set up ViewModel
        viewModel = new ViewModelProvider(this).get(ManageBookingsViewModel.class);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(this);
        recyclerView.setAdapter(adapter);

        // Set up tabs
        setupTabs();

        // Load all bookings by default
        loadBookings("all");
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Pending"));
        tabLayout.addTab(tabLayout.newTab().setText("Confirmed"));
        tabLayout.addTab(tabLayout.newTab().setText("Completed"));
        tabLayout.addTab(tabLayout.newTab().setText("Cancelled"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String status;
                switch (tab.getPosition()) {
                    case 0:
                        status = "all";
                        break;
                    case 1:
                        status = "pending";
                        break;
                    case 2:
                        status = "confirmed";
                        break;
                    case 3:
                        status = "completed";
                        break;
                    case 4:
                        status = "cancelled";
                        break;
                    default:
                        status = "all";
                        break;
                }
                loadBookings(status);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void loadBookings(String status) {
        showLoading(true);
        viewModel.getBookings(status);
        
        viewModel.getBookingsLiveData().observe(this, bookings -> {
            showLoading(false);
            if (bookings != null && !bookings.isEmpty()) {
                adapter.submitList(bookings);
                showEmptyView(false);
            } else {
                showEmptyView(true);
            }
        });
        
        viewModel.getErrorLiveData().observe(this, error -> {
            showLoading(false);
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showEmptyView(boolean isEmpty) {
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onViewDetails(Booking booking) {
        // Show booking details in a dialog or navigate to details screen
        Toast.makeText(this, "View details for booking " + booking.getId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdateStatus(Booking booking, String newStatus) {
        viewModel.updateBookingStatus(booking.getId(), newStatus);
        Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
    }
}