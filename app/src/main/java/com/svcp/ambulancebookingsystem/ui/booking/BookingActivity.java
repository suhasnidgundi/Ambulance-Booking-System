package com.svcp.ambulancebookingsystem.ui.booking;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.databinding.ActivityBookingBinding;

public class BookingActivity extends AppCompatActivity {
    private ActivityBookingBinding binding;
    private BookingViewModel bookingViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        // Load BookingFragment inside BookingActivity
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new BookingFragment())
                .commit();
    }
}
