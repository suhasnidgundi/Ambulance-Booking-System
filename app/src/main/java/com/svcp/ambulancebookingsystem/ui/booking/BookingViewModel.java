package com.svcp.ambulancebookingsystem.ui.booking;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.svcp.ambulancebookingsystem.data.model.Ambulance;
import com.svcp.ambulancebookingsystem.data.model.Booking;
import com.svcp.ambulancebookingsystem.data.model.User;
import com.svcp.ambulancebookingsystem.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookingViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final MutableLiveData<List<Ambulance>> availableAmbulances = new MutableLiveData<>();
    private final MutableLiveData<Booking> currentBooking = new MutableLiveData<>();
    private final MutableLiveData<String> bookingStatus = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<List<Booking>> bookingHistory = new MutableLiveData<>();

    public BookingViewModel() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public LiveData<List<Ambulance>> getAvailableAmbulances() {
        loadAvailableAmbulances();
        return availableAmbulances;
    }

    public LiveData<Booking> getCurrentBooking() {
        loadCurrentUserBooking();
        return currentBooking;
    }

    public LiveData<String> getBookingStatus() {
        return bookingStatus;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<List<Booking>> getBookingHistory() {
        loadBookingHistory();
        return bookingHistory;
    }

    private void loadAvailableAmbulances() {
        db.collection(Constants.AMBULANCES_COLLECTION)
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Ambulance> ambulanceList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Ambulance ambulance = document.toObject(Ambulance.class);
                        ambulanceList.add(ambulance);
                    }
                    availableAmbulances.setValue(ambulanceList);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Failed to load ambulances: " + e.getMessage());
                    availableAmbulances.setValue(new ArrayList<>());
                });
    }

    public void createBooking(String pickupAddress, GeoPoint pickupLocation,
                              String destinationAddress, GeoPoint destinationLocation,
                              String patientName, String patientCondition) {
        if (auth.getCurrentUser() == null) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // Get user details
        db.collection(Constants.USERS_COLLECTION).document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        User user = userDoc.toObject(User.class);
                        String bookingId = UUID.randomUUID().toString();

                        Booking booking = new Booking(
                                bookingId,
                                userId,
                                user.getName(),
                                user.getPhone(),
                                pickupLocation,
                                pickupAddress,
                                destinationLocation,
                                destinationAddress,
                                patientName,
                                patientCondition
                        );

                        // Save booking to Firestore
                        db.collection(Constants.BOOKINGS_COLLECTION)
                                .document(bookingId)
                                .set(booking)
                                .addOnSuccessListener(aVoid -> {
                                    // Add booking to user's history
                                    user.addToBookingHistory(bookingId);
                                    db.collection(Constants.USERS_COLLECTION)
                                            .document(userId)
                                            .update("bookingHistory", user.getBookingHistory());

                                    bookingStatus.setValue("Booking created successfully");
                                    currentBooking.setValue(booking);
                                })
                                .addOnFailureListener(e -> errorMessage.setValue("Failed to create booking: " + e.getMessage()));
                    } else {
                        errorMessage.setValue("User profile not found");
                    }
                })
                .addOnFailureListener(e -> errorMessage.setValue("Failed to get user data: " + e.getMessage()));
    }

    public void cancelBooking(String bookingId) {
        db.collection(Constants.BOOKINGS_COLLECTION)
                .document(bookingId)
                .update("status", Constants.STATUS_CANCELLED)
                .addOnSuccessListener(aVoid -> {
                    bookingStatus.setValue("Booking cancelled successfully");
                    loadCurrentUserBooking();
                })
                .addOnFailureListener(e -> errorMessage.setValue("Failed to cancel booking: " + e.getMessage()));
    }

    private void loadCurrentUserBooking() {
        if (auth.getCurrentUser() == null) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // Create a list of valid statuses (those that are not COMPLETED or CANCELLED)
        List<String> activeStatuses = new ArrayList<>();
        activeStatuses.add(Constants.STATUS_PENDING);
        activeStatuses.add(Constants.STATUS_ACCEPTED);
        activeStatuses.add(Constants.STATUS_IN_PROGRESS);

        db.collection(Constants.BOOKINGS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereIn("status", activeStatuses)  // Use whereIn instead of multiple whereNotEqualTo
                .orderBy("bookingTime", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Booking booking = queryDocumentSnapshots.getDocuments().get(0).toObject(Booking.class);
                        currentBooking.setValue(booking);
                    } else {
                        currentBooking.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("BookingViewModel", "Error loading current booking: " + e.getMessage());
                    errorMessage.setValue("Failed to load current booking: " + e.getMessage());
                    currentBooking.setValue(null);
                });
    }

    private void loadBookingHistory() {
        if (auth.getCurrentUser() == null) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection(Constants.BOOKINGS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("bookingTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Booking> history = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Booking booking = document.toObject(Booking.class);
                        history.add(booking);
                    }
                    bookingHistory.setValue(history);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Failed to load booking history: " + e.getMessage());
                    bookingHistory.setValue(new ArrayList<>());
                });
    }

    public void trackBooking(String bookingId) {
        db.collection(Constants.BOOKINGS_COLLECTION)
                .document(bookingId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        errorMessage.setValue("Error tracking booking: " + e.getMessage());
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Booking booking = documentSnapshot.toObject(Booking.class);
                        currentBooking.setValue(booking);
                    }
                });
    }
}