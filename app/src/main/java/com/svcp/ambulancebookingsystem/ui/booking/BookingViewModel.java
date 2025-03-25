package com.svcp.ambulancebookingsystem.ui.booking;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.svcp.ambulancebookingsystem.data.model.Ambulance;
import com.svcp.ambulancebookingsystem.data.model.Booking;
import com.svcp.ambulancebookingsystem.data.model.User;
import com.svcp.ambulancebookingsystem.data.remote.FCMService;
import com.svcp.ambulancebookingsystem.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final MutableLiveData<List<Booking>> userBookings = new MutableLiveData<>();
    private final MutableLiveData<Booking> currentBooking = new MutableLiveData<>();
    private final MutableLiveData<String> bookingError = new MutableLiveData<>();
    private final MutableLiveData<String> bookingStatus = new MutableLiveData<>();
    private ListenerRegistration bookingStatusListener;

    public BookingViewModel() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public LiveData<Booking> createBooking(
            GeoPoint pickupLocation,
            String pickupAddress,
            GeoPoint destinationLocation,
            String destinationAddress,
            String patientName,
            String patientCondition
    ) {
        String userId = auth.getCurrentUser().getUid();

        db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);

                    if (user != null) {
                        // Find nearest available ambulance
                        findNearestAvailableAmbulance(pickupLocation)
                                .addOnSuccessListener(ambulance -> {
                                    Booking newBooking = createBookingObject(
                                            userId, user, pickupLocation, pickupAddress,
                                            destinationLocation, destinationAddress,
                                            patientName, patientCondition, ambulance
                                    );

                                    saveBookingToFirestore(newBooking, ambulance);
                                })
                                .addOnFailureListener(e -> {
                                    bookingError.setValue("No available ambulance: " + e.getMessage());
                                });
                    }
                });

        return currentBooking;
    }

    private Task<Ambulance> findNearestAvailableAmbulance(GeoPoint pickupLocation) {
        return db.collection(Constants.AMBULANCES_COLLECTION)
                .whereEqualTo("available", true)
                .get()
                .continueWith(task -> {
                    QuerySnapshot snapshot = task.getResult();
                    List<Ambulance> availableAmbulances = snapshot.toObjects(Ambulance.class);

                    // Implement logic to find nearest ambulance (simplified here)
                    return availableAmbulances.isEmpty() ? null : availableAmbulances.get(0);
                });
    }

    private Booking createBookingObject(
            String userId, User user,
            GeoPoint pickupLocation, String pickupAddress,
            GeoPoint destinationLocation, String destinationAddress,
            String patientName, String patientCondition,
            Ambulance ambulance
    ) {
        Booking newBooking = new Booking(
                null, userId, user.getName(), user.getPhone(),
                pickupLocation, pickupAddress,
                destinationLocation, destinationAddress,
                patientName, patientCondition
        );

        // Assign ambulance details
        newBooking.setAmbulanceId(ambulance.getAmbulanceId());
        newBooking.setAssignedDriverId(ambulance.getDriverId());
        newBooking.setAssignedDriverName(ambulance.getDriverName());

        return newBooking;
    }

    private void saveBookingToFirestore(Booking newBooking, Ambulance ambulance) {
        DocumentReference bookingRef = db.collection(Constants.BOOKINGS_COLLECTION).document();
        newBooking.setBookingId(bookingRef.getId());

        // Batch write to update multiple documents
        db.runBatch(batch -> {
            // Save booking
            batch.set(bookingRef, newBooking);

            // Update ambulance availability
            DocumentReference ambulanceRef = db.collection(Constants.AMBULANCES_COLLECTION)
                    .document(ambulance.getAmbulanceId());
            batch.update(ambulanceRef, "available", false,
                    "currentBookingId", newBooking.getBookingId());

            // Update user booking history
            DocumentReference userRef = db.collection(Constants.USERS_COLLECTION)
                    .document(newBooking.getUserId());
            batch.update(userRef, "bookingHistory",
                    com.google.firebase.firestore.FieldValue.arrayUnion(newBooking.getBookingId()));
        }).addOnSuccessListener(aVoid -> {
            // Send notification to driver
            FCMService.sendDriverBookingNotification(newBooking);

            currentBooking.setValue(newBooking);
            listenToBookingStatusChanges(newBooking.getBookingId());
        }).addOnFailureListener(e -> {
            bookingError.setValue("Booking creation failed: " + e.getMessage());
        });
    }

    public void listenToBookingStatusChanges(String bookingId) {
        // Cancel previous listener if exists
        if (bookingStatusListener != null) {
            bookingStatusListener.remove();
        }

        bookingStatusListener = db.collection(Constants.BOOKINGS_COLLECTION)
                .document(bookingId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        bookingError.setValue("Error listening to booking status: " + e.getMessage());
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Booking updatedBooking = snapshot.toObject(Booking.class);
                        currentBooking.setValue(updatedBooking);
                        bookingStatus.setValue(updatedBooking.getStatus());
                    }
                });
    }

    public void cancelBooking(String bookingId) {
        db.collection(Constants.BOOKINGS_COLLECTION).document(bookingId)
                .update("status", "CANCELLED")
                .addOnSuccessListener(aVoid -> {
                    // Make ambulance available again
                    db.collection(Constants.BOOKINGS_COLLECTION).document(bookingId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                Booking booking = documentSnapshot.toObject(Booking.class);
                                if (booking != null && booking.getAmbulanceId() != null) {
                                    db.collection(Constants.AMBULANCES_COLLECTION)
                                            .document(booking.getAmbulanceId())
                                            .update("available", true, "currentBookingId", null);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    bookingError.setValue("Cancellation failed: " + e.getMessage());
                });
    }

    public void rateBooking(String bookingId, int rating, String feedback) {
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("rating", rating);
        ratingData.put("feedback", feedback);

        db.collection(Constants.BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(ratingData)
                .addOnFailureListener(e -> {
                    bookingError.setValue("Rating update failed: " + e.getMessage());
                });
    }

    public LiveData<String> getBookingStatus() {
        return bookingStatus;
    }

    public LiveData<String> getBookingError() {
        return bookingError;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (bookingStatusListener != null) {
            bookingStatusListener.remove();
        }
    }

    public LiveData<List<Booking>> getUserBookings() {
        return userBookings;
    }
}