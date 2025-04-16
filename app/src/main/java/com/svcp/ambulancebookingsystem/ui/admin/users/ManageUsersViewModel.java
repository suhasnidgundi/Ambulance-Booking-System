package com.svcp.ambulancebookingsystem.ui.admin.users;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.svcp.ambulancebookingsystem.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public void getAllUsers() {
        db.collection("users")
            .whereEqualTo("role", "user")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<User> userList = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    User user = document.toObject(User.class);
                    user.setId(document.getId());
                    userList.add(user);
                }
                usersLiveData.setValue(userList);
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error loading users: " + e.getMessage());
            });
    }

    public void updateUserStatus(String userId, boolean isActive) {
        db.collection("users").document(userId)
            .update("active", isActive)
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error updating user status: " + e.getMessage());
            });
    }

    public void deleteUser(String userId) {
        db.collection("users").document(userId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                List<User> currentUsers = usersLiveData.getValue();
                if (currentUsers != null) {
                    List<User> updatedUsers = new ArrayList<>(currentUsers);
                    updatedUsers.removeIf(user -> user.getId().equals(userId));
                    usersLiveData.setValue(updatedUsers);
                }
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue("Error deleting user: " + e.getMessage());
            });
    }

    public LiveData<List<User>> getUsersLiveData() {
        return usersLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
}