package com.svcp.ambulancebookingsystem.service;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Helper class for geocoding operations (converting between addresses and coordinates)
 */
public class GeocodingHelper {
    private static final String TAG = "GeocodingHelper";
    
    private final Context context;
    private final Geocoder geocoder;
    private final Executor executor;
    private final Handler mainHandler;
    
    public GeocodingHelper(Context context) {
        this.context = context;
        this.geocoder = new Geocoder(context, Locale.getDefault());
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Gets an address from coordinates
     */
    public void getAddressFromLocation(GeoPoint geoPoint, GeocodeCallback<String> callback) {
        getAddressFromLocation(geoPoint.getLatitude(), geoPoint.getLongitude(), callback);
    }
    
    /**
     * Gets an address from coordinates
     */
    public void getAddressFromLocation(double latitude, double longitude, GeocodeCallback<String> callback) {
        executor.execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder sb = new StringBuilder();
                    
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        sb.append(address.getAddressLine(i));
                        if (i < address.getMaxAddressLineIndex()) {
                            sb.append(", ");
                        }
                    }
                    
                    String addressText = sb.toString();
                    mainHandler.post(() -> callback.onSuccess(addressText));
                } else {
                    mainHandler.post(() -> callback.onError("No address found"));
                }
            } catch (IOException e) {
                Log.e(TAG, "Error getting address: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Geocoding failed: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Gets coordinates from an address
     */
    public void getLocationFromAddress(String addressString, GeocodeCallback<GeoPoint> callback) {
        executor.execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    GeoPoint geoPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
                    mainHandler.post(() -> callback.onSuccess(geoPoint));
                } else {
                    mainHandler.post(() -> callback.onError("No location found for address"));
                }
            } catch (IOException e) {
                Log.e(TAG, "Error getting location: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Geocoding failed: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Callback interface for geocoding operations
     */
    public interface GeocodeCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }
}