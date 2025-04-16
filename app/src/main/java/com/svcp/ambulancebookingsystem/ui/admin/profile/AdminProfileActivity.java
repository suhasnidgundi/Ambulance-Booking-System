package com.svcp.ambulancebookingsystem.ui.admin.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.svcp.ambulancebookingsystem.R;

public class AdminProfileActivity extends AppCompatActivity {

    private AdminProfileViewModel viewModel;
    private EditText etName, etEmail, etPhone;
    private TextInputLayout tilCurrentPassword, tilNewPassword, tilConfirmPassword;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnUpdateProfile, btnUpdatePassword;
    private ProgressBar progressProfile, progressPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Admin Profile");

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(AdminProfileViewModel.class);

        // Initialize views
        etName = findViewById(R.id.etAdminName);
        etEmail = findViewById(R.id.etAdminEmail);
        etPhone = findViewById(R.id.etAdminPhone);
        
        tilCurrentPassword = findViewById(R.id.tilCurrentPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        
        progressProfile = findViewById(R.id.progressProfile);
        progressPassword = findViewById(R.id.progressPassword);

        // Load admin data
        loadAdminData();

        // Set button click listeners
        setupButtonListeners();
        
        // Observe responses from ViewModel
        observeViewModel();
    }

    private void loadAdminData() {
        viewModel.loadAdminData();
    }

    private void setupButtonListeners() {
        btnUpdateProfile.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            
            if (name.isEmpty()) {
                etName.setError("Name cannot be empty");
                return;
            }
            
            if (phone.isEmpty()) {
                etPhone.setError("Phone cannot be empty");
                return;
            }
            
            showProfileLoading(true);
            viewModel.updateAdminProfile(name, phone);
        });
        
        btnUpdatePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();
            
            resetPasswordErrors();
            
            if (currentPassword.isEmpty()) {
                tilCurrentPassword.setError("Please enter current password");
                return;
            }
            
            if (newPassword.isEmpty()) {
                tilNewPassword.setError("Please enter new password");
                return;
            }
            
            if (newPassword.length() < 6) {
                tilNewPassword.setError("Password must be at least 6 characters");
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                tilConfirmPassword.setError("Passwords don't match");
                return;
            }
            
            showPasswordLoading(true);
            viewModel.updatePassword(currentPassword, newPassword);
        });
    }
    
    private void resetPasswordErrors() {
        tilCurrentPassword.setError(null);
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void observeViewModel() {
        viewModel.getAdminData().observe(this, admin -> {
            if (admin != null) {
                etName.setText(admin.getName());
                etEmail.setText(admin.getEmail());
                etPhone.setText(admin.getPhone());
                etEmail.setEnabled(false); // Email cannot be changed
            }
        });
        
        viewModel.getProfileUpdateStatus().observe(this, success -> {
            showProfileLoading(false);
            if (success) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            }
        });
        
        viewModel.getPasswordUpdateStatus().observe(this, success -> {
            showPasswordLoading(false);
            if (success) {
                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                clearPasswordFields();
            }
        });
        
        viewModel.getError().observe(this, error -> {
            showProfileLoading(false);
            showPasswordLoading(false);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }
    
    private void clearPasswordFields() {
        etCurrentPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
    }
    
    private void showProfileLoading(boolean isLoading) {
        progressProfile.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnUpdateProfile.setEnabled(!isLoading);
    }
    
    private void showPasswordLoading(boolean isLoading) {
        progressPassword.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnUpdatePassword.setEnabled(!isLoading);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}