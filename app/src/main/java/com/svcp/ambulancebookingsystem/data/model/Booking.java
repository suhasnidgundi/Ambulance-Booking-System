package com.svcp.ambulancebookingsystem.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class Booking {
    private String bookingId;
    private String userId;
    private String userName;
    private String userPhone;
    private GeoPoint pickupLocation;
    private String pickupAddress;
    private GeoPoint destinationLocation;
    private String destinationAddress;
    private String patientName;
    private String patientCondition;
    private String ambulanceId;
    private String assignedDriverId;
    private String assignedDriverName;
    private String status; // PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED
    private Timestamp bookingTime;
    private Timestamp acceptedTime;
    private Timestamp completedTime;

    public Booking() {
        // Required empty constructor for Firestore
    }

    public Booking(String bookingId, String userId, String userName, String userPhone,
                   GeoPoint pickupLocation, String pickupAddress,
                   GeoPoint destinationLocation, String destinationAddress,
                   String patientName, String patientCondition) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.pickupLocation = pickupLocation;
        this.pickupAddress = pickupAddress;
        this.destinationLocation = destinationLocation;
        this.destinationAddress = destinationAddress;
        this.patientName = patientName;
        this.patientCondition = patientCondition;
        this.status = "PENDING";
        this.bookingTime = Timestamp.now();
    }

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public GeoPoint getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(GeoPoint pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public GeoPoint getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(GeoPoint destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientCondition() {
        return patientCondition;
    }

    public void setPatientCondition(String patientCondition) {
        this.patientCondition = patientCondition;
    }

    public String getAmbulanceId() {
        return ambulanceId;
    }

    public void setAmbulanceId(String ambulanceId) {
        this.ambulanceId = ambulanceId;
    }

    public String getAssignedDriverId() {
        return assignedDriverId;
    }

    public void setAssignedDriverId(String assignedDriverId) {
        this.assignedDriverId = assignedDriverId;
    }

    public String getAssignedDriverName() {
        return assignedDriverName;
    }

    public void setAssignedDriverName(String assignedDriverName) {
        this.assignedDriverName = assignedDriverName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(Timestamp bookingTime) {
        this.bookingTime = bookingTime;
    }

    public Timestamp getAcceptedTime() {
        return acceptedTime;
    }

    public void setAcceptedTime(Timestamp acceptedTime) {
        this.acceptedTime = acceptedTime;
    }

    public Timestamp getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(Timestamp completedTime) {
        this.completedTime = completedTime;
    }
}