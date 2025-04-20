package com.example.mapalert.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mapalert.R;
import com.example.mapalert.models.Crime;
import com.example.mapalert.network.ApiClient;
import com.example.mapalert.network.CrimeService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private MapView mapView;
    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private boolean isMapReady = false;
    private Marker userMarker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mapView = view.findViewById(R.id.mapView);
        FloatingActionButton fabLocation = view.findViewById(R.id.fab_location);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(callback);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        requestLocationPermission();

        fabLocation.setOnClickListener(v -> {
            if (isGPSOn()) {
                moveToCurrentLocation();
            } else {
                promptEnableGPS();
            }
        });

        return view;
    }

    private final OnMapReadyCallback callback = googleMap -> {
        gMap = googleMap;
        gMap.getUiSettings().setZoomControlsEnabled(false);
        gMap.getUiSettings().setMapToolbarEnabled(false);
        isMapReady = true;

        if (hasLocationPermission()) {
            try {
                gMap.setMyLocationEnabled(false); // Hide default blue dot
                getLastKnownLocation();
            } catch (SecurityException e) {
                Log.e("HomeFragment", "Permission error: " + e.getMessage());
            }
        }
    };

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getLastKnownLocation();
                } else {
                    Toast.makeText(requireContext(), "Location permission denied.", Toast.LENGTH_SHORT).show();
                }
            });

    private void getLastKnownLocation() {
        if (!hasLocationPermission()) return;

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            currentLocation = location;
                            if (isMapReady) {
                                updateUserMarker(location);
                                fetchCrimeData(); // 👈 Fetch crimes here
                            }
                        } else {
                            Log.e("HomeFragment", "Location is null");
                        }
                    });
        } catch (SecurityException e) {
            Log.e("HomeFragment", "SecurityException: " + e.getMessage());
        }
    }

    private void updateUserMarker(Location location) {
        if (gMap == null) return;

        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (userMarker == null) {
            userMarker = gMap.addMarker(new MarkerOptions()
                    .position(userLatLng)
                    .title("You are here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            );
        } else {
            animateMarker(userMarker, userLatLng);
        }

        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
    }

    private void moveToCurrentLocation() {
        if (currentLocation != null && gMap != null) {
            LatLng userLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
        } else {
            Toast.makeText(getContext(), "Current location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isGPSOn() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void promptEnableGPS() {
        Toast.makeText(requireContext(), "Please enable GPS for location tracking.", Toast.LENGTH_LONG).show();
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    private void animateMarker(final Marker marker, final LatLng toPosition) {
        final long duration = 1000;
        final Interpolator interpolator = new LinearInterpolator();

        final LatLng startPosition = marker.getPosition();
        final long startTime = System.currentTimeMillis();

        new android.os.Handler().post(new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lat = (toPosition.latitude - startPosition.latitude) * t + startPosition.latitude;
                double lng = (toPosition.longitude - startPosition.longitude) * t + startPosition.longitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    new android.os.Handler().postDelayed(this, 16);
                }
            }
        });
    }

    private void fetchCrimeData() {
        if (gMap == null) return;

        ApiClient.getClient().create(CrimeService.class).getAllCrimes().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Crime>> call, @NonNull Response<List<Crime>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    gMap.clear(); // clear previous markers
                    updateUserMarker(currentLocation); // re-add user marker

                    for (Crime crime : response.body()) {
                        gMap.addMarker(new MarkerOptions()
                                .position(new LatLng(crime.getLatitude(), crime.getLongitude()))
                                .title(crime.getCrimeType())
                                .snippet(crime.getDescription()));
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch crime data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Crime>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error fetching crime data", Toast.LENGTH_SHORT).show();
                Log.e("CrimeAPI", "Fetch error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (hasLocationPermission()) {
            getLastKnownLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
