package com.svcp.ambulancebookingsystem.data.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.svcp.ambulancebookingsystem.data.model.Ambulance;
import com.svcp.ambulancebookingsystem.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class FirebaseAmbulanceSource {
    private FirebaseFirestore firestore;

    public FirebaseAmbulanceSource() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public LiveData<String> registerAmbulance(Ambulance ambulance) {
        MutableLiveData<String> resultLiveData = new MutableLiveData<>();

        DocumentReference ambulanceRef = firestore.collection(Constants.AMBULANCES_COLLECTION).document();
        ambulance.setAmbulanceId(ambulanceRef.getId());

        ambulanceRef.set(ambulance)
                .addOnSuccessListener(aVoid -> resultLiveData.setValue(ambulance.getAmbulanceId()))
                .addOnFailureListener(e -> resultLiveData.setValue(null));

        return resultLiveData;
    }

    public LiveData<Ambulance> getAmbulance(String ambulanceId) {
        MutableLiveData<Ambulance> ambulanceLiveData = new MutableLiveData<>();

        firestore.collection(Constants.AMBULANCES_COLLECTION)
                .document(ambulanceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Ambulance ambulance = documentSnapshot.toObject(Ambulance.class);
                        ambulanceLiveData.setValue(ambulance);
                    } else {
                        ambulanceLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> ambulanceLiveData.setValue(null));

        return ambulanceLiveData;
    }

    public LiveData<List<Ambulance>> getAvailableAmbulances() {
        MutableLiveData<List<Ambulance>> ambulancesLiveData = new MutableLiveData<>();

        firestore.collection(Constants.AMBULANCES_COLLECTION)
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Ambulance> ambulances = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Ambulance ambulance = document.toObject(Ambulance.class);
                        ambulances.add(ambulance);
                    }
                    ambulancesLiveData.setValue(ambulances);
                })
                .addOnFailureListener(e -> ambulancesLiveData.setValue(new ArrayList<>()));

        return ambulancesLiveData;
    }

    public LiveData<Boolean> updateAmbulanceStatus(String ambulanceId, boolean available, String currentBookingId) {
        MutableLiveData<Boolean> successLiveData = new MutableLiveData<>();

        firestore.collection(Constants.AMBULANCES_COLLECTION)
                .document(ambulanceId)
                .update(
                        "available", available,
                        "currentBookingId", currentBookingId
                )
                .addOnSuccessListener(aVoid -> successLiveData.setValue(true))
                .addOnFailureListener(e -> successLiveData.setValue(false));

        return successLiveData;
    }

    public LiveData<Boolean> updateAmbulanceLocation(String ambulanceId, GeoPoint location) {
        MutableLiveData<Boolean> successLiveData = new MutableLiveData<>();

        firestore.collection(Constants.AMBULANCES_COLLECTION)
                .document(ambulanceId)
                .update("location", location)
                .addOnSuccessListener(aVoid -> successLiveData.setValue(true))
                .addOnFailureListener(e -> successLiveData.setValue(false));

        return successLiveData;
    }
}