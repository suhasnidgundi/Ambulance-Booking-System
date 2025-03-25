package com.svcp.ambulancebookingsystem.ui.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.svcp.ambulancebookingsystem.databinding.ActivityUserProfileBinding;
import com.svcp.ambulancebookingsystem.data.model.User;

public class UserProfileActivity extends AppCompatActivity {
    private ActivityUserProfileBinding binding;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        userViewModel.getUserData().observe(this, user -> {
            if (user != null) {
                binding.etUserName.setText(user.getName());
                binding.etUserEmail.setText(user.getEmail());
                binding.etUserPhone.setText(user.getPhone());
                binding.etUserAddress.setText(user.getAddress());
            }
        });

        binding.btnSaveProfile.setOnClickListener(v -> saveUserProfile());
    }

    private void saveUserProfile() {
        String name = binding.etUserName.getText().toString();
        String phone = binding.etUserPhone.getText().toString();
        String address = binding.etUserAddress.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        User updatedUser = new User();
        updatedUser.setName(name);
        updatedUser.setPhone(phone);
        updatedUser.setAddress(address);

        userViewModel.updateUserProfile(updatedUser);
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }
}
