package com.svcp.ambulancebookingsystem.ui.booking;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.databinding.ActivityBookingBinding;
import com.svcp.ambulancebookingsystem.utils.Constants;

import java.util.Objects;

public class BookingActivity extends AppCompatActivity {
    private ActivityBookingBinding binding;
    private BookingViewModel bookingViewModel;
    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Book an Ambulance");

        // Initialize ViewModel
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        // Check if we're editing an existing booking
        if (getIntent() != null && getIntent().hasExtra(Constants.KEY_BOOKING_ID)) {
            bookingId = getIntent().getStringExtra(Constants.KEY_BOOKING_ID);
        }

        // Create and load booking fragment with the booking ID if available
        BookingFragment fragment = new BookingFragment();
        if (bookingId != null) {
            Bundle args = new Bundle();
            args.putString(Constants.KEY_BOOKING_ID, bookingId);
            fragment.setArguments(args);
        }

        // Load fragment into container
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}