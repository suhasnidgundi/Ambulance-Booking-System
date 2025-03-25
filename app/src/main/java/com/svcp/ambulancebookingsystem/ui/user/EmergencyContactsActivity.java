package com.svcp.ambulancebookingsystem.ui.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.svcp.ambulancebookingsystem.databinding.ActivityEmergencyContactsBinding;
import com.svcp.ambulancebookingsystem.data.model.EmergencyContact;
import com.svcp.ambulancebookingsystem.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsActivity extends AppCompatActivity {
    private ActivityEmergencyContactsBinding binding;
    private UserViewModel userViewModel;
    private List<EmergencyContact> contactsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmergencyContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        userViewModel.getUserData().observe(this, user -> {
            if (user != null) {
                contactsList = user.getEmergencyContacts();
            }
        });

        binding.btnAddContact.setOnClickListener(v -> addEmergencyContact());
    }

    private void addEmergencyContact() {
        String name = binding.etContactName.getText().toString();
        String phone = binding.etContactPhone.getText().toString();
        String relation = binding.etContactRelation.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(relation)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        EmergencyContact newContact = new EmergencyContact(null, name, phone, relation);
        contactsList.add(newContact);

        User updatedUser = new User();
        updatedUser.setEmergencyContacts(contactsList);

        userViewModel.updateUserProfile(updatedUser);
        Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show();
    }
}
