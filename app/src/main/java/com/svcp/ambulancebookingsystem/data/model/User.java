package com.svcp.ambulancebookingsystem.data.model;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String id;
    private String profileImageUrl;
    private boolean active;
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private boolean isAdmin;
    private List<EmergencyContact> emergencyContacts;
    private String fcmToken;

    private List<String> bookingHistory;

    public User() {
        // Required empty constructor for Firestore
        emergencyContacts = new ArrayList<>();
    }

    public User(String userId, String name, String email, String phone, String address) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.isAdmin = false;
        this.emergencyContacts = new ArrayList<>();
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public List<EmergencyContact> getEmergencyContacts() {
        return emergencyContacts;
    }

    public void setEmergencyContacts(List<EmergencyContact> emergencyContacts) {
        this.emergencyContacts = emergencyContacts;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public List<String> getBookingHistory() {
        return bookingHistory != null ? bookingHistory : new ArrayList<>();
    }

    public void addToBookingHistory(String bookingId) {
        if (this.bookingHistory == null) {
            this.bookingHistory = new ArrayList<>();
        }
        this.bookingHistory.add(bookingId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
