package com.svcp.ambulancebookingsystem.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.databinding.ActivityLoginBinding;
import com.svcp.ambulancebookingsystem.ui.admin.AdminDashboardActivity;
import com.svcp.ambulancebookingsystem.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(task);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check if user is already logged in
        if (authViewModel.isUserLoggedIn()) {
            navigateToAppropriateScreen();
            finish();
            return;
        }

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            authViewModel.loginUser(email, password);
        });

        binding.btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void observeViewModel() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnLogin.setEnabled(false);
                binding.btnGoogleSignIn.setEnabled(false);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                binding.btnGoogleSignIn.setEnabled(true);
            }
        });

        authViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Snackbar.make(binding.getRoot(), errorMessage, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                authViewModel.googleSignIn(account.getIdToken()).observe(this, user -> {
                    if (user != null) {
                        navigateToAppropriateScreen();
                    }
                });
            }
        } catch (ApiException e) {
            Toast.makeText(this, "Google Sign-In failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToAppropriateScreen() {
        boolean isAdmin = getSharedPreferences("ambulance_booking_prefs", MODE_PRIVATE)
                .getBoolean("is_admin", false);

        if (isAdmin) {
            startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
        } else {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
        finish();
    }
}