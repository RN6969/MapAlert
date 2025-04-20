package com.example.mapalert.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapalert.R; // <-- Make sure this is correctly imported
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.*;

public class LiveTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker memberMarker;
    private DatabaseReference locationRef;
    private String memberId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_tracking); // <-- Ensure this XML file exists

        memberId = getIntent().getStringExtra("memberId");
        if (memberId == null) {
            Log.e("LiveTrackingActivity", "Member ID is null");
            finish(); // Close activity if no member ID is passed
            return;
        }

        locationRef = FirebaseDatabase.getInstance().getReference("users")
                .child(memberId).child("location");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map); // <-- Ensure 'map' ID exists in XML
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        startTracking();
    }

    private void startTracking() {
        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.e("LiveTrackingActivity", "Location data not found for: " + memberId);
                    return;
                }

                Double lat = snapshot.child("latitude").getValue(Double.class);
                Double lng = snapshot.child("longitude").getValue(Double.class);

                if (lat == null || lng == null) {
                    Log.e("LiveTrackingActivity", "Invalid location data");
                    return;
                }

                LatLng location = new LatLng(lat, lng);

                if (mMap != null) {
                    if (memberMarker != null) memberMarker.remove();
                    memberMarker = mMap.addMarker(new MarkerOptions().position(location).title("Live Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LiveTrackingActivity", "Database Error: " + error.getMessage());
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }
}
