package com.example.mapalert.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FirebaseHelper {
    private static final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

    public static void sendTrackingNotification(String memberId, String message) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        DatabaseReference notificationsRef = usersRef.child(memberId).child("notifications");

        String notificationId = notificationsRef.push().getKey();
        HashMap<String, Object> notificationData = new HashMap<>();
        notificationData.put("message", message);
        notificationData.put("timestamp", System.currentTimeMillis());

        if (notificationId != null) {
            notificationsRef.child(notificationId).setValue(notificationData);
        }
    }

    public static void updateLocation(double latitude, double longitude) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        DatabaseReference locationRef = usersRef.child(currentUserId).child("location");

        HashMap<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);

        locationRef.setValue(locationData);
    }
}
