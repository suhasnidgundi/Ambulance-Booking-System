package com.svcp.ambulancebookingsystem.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.databinding.FragmentHomeBinding;
import com.svcp.ambulancebookingsystem.ui.booking.BookingViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private MainViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Add recent bookings logic
        BookingViewModel bookingViewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);
        bookingViewModel.getUserBookings().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings != null && !bookings.isEmpty()) {
                binding.tvNoBookings.setVisibility(View.GONE);
                binding.rvRecentBookings.setVisibility(View.VISIBLE);
                // Setup RecyclerView adapter for recent bookings
            } else {
                binding.tvNoBookings.setVisibility(View.VISIBLE);
                binding.rvRecentBookings.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}