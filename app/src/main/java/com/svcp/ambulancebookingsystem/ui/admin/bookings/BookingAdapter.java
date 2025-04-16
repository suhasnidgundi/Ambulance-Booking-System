package com.svcp.ambulancebookingsystem.ui.admin.bookings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.data.model.Booking;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingAdapter extends ListAdapter<Booking, BookingAdapter.BookingViewHolder> {

    private final BookingActionListener listener;

    public interface BookingActionListener {
        void onViewDetails(Booking booking);
        void onUpdateStatus(Booking booking, String newStatus);
    }

    protected BookingAdapter(BookingActionListener listener) {
        super(new BookingDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = getItem(position);
        holder.bind(booking, listener);
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvBookingId;
        private final TextView tvUserName;
        private final TextView tvPickupLocation;
        private final TextView tvDropLocation;
        private final TextView tvDateTime;
        private final TextView tvStatus;
        private final Button btnViewDetails;
        private final Button btnUpdateStatus;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvPickupLocation = itemView.findViewById(R.id.tvPickupLocation);
            tvDropLocation = itemView.findViewById(R.id.tvDropLocation);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }

        public void bind(Booking booking, BookingActionListener listener) {
            tvBookingId.setText("Booking ID: " + booking.getId());
            tvUserName.setText("User: " + booking.getUserName());
            tvPickupLocation.setText("Pickup: " + booking.getPickupLocation());
            tvDropLocation.setText("Drop: " + booking.getDropLocation());
            
            // Format date
            Date bookingDate = booking.getBookingTime().toDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            tvDateTime.setText("Date: " + dateFormat.format(bookingDate));
            
            // Set status with color
            tvStatus.setText("Status: " + booking.getStatus());
            
            switch (booking.getStatus().toLowerCase()) {
                case "pending":
                    tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                    btnUpdateStatus.setText("Confirm");
                    btnUpdateStatus.setVisibility(View.VISIBLE);
                    break;
                case "confirmed":
                    tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark));
                    btnUpdateStatus.setText("Complete");
                    btnUpdateStatus.setVisibility(View.VISIBLE);
                    break;
                case "completed":
                    tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                    btnUpdateStatus.setVisibility(View.GONE);
                    break;
                case "cancelled":
                    tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                    btnUpdateStatus.setVisibility(View.GONE);
                    break;
                default:
                    tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                    btnUpdateStatus.setVisibility(View.GONE);
                    break;
            }
            
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(booking);
                }
            });
            
            btnUpdateStatus.setOnClickListener(v -> {
                if (listener != null) {
                    String newStatus;
                    if (booking.getStatus().equalsIgnoreCase("pending")) {
                        newStatus = "confirmed";
                    } else if (booking.getStatus().equalsIgnoreCase("confirmed")) {
                        newStatus = "completed";
                    } else {
                        return;
                    }
                    listener.onUpdateStatus(booking, newStatus);
                }
            });
        }
    }

    static class BookingDiffCallback extends DiffUtil.ItemCallback<Booking> {
        @Override
        public boolean areItemsTheSame(@NonNull Booking oldItem, @NonNull Booking newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Booking oldItem, @NonNull Booking newItem) {
            return oldItem.equals(newItem);
        }
    }
}