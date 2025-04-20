package com.example.mapalert.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.mapalert.R;
import com.example.mapalert.models.CrimeReportResponse;
import com.example.mapalert.network.ApiClient;
import com.example.mapalert.network.CrimeService;
import com.example.mapalert.viewmodels.CrimeSharedViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private EditText searchBar;
    private CrimeService crimeService;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationClient;
    private CrimeSharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchBar = view.findViewById(R.id.searchBar);
        crimeService = ApiClient.getClient().create(CrimeService.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(CrimeSharedViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key), Locale.getDefault());
        }
        placesClient = Places.createClient(requireContext());

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                handleSearch(searchBar.getText().toString().trim());
                return true;
            }
            return false;
        });

        return view;
    }

    private void handleSearch(String query) {
        if (TextUtils.isEmpty(query)) {
            Toast.makeText(requireContext(), "Please enter something to search", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] tokens = query.split("\\s+");
        String crime = null, locationQuery = null;

        if (tokens.length == 1) {
            // Could be crime or location, let's try crime
            crime = tokens[0];
        } else {
            // Assume last token(s) are location
            locationQuery = tokens[tokens.length - 1];
            crime = query.replace(locationQuery, "").trim();
        }

        if (locationQuery != null) {
            findLocationCoordinates(locationQuery, crime);
        } else {
            getCurrentLocationAndSearch(crime);
        }
    }

    private void findLocationCoordinates(String locationQuery, @Nullable String crimeType) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(locationQuery)
                .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    if (!response.getAutocompletePredictions().isEmpty()) {
                        AutocompletePrediction prediction = response.getAutocompletePredictions().get(0);
                        fetchLatLngFromPlaceId(prediction.getPlaceId(), crimeType);
                    } else {
                        Toast.makeText(requireContext(), "No such location found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error resolving location", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchLatLngFromPlaceId(String placeId, @Nullable String crimeType) {
        com.google.android.libraries.places.api.net.FetchPlaceRequest request =
                com.google.android.libraries.places.api.net.FetchPlaceRequest.builder(
                        placeId,
                        List.of(com.google.android.libraries.places.api.model.Place.Field.LAT_LNG)
                ).build();

        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    com.google.android.libraries.places.api.model.Place place = response.getPlace();
                    if (place.getLatLng() != null) {
                        double lat = place.getLatLng().latitude;
                        double lng = place.getLatLng().longitude;
                        fetchCrimes(lat, lng, crimeType);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to get coordinates", Toast.LENGTH_SHORT).show();
                });
    }

    private void getCurrentLocationAndSearch(String crimeType) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        fetchCrimes(location.getLatitude(), location.getLongitude(), crimeType);
                    } else {
                        Toast.makeText(requireContext(), "Could not get current location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchCrimes(double latitude, double longitude, @Nullable String crimeType) {
        Call<List<CrimeReportResponse>> call = crimeService.getNearbyCrimes(latitude, longitude, 50, crimeType);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<CrimeReportResponse>> call, @NonNull Response<List<CrimeReportResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sharedViewModel.setCrimeResults(response.body());
                    Navigation.findNavController(requireView()).navigate(R.id.action_searchFragment_to_homeFragment);
                } else {
                    Toast.makeText(requireContext(), "No crimes found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CrimeReportResponse>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
