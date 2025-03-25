package com.svcp.ambulancebookingsystem.utils;

public class Constants {
    // Firebase Collection Names
    public static final String USERS_COLLECTION = "users";
    public static final String AMBULANCES_COLLECTION = "ambulances";
    public static final String BOOKINGS_COLLECTION = "bookings";
    public static final String EMERGENCY_CONTACTS_COLLECTION = "emergency_contacts";

    // Booking Status
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Intent Keys
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_BOOKING_ID = "booking_id";
    public static final String KEY_AMBULANCE_ID = "ambulance_id";
    public static final String KEY_IS_ADMIN = "is_admin";

    // Location Updates
    public static final int LOCATION_UPDATE_INTERVAL = 10000; // 10 seconds
    public static final int FASTEST_LOCATION_INTERVAL = 5000; // 5 seconds

    // Notification Channels
    public static final String BOOKING_NOTIFICATION_CHANNEL_ID = "booking_channel";
    public static final String TRACKING_NOTIFICATION_CHANNEL_ID = "tracking_channel";

    // Shared Preferences
    public static final String PREFS_NAME = "ambulance_booking_prefs";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_IS_ADMIN = "is_admin";
    public static final String PREF_FCM_TOKEN = "fcm_token";

    // Emergency Numbers
    public static final String EMERGENCY_NUMBER = "911"; // Replace with your country's emergency number
}