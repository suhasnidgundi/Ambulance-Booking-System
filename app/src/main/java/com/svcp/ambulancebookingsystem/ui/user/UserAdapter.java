package com.svcp.ambulancebookingsystem.ui.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.data.model.User;

public class UserAdapter extends ListAdapter<User, UserAdapter.UserViewHolder> {

    protected UserAdapter() {
        super(new DiffUtil.ItemCallback<User>() {
            @Override
            public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivUserProfile;
        private final Button btnViewDetails;
        private final Switch switchUserStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserProfile = itemView.findViewById(R.id.ivUserProfile);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            switchUserStatus = itemView.findViewById(R.id.switchUserStatus);
        }

        public void bind(User user) {
            switchUserStatus.setChecked(user.isActive());
            switchUserStatus.setText(user.isActive() ? "Active" : "Inactive");

            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(user.getProfileImageUrl())
                        .into(ivUserProfile);
            }
        }
    }
}