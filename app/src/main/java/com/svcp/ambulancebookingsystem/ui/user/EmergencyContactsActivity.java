package com.svcp.ambulancebookingsystem.ui.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.data.model.EmergencyContact;
import com.svcp.ambulancebookingsystem.data.model.User;
import com.svcp.ambulancebookingsystem.databinding.ActivityEmergencyContactsBinding;
import com.svcp.ambulancebookingsystem.ui.user.adapters.EmergencyContactsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EmergencyContactsActivity extends AppCompatActivity {
    private ActivityEmergencyContactsBinding binding;
    private UserViewModel userViewModel;
    private List<EmergencyContact> contactsList = new ArrayList<>();
    private EmergencyContactsAdapter adapter;
    private static final int CALL_PERMISSION_REQUEST_CODE = 123;
    private EmergencyContact selectedContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmergencyContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Emergency Contacts");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        
        setupRecyclerView();
        
        userViewModel.getUserData().observe(this, user -> {
            if (user != null && user.getEmergencyContacts() != null) {
                contactsList = user.getEmergencyContacts();
                updateUI();
            } else {
                contactsList = new ArrayList<>();
                updateUI();
            }
        });

        binding.fabAddContact.setOnClickListener(v -> showAddContactDialog());
    }
    
    private void setupRecyclerView() {
        binding.rvEmergencyContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmergencyContactsAdapter(contactsList,
            new EmergencyContactsAdapter.OnContactClickListener() {
                @Override
                public void onCallClick(EmergencyContact contact) {
                    selectedContact = contact;
                    callEmergencyContact(contact);
                }

                @Override
                public void onDeleteClick(EmergencyContact contact) {
                    showDeleteConfirmationDialog(contact);
                }
            });
        binding.rvEmergencyContacts.setAdapter(adapter);
    }

    private void updateUI() {
        if (contactsList.isEmpty()) {
            binding.tvNoContacts.setVisibility(View.VISIBLE);
            binding.rvEmergencyContacts.setVisibility(View.GONE);
        } else {
            binding.tvNoContacts.setVisibility(View.GONE);
            binding.rvEmergencyContacts.setVisibility(View.VISIBLE);
            adapter.updateContacts(contactsList);
        }
    }

    private void showAddContactDialog() {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_emergency_contact, null);
        builder.setView(dialogView);
        
        EditText etName = dialogView.findViewById(R.id.etContactName);
        EditText etPhone = dialogView.findViewById(R.id.etContactPhone);
        EditText etRelation = dialogView.findViewById(R.id.etContactRelation);
        Button btnAdd = dialogView.findViewById(R.id.btnAddContact);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String phone = etPhone.getText().toString();
            String relation = etRelation.getText().toString();
            
            if (validateInput(name, phone, relation)) {
                addEmergencyContact(name, phone, relation);
                dialog.dismiss();
            }
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }
    
    private void showEditContactDialog(EmergencyContact contact) {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_emergency_contact, null);
        builder.setView(dialogView);
        
        TextView title = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etName = dialogView.findViewById(R.id.etContactName);
        EditText etPhone = dialogView.findViewById(R.id.etContactPhone);
        EditText etRelation = dialogView.findViewById(R.id.etContactRelation);
        Button btnAdd = dialogView.findViewById(R.id.btnAddContact);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        
        title.setText("Edit Contact");
        btnAdd.setText("Update");
        etName.setText(contact.getName());
        etPhone.setText(contact.getPhone());
        etRelation.setText(contact.getRelation());
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String phone = etPhone.getText().toString();
            String relation = etRelation.getText().toString();
            
            if (validateInput(name, phone, relation)) {
                updateEmergencyContact(contact, name, phone, relation);
                dialog.dismiss();
            }
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }
    
    private void showDeleteConfirmationDialog(EmergencyContact contact) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete this contact?")
            .setPositiveButton("Delete", (dialog, which) -> deleteEmergencyContact(contact))
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private boolean validateInput(String name, String phone, String relation) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(relation)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addEmergencyContact(String name, String phone, String relation) {
        String contactId = UUID.randomUUID().toString();
        EmergencyContact newContact = new EmergencyContact(contactId, name, phone, relation);
        
        List<EmergencyContact> updatedContacts = new ArrayList<>(contactsList);
        updatedContacts.add(newContact);
        
        User updatedUser = new User();
        updatedUser.setEmergencyContacts(updatedContacts);

        userViewModel.updateUserEmergencyContacts(updatedContacts);
        Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show();
    }
    
    private void updateEmergencyContact(EmergencyContact contact, String name, String phone, String relation) {
        List<EmergencyContact> updatedContacts = new ArrayList<>();
        
        for (EmergencyContact existingContact : contactsList) {
            if (existingContact.getContactId() != null && existingContact.getContactId().equals(contact.getContactId())) {
                existingContact.setName(name);
                existingContact.setPhone(phone);
                existingContact.setRelation(relation);
            }
            updatedContacts.add(existingContact);
        }
        
        userViewModel.updateUserEmergencyContacts(updatedContacts);
        Toast.makeText(this, "Contact updated successfully", Toast.LENGTH_SHORT).show();
    }
    
    private void deleteEmergencyContact(EmergencyContact contact) {
        List<EmergencyContact> updatedContacts = new ArrayList<>();
        
        for (EmergencyContact existingContact : contactsList) {
            if (existingContact.getContactId() == null || !existingContact.getContactId().equals(contact.getContactId())) {
                updatedContacts.add(existingContact);
            }
        }
        
        userViewModel.updateUserEmergencyContacts(updatedContacts);
        Toast.makeText(this, "Contact deleted successfully", Toast.LENGTH_SHORT).show();
    }
    
    private void callEmergencyContact(EmergencyContact contact) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST_CODE);
        } else {
            initiateCall(contact);
        }
    }
    
    private void initiateCall(EmergencyContact contact) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + contact.getPhone()));
        startActivity(callIntent);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (selectedContact != null) {
                    initiateCall(selectedContact);
                }
            } else {
                Toast.makeText(this, "Permission denied to make phone calls", Toast.LENGTH_SHORT).show();
            }
        }
    }
}