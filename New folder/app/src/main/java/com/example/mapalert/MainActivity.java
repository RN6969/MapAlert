package com.example.mapalert;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mapalert.fragments.HomeFragment;
import com.example.mapalert.fragments.ProfileFragment;
import com.example.mapalert.fragments.ReportFragment;
import com.example.mapalert.fragments.SearchFragment;
import com.example.mapalert.fragments.TrackFragment;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.libraries.places.api.Places;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ Initialize Firebase
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();

        Log.d("FirebaseAuth", "FirebaseAuth instance initialized successfully.");

        // ✅ Initialize Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key)); // <-- 🔁 Replace with your API key
            Log.d("Places", "Places API initialized.");
        }

        // ✅ Load HomeFragment by default
        loadFragment(new HomeFragment());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            } else if (item.getItemId() == R.id.nav_report) {
                if (isUserLoggedIn()) {
                    selectedFragment = new ReportFragment();
                } else {
                    showLoginToast();
                    return false;
                }
            } else if (item.getItemId() == R.id.nav_track) {
                if (isUserLoggedIn()) {
                    selectedFragment = new TrackFragment();
                } else {
                    showLoginToast();
                    return false;
                }
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });

        checkUserStatus();
    }

    private void loadFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private boolean isUserLoggedIn() {
        if (firebaseAuth == null) {
            Log.e("FirebaseAuth", "FirebaseAuth instance is null! Cannot check login status.");
            return false;
        }
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null;
    }

    private void checkUserStatus() {
        if (firebaseAuth == null) {
            Log.e("FirebaseAuth", "FirebaseAuth instance is null!");
            return;
        }
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Log.e("FirebaseAuth", "User is NOT logged in.");
        } else {
            Log.d("FirebaseAuth", "User logged in: " + user.getEmail());
        }
    }

    private void showLoginToast() {
        Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
    }
}
