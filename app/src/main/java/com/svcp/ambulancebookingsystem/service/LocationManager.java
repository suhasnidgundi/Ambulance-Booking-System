package com.svcp.ambulancebookingsystem.service;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.svcp.ambulancebookingsystem.utils.Constants;

/**
 * Manager class for handling location service and permissions
 */
public class LocationManager {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private final Context context;
    private FusedLocationProviderClient fusedLocationClient;
    
    public LocationManager(Context context) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }
    
    /**
     * Starts the location service for a specific ambulance
     */
    public void startLocationService(String ambulanceId) {
        if (hasLocationPermission()) {
            Intent serviceIntent = new Intent(context, LocationService.class);
            serviceIntent.putExtra(Constants.KEY_AMBULANCE_ID, ambulanceId);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        } else {
            Toast.makeText(context, "Location permission is required", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Stops the location service
     */
    public void stopLocationService() {
        Intent serviceIntent = new Intent(context, LocationService.class);
        context.stopService(serviceIntent);
    }
    
    /**
     * Requests a single current location update
     */
    public void getCurrentLocation(OnLocationObtainedListener listener) {
        if (hasLocationPermission()) {
            try {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                listener.onLocationObtained(location);
                            } else {
                                listener.onLocationFailed("Location is null");
                            }
                        })
                        .addOnFailureListener(e -> {
                            listener.onLocationFailed(e.getMessage());
                        });
            } catch (SecurityException e) {
                listener.onLocationFailed("Security exception: " + e.getMessage());
            }
        } else {
            listener.onLocationFailed("Location permission not granted");
        }
    }
    
    /**
     * Returns LiveData for continuous location updates
     */
    public LiveData<Location> getLocationUpdates() {
        return LocationService.getCurrentLocation();
    }
    
    /**
     * Checks if the app has location permissions
     */
    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Requests location permissions
     */
    public void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }
    
    /**
     * Interface for location callbacks
     */
    public interface OnLocationObtainedListener {
        void onLocationObtained(Location location);
        void onLocationFailed(String error);
    }
}