package com.svcp.ambulancebookingsystem.ui.admin.users;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.data.model.User;

public class ManageUsersActivity extends AppCompatActivity implements UserAdapter.UserActionListener {

    private ManageUsersViewModel viewModel;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private FloatingActionButton fabAddUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Manage Users");

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewUsers);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        fabAddUser = findViewById(R.id.fabAddUser);

        // Set up ViewModel
        viewModel = new ViewModelProvider(this).get(ManageUsersViewModel.class);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(this);
        recyclerView.setAdapter(adapter);

        // Set up FAB
        fabAddUser.setOnClickListener(v -> {
            // TODO: Implement add user functionality
            Toast.makeText(this, "Add User functionality coming soon", Toast.LENGTH_SHORT).show();
        });

        // Load users
        loadUsers();
    }

    private void loadUsers() {
        showLoading(true);
        viewModel.getAllUsers();
        
        viewModel.getUsersLiveData().observe(this, users -> {
            showLoading(false);
            if (users != null && !users.isEmpty()) {
                adapter.submitList(users);
                showEmptyView(false);
            } else {
                showEmptyView(true);
            }
        });
        
        viewModel.getErrorLiveData().observe(this, error -> {
            showLoading(false);
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showEmptyView(boolean isEmpty) {
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onViewDetails(User user) {
        // TODO: Implement view details functionality
        Toast.makeText(this, "View details for " + user.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(User user, boolean isActive) {
        viewModel.updateUserStatus(user.getId(), isActive);
        Toast.makeText(this, "Status updated for " + user.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteUser(User user) {
        // Show confirmation dialog before deleting
        viewModel.deleteUser(user.getId());
        Toast.makeText(this, "User deleted: " + user.getName(), Toast.LENGTH_SHORT).show();
    }
}