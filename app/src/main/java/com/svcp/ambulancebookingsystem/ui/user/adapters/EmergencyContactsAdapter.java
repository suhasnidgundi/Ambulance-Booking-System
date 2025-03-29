package com.svcp.ambulancebookingsystem.ui.user.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.data.model.EmergencyContact;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsAdapter extends RecyclerView.Adapter<EmergencyContactsAdapter.ContactViewHolder> {
    private List<EmergencyContact> contacts;
    private OnContactClickListener listener;

    public interface OnContactClickListener {
        void onCallClick(EmergencyContact contact);
        void onDeleteClick(EmergencyContact contact);
    }

    public EmergencyContactsAdapter(List<EmergencyContact> contactsList, OnContactClickListener listener) {
        this.contacts = new ArrayList<>();
        this.listener = listener;
    }

    public void updateContacts(List<EmergencyContact> newContacts) {
        this.contacts.clear();
        if (newContacts != null) {
            this.contacts.addAll(newContacts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emergency_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        EmergencyContact contact = contacts.get(position);
        holder.tvContactName.setText(contact.getName());
        holder.tvContactPhone.setText(contact.getPhone());
        holder.tvContactRelation.setText(contact.getRelation());

        holder.btnCall.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCallClick(contact);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactName;
        TextView tvContactPhone;
        TextView tvContactRelation;
        ImageButton btnCall;
        ImageButton btnDelete;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvContactPhone = itemView.findViewById(R.id.tvContactPhone);
            tvContactRelation = itemView.findViewById(R.id.tvContactRelation);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}