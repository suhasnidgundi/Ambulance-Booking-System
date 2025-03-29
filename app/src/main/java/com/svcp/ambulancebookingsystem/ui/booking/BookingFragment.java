package com.svcp.ambulancebookingsystem.ui.booking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.GeoPoint;
import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.data.model.Booking;
import com.svcp.ambulancebookingsystem.databinding.FragmentBookingBinding;
import com.svcp.ambulancebookingsystem.ui.tracking.TrackingActivity;
import com.svcp.ambulancebookingsystem.utils.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class BookingFragment extends Fragment {
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private FragmentBookingBinding binding;
    private BookingViewModel bookingViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private GeoPoint currentPickupLocation;
    private GeoPoint currentDestinationLocation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setupPatientConditionSpinner();
        setupObservers();
        setupClickListeners();

        // Load existing booking if any
        bookingViewModel.getCurrentBooking();
    }

    private void setupPatientConditionSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.patient_conditions,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPatientCondition.setAdapter(adapter);
    }

    private void setupObservers() {
        // Observe current booking
        bookingViewModel.getCurrentBooking().observe(getViewLifecycleOwner(), booking -> {
            if (booking != null) {
                updateUIWithCurrentBooking(booking);
            } else {
                showBookingForm();
            }
        });

        // Observe booking status
        bookingViewModel.getBookingStatus().observe(getViewLifecycleOwner(), status -> {
            binding.txtBookingStatus.setText(status);
            Snackbar.make(binding.getRoot(), status, Snackbar.LENGTH_LONG).show();
        });

        // Observe error messages
        bookingViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            Snackbar.make(binding.getRoot(), errorMsg, Snackbar.LENGTH_LONG).show();
            binding.progressBar.setVisibility(View.GONE);
        });
    }

    private void setupClickListeners() {
        // Book Now button
        binding.btnBookNow.setOnClickListener(v -> {
            if (validateBookingForm()) {
                createBooking();
            }
        });

        // Use Current Location button
        binding.btnUseCurrentLocation.setOnClickListener(v -> {
            requestCurrentLocation();
        });

        // Cancel Booking button
        binding.btnCancelBooking.setOnClickListener(v -> {
            Booking currentBooking = bookingViewModel.getCurrentBooking().getValue();
            if (currentBooking != null) {
                bookingViewModel.cancelBooking(currentBooking.getBookingId());
            }
        });

        // Track Ambulance button
        binding.btnTrackAmbulance.setOnClickListener(v -> {
            Booking currentBooking = bookingViewModel.getCurrentBooking().getValue();
            if (currentBooking != null) {
                Intent trackingIntent = new Intent(requireContext(), TrackingActivity.class);
                trackingIntent.putExtra(Constants.KEY_BOOKING_ID, currentBooking.getBookingId());
                startActivity(trackingIntent);
            }
        });
    }

    private boolean validateBookingForm() {
        boolean isValid = true;

        if (binding.etPickupAddress.getText().toString().trim().isEmpty()) {
            binding.etPickupAddress.setError("Pickup address is required");
            isValid = false;
        }

        if (binding.etDestinationAddress.getText().toString().trim().isEmpty()) {
            binding.etDestinationAddress.setError("Destination address is required");
            isValid = false;
        }

        if (binding.etPatientName.getText().toString().trim().isEmpty()) {
            binding.etPatientName.setError("Patient name is required");
            isValid = false;
        }

        return isValid;
    }

    private void createBooking() {
        binding.progressBar.setVisibility(View.VISIBLE);

        // If we don't have geocoded positions, geocode them now
        if (currentPickupLocation == null) {
            geocodeAddress(binding.etPickupAddress.getText().toString(), true);
        }

        if (currentDestinationLocation == null) {
            geocodeAddress(binding.etDestinationAddress.getText().toString(), false);
        }

        // If both locations are ready, create the booking
        if (currentPickupLocation != null && currentDestinationLocation != null) {
            String pickupAddress = binding.etPickupAddress.getText().toString();
            String destinationAddress = binding.etDestinationAddress.getText().toString();
            String patientName = binding.etPatientName.getText().toString();
            String patientCondition = binding.spinnerPatientCondition.getSelectedItem().toString();

            bookingViewModel.createBooking(
                    pickupAddress,
                    currentPickupLocation,
                    destinationAddress,
                    currentDestinationLocation,
                    patientName,
                    patientCondition
            );
        } else {
            binding.progressBar.setVisibility(View.GONE);
            Snackbar.make(binding.getRoot(), "Still geocoding addresses, please try again in a moment", Snackbar.LENGTH_LONG).show();
        }
    }

    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            binding.progressBar.setVisibility(View.GONE);
            if (location != null) {
                currentPickupLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                getAddressFromLocation(location);
            } else {
                Snackbar.make(binding.getRoot(), "Unable to get current location", Snackbar.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            Snackbar.make(binding.getRoot(), "Location error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        });
    }

    private void getAddressFromLocation(Location location) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressText = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressText.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        addressText.append(", ");
                    }
                }
                binding.etPickupAddress.setText(addressText.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Could not get address from location", Toast.LENGTH_SHORT).show();
        }
    }

    private void geocodeAddress(String addressString, boolean isPickup) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
            if (!addresses.isEmpty()) {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();
                GeoPoint geoPoint = new GeoPoint(latitude, longitude);

                if (isPickup) {
                    currentPickupLocation = geoPoint;
                } else {
                    currentDestinationLocation = geoPoint;
                }
            } else {
                Snackbar.make(binding.getRoot(), "Could not find location for the address", Snackbar.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Snackbar.make(binding.getRoot(), "Geocoding error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void updateUIWithCurrentBooking(Booking booking) {
        binding.layoutCurrentBooking.setVisibility(View.VISIBLE);
        binding.btnBookNow.setVisibility(View.GONE);
        binding.cardPickupLocation.setVisibility(View.GONE);
        binding.cardDestination.setVisibility(View.GONE);
        binding.cardPatientDetails.setVisibility(View.GONE);

        String statusText = "Status: " + booking.getStatus();
        binding.txtBookingStatus.setText(statusText);

        // Show/hide appropriate buttons based on booking status
        if (booking.getStatus().equals(Constants.STATUS_ACCEPTED) ||
                booking.getStatus().equals(Constants.STATUS_IN_PROGRESS)) {
            binding.btnTrackAmbulance.setVisibility(View.VISIBLE);

            // Start tracking the booking automatically
            bookingViewModel.trackBooking(booking.getBookingId());
        } else {
            binding.btnTrackAmbulance.setVisibility(View.GONE);
        }
    }

    private void showBookingForm() {
        binding.layoutCurrentBooking.setVisibility(View.GONE);
        binding.btnBookNow.setVisibility(View.VISIBLE);
        binding.cardPickupLocation.setVisibility(View.VISIBLE);
        binding.cardDestination.setVisibility(View.VISIBLE);
        binding.cardPatientDetails.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestCurrentLocation();
            } else {
                Snackbar.make(binding.getRoot(), "Location permission denied", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}