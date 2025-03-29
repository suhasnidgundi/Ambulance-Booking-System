package com.svcp.ambulancebookingsystem.ui.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.firestore.GeoPoint;
import com.svcp.ambulancebookingsystem.databinding.FragmentBookingBinding;

public class BookingFragment extends Fragment {
    private FragmentBookingBinding binding;
    private BookingViewModel bookingViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        binding.btnBookNow.setOnClickListener(v -> createBooking());
    }

    private void createBooking() {
        GeoPoint pickupLocation = new GeoPoint(37.7749, -122.4194); // Example coordinates
        String pickupAddress = "123 Main St, San Francisco, CA";
        GeoPoint destinationLocation = new GeoPoint(37.7849, -122.4094);
        String destinationAddress = "Hospital, San Francisco, CA";
        String patientName = "John Doe";
        String patientCondition = "Critical";


    }
}
