package com.svcp.ambulancebookingsystem.ui.admin.users;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.svcp.ambulancebookingsystem.data.model.User;
import com.svcp.ambulancebookingsystem.data.repository.UserRepository;

import java.util.List;

public class UsersViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
    private final MutableLiveData<User> selectedUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public UsersViewModel() {
        userRepository = new UserRepository();
    }

    public void loadUsers() {
        isLoadingLiveData.setValue(true);

        userRepository.getAllUsers(new UserRepository.UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                usersLiveData.postValue(users);
                isLoadingLiveData.postValue(false);
            }

            @Override
            public void onFailure(String message) {
                errorMessageLiveData.postValue(message);
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void getUserDetails(String userId) {
        isLoadingLiveData.setValue(true);

        userRepository.getUserById(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                selectedUserLiveData.postValue(user);
                isLoadingLiveData.postValue(false);
            }

            @Override
            public void onFailure(String message) {
                errorMessageLiveData.postValue(message);
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void addUser(User user, OperationCallback callback) {
        isLoadingLiveData.setValue(true);

        userRepository.addUser(user, new UserRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                loadUsers();  // Refresh the list
                callback.onSuccess();
                isLoadingLiveData.postValue(false);
            }

            @Override
            public void onFailure(String message) {
                errorMessageLiveData.postValue(message);
                callback.onFailure(message);
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void updateUser(User user, OperationCallback callback) {
        isLoadingLiveData.setValue(true);

        userRepository.updateUser(user, new UserRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                loadUsers();  // Refresh the list
                callback.onSuccess();
                isLoadingLiveData.postValue(false);
            }

            @Override
            public void onFailure(String message) {
                errorMessageLiveData.postValue(message);
                callback.onFailure(message);
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void deleteUser(String userId, OperationCallback callback) {
        isLoadingLiveData.setValue(true);

        userRepository.deleteUser(userId, new UserRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                loadUsers();  // Refresh the list
                callback.onSuccess();
                isLoadingLiveData.postValue(false);
            }

            @Override
            public void onFailure(String message) {
                errorMessageLiveData.postValue(message);
                callback.onFailure(message);
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void toggleUserStatus(String userId, boolean isActive, OperationCallback callback) {
        isLoadingLiveData.setValue(true);

        userRepository.toggleUserStatus(userId, isActive, new UserRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                loadUsers();  // Refresh the list
                callback.onSuccess();
                isLoadingLiveData.postValue(false);
            }

            @Override
            public void onFailure(String message) {
                errorMessageLiveData.postValue(message);
                callback.onFailure(message);
                isLoadingLiveData.postValue(false);
            }
        });
    }

    // LiveData getters
    public LiveData<List<User>> getUsersLiveData() {
        return usersLiveData;
    }

    public LiveData<User> getSelectedUserLiveData() {
        return selectedUserLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    // Operation callback interface
    public interface OperationCallback {
        void onSuccess();
        void onFailure(String message);
    }
}