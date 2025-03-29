package com.svcp.ambulancebookingsystem.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationUtils {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int REQUEST_CHECK_SETTINGS = 1002;

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private final MutableLiveData<GeoPoint> currentLocation = new MutableLiveData<>();
    private final MutableLiveData<String> currentAddress = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean requestingLocationUpdates = false;

    public LocationUtils(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        setupLocationRequest();
        setupLocationCallback();
    }

    private void setupLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(15000)
                .build();
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update location data
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    currentLocation.setValue(geoPoint);

                    // Get address from location
                    getAddressFromLocation(location);
                }
            }
        };
    }

    public boolean checkLocationPermissions() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    public void checkLocationSettings(Activity activity) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(context)
                .checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
            // All location settings are satisfied, start location updates
            startLocationUpdates();
        });

        task.addOnFailureListener(e -> {
            int statusCode = ((ApiException) e).getStatusCode();
            if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                // Location settings are not satisfied, show dialog to fix
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    errorMessage.setValue("Error: Unable to start location settings resolution");
                }
            } else if (statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                // Location settings can't be fixed, inform user
                errorMessage.setValue("Error: Location settings cannot be changed to meet requirements");
            }
        });
    }

    public void getLastKnownLocation() {
        if (!checkLocationPermissions()) {
            errorMessage.setValue("Location permission not granted");
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            currentLocation.setValue(geoPoint);
                            getAddressFromLocation(location);
                        } else {
                            startLocationUpdates(); // No last known location, request updates
                        }
                    })
                    .addOnFailureListener(e -> {
                        errorMessage.setValue("Error getting location: " + e.getMessage());
                        startLocationUpdates(); // Try to request updates instead
                    });
        } catch (SecurityException e) {
            errorMessage.setValue("Security exception: " + e.getMessage());
        }
    }

    public void startLocationUpdates() {
        if (!checkLocationPermissions()) {
            errorMessage.setValue("Location permission not granted");
            return;
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
            requestingLocationUpdates = true;
        } catch (SecurityException e) {
            errorMessage.setValue("Security exception when requesting location updates: " + e.getMessage());
            requestingLocationUpdates = false;
        }
    }

    public void stopLocationUpdates() {
        if (requestingLocationUpdates) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            requestingLocationUpdates = false;
        }
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
            );

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        sb.append(", ");
                    }
                }

                currentAddress.setValue(sb.toString());
            } else {
                currentAddress.setValue("Address not found");
            }
        } catch (IOException e) {
            errorMessage.setValue("Error getting address: " + e.getMessage());
        }
    }

    public String getAddressFromGeoPoint(GeoPoint geoPoint) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    geoPoint.getLatitude(),
                    geoPoint.getLongitude(),
                    1
            );

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        sb.append(", ");
                    }
                }

                return sb.toString();
            } else {
                return "Address not found";
            }
        } catch (IOException e) {
            return "Error getting address";
        }
    }

    public GeoPoint getGeoPointFromAddress(String addressString) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(addressString, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return new GeoPoint(address.getLatitude(), address.getLongitude());
            }
        } catch (IOException e) {
            errorMessage.setValue("Error getting location from address: " + e.getMessage());
        }

        return null;
    }

    public double calculateDistance(GeoPoint start, GeoPoint end) {
        if (start == null || end == null) {
            return 0;
        }

        Location startLocation = new Location("start");
        startLocation.setLatitude(start.getLatitude());
        startLocation.setLongitude(start.getLongitude());

        Location endLocation = new Location("end");
        endLocation.setLatitude(end.getLatitude());
        endLocation.setLongitude(end.getLongitude());

        // Returns distance in meters
        return startLocation.distanceTo(endLocation);
    }

    public List<GeoPoint> findNearbyHospitals(GeoPoint currentLocation, double radiusInKm) {
        // This is a placeholder - In a real app, you would use Places API or similar service
        // For demo purposes, returning dummy data
        List<GeoPoint> hospitals = new ArrayList<>();
        hospitals.add(new GeoPoint(currentLocation.getLatitude() + 0.01, currentLocation.getLongitude() + 0.01));
        hospitals.add(new GeoPoint(currentLocation.getLatitude() - 0.01, currentLocation.getLongitude() + 0.01));
        hospitals.add(new GeoPoint(currentLocation.getLatitude() + 0.01, currentLocation.getLongitude() - 0.01));
        return hospitals;
    }

    // LiveData getters
    public LiveData<GeoPoint> getCurrentLocation() {
        return currentLocation;
    }

    public LiveData<String> getCurrentAddress() {
        return currentAddress;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void handleActivityResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                // User agreed to make required location settings changes
                startLocationUpdates();
            } else {
                // User chose not to make required location settings changes
                errorMessage.setValue("Location settings not satisfied, some features may not work correctly");
            }
        }
    }

    public void handlePermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                getLastKnownLocation();
            } else {
                errorMessage.setValue("Location permission denied, some features may not work correctly");
            }
        }
    }
}