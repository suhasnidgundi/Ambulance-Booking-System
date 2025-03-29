package com.svcp.ambulancebookingsystem.data.remote;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.svcp.ambulancebookingsystem.R;
import com.svcp.ambulancebookingsystem.data.model.Booking;
import com.svcp.ambulancebookingsystem.ui.main.MainActivity;

import java.util.HashMap;
import java.util.Map;

public class FCMService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle incoming booking notification
        if (!remoteMessage.getData().isEmpty()) {
            Map<String, String> data = remoteMessage.getData();
            String bookingId = data.get("bookingId");
            String pickupAddress = data.get("pickupAddress");

            // Show notification to driver
            // This would typically use Android's NotificationCompat
        }
    }

    private void showNotification(String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "ambulance_notifications";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Ambulance Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_ambulance)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(0, builder.build());
    }

    public static void sendDriverBookingNotification(Booking booking) {
        // Get the driver's FCM token (You'll need to store/retrieve this when driver logs in)
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    // Prepare notification payload
                    Map<String, String> payload = new HashMap<>();
                    payload.put("bookingId", booking.getBookingId());
                    payload.put("pickupAddress", booking.getPickupAddress());
                    payload.put("patientName", booking.getPatientName());
                    payload.put("patientCondition", booking.getPatientCondition());

                    // Send message to driver's device
                    RemoteMessage message = new RemoteMessage.Builder(token)
                            .setData(payload)
                            .build();

                    FirebaseMessaging.getInstance().send(message);
                });
    }
}
