package com.example.mapalert.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mapalert.FullMapActivity;
import com.example.mapalert.R;

public class ZonePreferenceActivity extends AppCompatActivity {

    private Spinner spinnerRisky, spinnerSafe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone_preference);

        // Toolbar Setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Zone Preference");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize UI
        spinnerRisky = findViewById(R.id.spinner_risky);
        spinnerSafe = findViewById(R.id.spinner_safe);

        FrameLayout mapPreview = findViewById(R.id.map_preview_container);
        mapPreview.setOnClickListener(v -> {
            Intent intent = new Intent(ZonePreferenceActivity.this, FullMapActivity.class);
            startActivityForResult(intent, 100);
        });
    }

    // Receive Data from FullMapActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            double lat = data.getDoubleExtra("latitude", 0.0);
            double lng = data.getDoubleExtra("longitude", 0.0);
            double radius = data.getDoubleExtra("radius", 0.0);
            boolean isRisky = data.getBooleanExtra("isRisky", false);

            if (lat != 0.0 && lng != 0.0) {
                // Update dropdowns with selected location
                updateDropdown(lat, lng, radius, isRisky);
            }
        }
    }

    private void updateDropdown(double lat, double lng, double radius, boolean isRisky) {
        // Logic to add selected location to the respective dropdown list
    }
}
