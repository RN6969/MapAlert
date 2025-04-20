package com.example.mapalert;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public class FullMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Circle zoneCircle;
    private boolean isRisky = true;
    private double radius = 500.0; // Default radius (meters)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.full_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        findViewById(R.id.btn_toggle_zone).setOnClickListener(v -> toggleZone());
        findViewById(R.id.btn_remove_zone).setOnClickListener(v -> removeZone());
        findViewById(R.id.btn_save_zone).setOnClickListener(v -> saveZone());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng defaultLocation = new LatLng(-34, 151);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 14));

        zoneCircle = mMap.addCircle(new CircleOptions()
                .center(defaultLocation)
                .radius(radius)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(50, 255, 0, 0))
                .clickable(true));

        mMap.setOnMapClickListener(this::moveZone);
    }

    private void moveZone(LatLng latLng) {
        zoneCircle.setCenter(latLng);
    }

    private void toggleZone() {
        isRisky = !isRisky;
        zoneCircle.setStrokeColor(isRisky ? Color.RED : Color.GREEN);
    }

    private void removeZone() {
        zoneCircle.remove();
    }

    private void saveZone() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitude", zoneCircle.getCenter().latitude);
        resultIntent.putExtra("longitude", zoneCircle.getCenter().longitude);
        resultIntent.putExtra("isRisky", isRisky);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
