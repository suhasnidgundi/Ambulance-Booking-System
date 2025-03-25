package com.svcp.ambulancebookingsystem.data.remote;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.svcp.ambulancebookingsystem.data.model.Ambulance;
import com.svcp.ambulancebookingsystem.data.model.Booking;
import com.svcp.ambulancebookingsystem.utils.Constants;

public class FirestoreSource {
    private final FirebaseFirestore db;

    public FirestoreSource() {
        db = FirebaseFirestore.getInstance();
    }

    public Query getUserBookings(String userId) {
        return db.collection(Constants.BOOKINGS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("bookingTime", Query.Direction.DESCENDING)
                .limit(50); // Limit to improve performance
    }

    public void addBooking(Booking booking, OnFirestoreBookingListener listener) {
        db.collection(Constants.BOOKINGS_COLLECTION)
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    booking.setBookingId(documentReference.getId());
                    listener.onSuccess(booking);
                })
                .addOnFailureListener(listener::onFailure);
    }

    public void updateBookingStatus(String bookingId, String status, OnFirestoreUpdateListener listener) {
        db.collection(Constants.BOOKINGS_COLLECTION)
                .document(bookingId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    public void assignAmbulanceToBooking(String bookingId, Ambulance ambulance, OnFirestoreUpdateListener listener) {
        WriteBatch batch = db.batch();

        // Update booking with ambulance details
        batch.update(
                db.collection(Constants.BOOKINGS_COLLECTION).document(bookingId),
                "ambulanceId", ambulance.getAmbulanceId(),
                "assignedDriverId", ambulance.getDriverId(),
                "assignedDriverName", ambulance.getDriverName(),
                "status", "ACCEPTED"
        );

        // Update ambulance availability
        batch.update(
                db.collection(Constants.AMBULANCES_COLLECTION).document(ambulance.getAmbulanceId()),
                "available", false,
                "currentBookingId", bookingId
        );

        batch.commit()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    public void addBookingRating(String bookingId, int rating, String feedback, OnFirestoreUpdateListener listener) {
        db.collection(Constants.BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(
                        "rating", rating,
                        "feedback", feedback
                )
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    public interface OnFirestoreBookingListener {
        void onSuccess(Booking booking);
        void onFailure(Exception e);
    }

    public interface OnFirestoreUpdateListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}