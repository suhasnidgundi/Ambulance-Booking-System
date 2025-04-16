package com.svcp.ambulancebookingsystem.ui.admin.users;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.data.model.User;

public class UserAdapter extends ListAdapter<User, UserAdapter.UserViewHolder> {
    
    private final UserActionListener listener;

    public interface UserActionListener {
        void onViewDetails(User user);
        void onStatusChanged(User user, boolean isActive);
        void onDeleteUser(User user);
    }

    protected UserAdapter(UserActionListener listener) {
        super(new UserDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = getItem(position);
        holder.bind(user, listener);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivUserProfile;
        private final TextView tvUserName;
        private final TextView tvUserEmail;
        private final TextView tvUserPhone;
        private final SwitchMaterial switchUserStatus;
        private final MaterialButton btnViewDetails;
        private final MaterialButton btnDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserProfile = itemView.findViewById(R.id.ivUserProfile);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserPhone = itemView.findViewById(R.id.tvUserPhone);
            switchUserStatus = itemView.findViewById(R.id.switchUserStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }

        public void bind(User user, UserActionListener listener) {
            // Set user data
            tvUserName.setText(user.getName());
            tvUserEmail.setText(user.getEmail());
            tvUserPhone.setText(user.getPhone());
            
            // Set user status
            switchUserStatus.setChecked(user.isActive());
            switchUserStatus.setText(user.isActive() ? "Active" : "Inactive");
            
            // Load profile image if available
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(ivUserProfile.getContext())
                        .load(user.getProfileImageUrl())
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .circleCrop()
                        .into(ivUserProfile);
            } else {
                ivUserProfile.setImageResource(R.drawable.ic_profile);
            }
            
            // Set click listeners
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(user);
                }
            });
            
            btnDeleteUser.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteUser(user);
                }
            });
            
            switchUserStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onStatusChanged(user, isChecked);
                }
                // Update the switch text based on the new status
                switchUserStatus.setText(isChecked ? "Active" : "Inactive");
            });
        }
    }

    static class UserDiffCallback extends DiffUtil.ItemCallback<User> {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.equals(newItem);
        }
    }
}