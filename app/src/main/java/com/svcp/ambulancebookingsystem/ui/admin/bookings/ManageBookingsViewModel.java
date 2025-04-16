package com.svcp.ambulancebookingsystem.ui.admin.bookings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.svcp.ambulancebookingsystem.data.model.Booking;

import java.util.ArrayList;
import java.util.List;

public class ManageBookingsViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<Booking>> bookingsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public void getBookings(String status) {
        Query query = db.collection("bookings");
        
        if (!status.equals("all")) {
            query = query.whereEqualTo("status", status);
        }
        
        query.orderBy("bookingTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Booking> bookingList = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Booking booking = document.toObject(Booking.class);
                    booking.setId(document.getId());
                    bookingList.add(booking);
                }
                bookingsLiveData.setValue(bookingList);
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error loading bookings: " + e.getMessage());
            });
    }

    public void updateBookingStatus(String bookingId, String newStatus) {
        db.collection("bookings").document(bookingId)
            .update("status", newStatus)
            .addOnSuccessListener(aVoid -> {
                // Update the local list
                List<Booking> currentBookings = bookingsLiveData.getValue();
                if (currentBookings != null) {
                    for (Booking booking : currentBookings) {
                        if (booking.getId().equals(bookingId)) {
                            booking.setStatus(newStatus);
                            break;
                        }
                    }
                    bookingsLiveData.setValue(currentBookings);
                }
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error updating booking status: " + e.getMessage());
            });
    }

    public LiveData<List<Booking>> getBookingsLiveData() {
        return bookingsLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
}