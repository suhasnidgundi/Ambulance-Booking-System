package com.svcp.ambulancebookingsystem.data.model;

import com.google.firebase.firestore.GeoPoint;

public class Ambulance {
    private String ambulanceId;
    private String driverId;
    private String driverName;
    private String vehicleNumber;
    private String phone;
    private boolean available;
    private GeoPoint location;
    private String currentBookingId;

    public Ambulance() {
        // Required empty constructor for Firestore
    }

    public Ambulance(String ambulanceId, String driverId, String driverName, String vehicleNumber, String phone) {
        this.ambulanceId = ambulanceId;
        this.driverId = driverId;
        this.driverName = driverName;
        this.vehicleNumber = vehicleNumber;
        this.phone = phone;
        this.available = true;
    }

    // Getters and Setters
    public String getAmbulanceId() {
        return ambulanceId;
    }

    public void setAmbulanceId(String ambulanceId) {
        this.ambulanceId = ambulanceId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getCurrentBookingId() {
        return currentBookingId;
    }

    public void setCurrentBookingId(String currentBookingId) {
        this.currentBookingId = currentBookingId;
    }
}