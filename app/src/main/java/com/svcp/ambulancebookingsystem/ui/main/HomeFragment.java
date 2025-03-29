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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}