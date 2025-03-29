package com.svcp.ambulancebookingsystem.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;
import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.data.remote.FirebaseAmbulanceSource;
import com.svcp.ambulancebookingsystem.ui.main.MainActivity;
import com.svcp.ambulancebookingsystem.utils.Constants;

/**
 * A foreground service that tracks location updates and sends them to Firebase
 * for real-time ambulance location tracking.
 */
public class LocationService extends Service {
    private static final String TAG = "LocationService";

    // Location tracking
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    // Firebase
    private FirebaseAmbulanceSource ambulanceSource;
    private String ambulanceId;

    // LiveData for current location
    private final MutableLiveData<Location> currentLocation = new MutableLiveData<>();
    
    // Notification ID and Channel
    private static final int NOTIFICATION_ID = 12345;
    private static final String CHANNEL_ID = Constants.TRACKING_NOTIFICATION_CHANNEL_ID;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        ambulanceSource = new FirebaseAmbulanceSource();
        
        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();
        
        // Create notification channel for Android O and above
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            ambulanceId = intent.getStringExtra(Constants.KEY_AMBULANCE_ID);
            
            if (ambulanceId == null) {
                Log.e(TAG, "Ambulance ID is null. Cannot start location service.");
                stopSelf();
                return START_NOT_STICKY;
            }
            
            // Start as a foreground service with notification
            startForeground();
            
            // Start location updates
            startLocationUpdates();
        }
        
        // If this service is killed, restart it
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
    
    /**
     * Creates the location request with the defined intervals
     */
    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(Constants.LOCATION_UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(Constants.FASTEST_LOCATION_INTERVAL)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();
    }
    
    /**
     * Creates the location callback that handles location updates
     */
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                
                // Process the latest location
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    // Update LiveData for any observers
                    currentLocation.setValue(location);
                    
                    // Update Firestore with new location
                    updateLocationInFirebase(location);
                    
                    Log.d(TAG, "Location update: " + location.getLatitude() + ", " + location.getLongitude());
                }
            }
        };
    }
    
    /**
     * Starts location updates
     */
    private void startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest, 
                    locationCallback,
                    Looper.getMainLooper()
            );
            Log.d(TAG, "Location updates started");
        } catch (SecurityException e) {
            Log.e(TAG, "Error starting location updates: " + e.getMessage());
        }
    }
    
    /**
     * Stops location updates
     */
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        Log.d(TAG, "Location updates stopped");
    }


    /**
     * Updates the ambulance location in Firebase
     */
    private void updateLocationInFirebase(Location location) {
        if (ambulanceId != null) {
            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

            // Fix for line 175 - using proper observer pattern
            ambulanceSource.updateAmbulanceLocation(ambulanceId, geoPoint)
                    .observeForever(success -> {
                        if (Boolean.FALSE.equals(success)) {
                            Log.e(TAG, "Failed to update location in Firebase");
                        }
                    });
        }
    }
    
    /**
     * Creates notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Tracking Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    
    /**
     * Starts the service as a foreground service with a persistent notification
     */
    private void startForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );
        
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Ambulance Location Service")
                .setContentText("Tracking and sharing your location")
                .setSmallIcon(R.drawable.ic_notification) // Make sure this icon exists in your drawable resources
                .setContentIntent(pendingIntent)
                .build();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                    NOTIFICATION_ID, 
                    notification, 
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            );
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }
    
    /**
     * Public accessor for the current location LiveData
     */
    public static LiveData<Location> getCurrentLocation() {
        return getInstance().currentLocation;
    }
    
    // Static instance for Singleton pattern
    private static LocationService instance;
    
    public static LocationService getInstance() {
        if (instance == null) {
            instance = new LocationService();
        }
        return instance;
    }
}