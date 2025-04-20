package com.example.mapalert.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mapalert.R;
import com.example.mapalert.activities.SelectLocationActivity;
import com.example.mapalert.models.CrimeReportRequest;
import com.example.mapalert.models.ReportResponse;
import com.example.mapalert.network.ApiClient;
import com.example.mapalert.network.CrimeService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportFragment extends Fragment {

    private MaterialAutoCompleteTextView crimeTypeDropdown;
    private TextInputEditText inputCrimeDescription, inputDate, inputLocation;
    private Calendar calendar;
    private CrimeService crimeService;

    public ReportFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        // Initialize UI elements
        crimeTypeDropdown = view.findViewById(R.id.crimeTypeDropdown);
        inputCrimeDescription = view.findViewById(R.id.input_crime_description);
        inputDate = view.findViewById(R.id.input_date);
        inputLocation = view.findViewById(R.id.input_location);
        MaterialButton buttonSubmit = view.findViewById(R.id.button_submit);

        // Initialize API service
        crimeService = ApiClient.getClient().create(CrimeService.class);

        // Setup UI components
        setupCrimeTypeDropdown();
        setupDatePicker();
        inputLocation.setOnClickListener(v -> openLocationPicker());

        // Submit button action
        buttonSubmit.setOnClickListener(v -> submitReport());

        return view;
    }

    private void setupCrimeTypeDropdown() {
        String[] crimeTypes = {"Theft", "Assault", "Vandalism", "Fraud", "Burglary"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, crimeTypes);
        crimeTypeDropdown.setAdapter(adapter);
    }

    private void setupDatePicker() {
        calendar = Calendar.getInstance();
        inputDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (DatePicker view, int year, int month, int dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        inputDate.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void openLocationPicker() {
        Intent intent = new Intent(requireContext(), SelectLocationActivity.class);
        locationPickerLauncher.launch(intent);
    }

    @SuppressLint("SetTextI18n")
    private final ActivityResultLauncher<Intent> locationPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();
                            String latitude = data.getStringExtra("latitude");
                            String longitude = data.getStringExtra("longitude");

                            if (latitude != null && longitude != null) {
                                inputLocation.setText(latitude + ", " + longitude);
                            } else {
                                Toast.makeText(requireContext(), "Failed to retrieve location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    private void submitReport() {
        String crimeType = crimeTypeDropdown.getText().toString();
        String description = Objects.requireNonNull(inputCrimeDescription.getText()).toString();
        String rawDate = Objects.requireNonNull(inputDate.getText()).toString();
        String location = Objects.requireNonNull(inputLocation.getText()).toString();

        if (crimeType.isEmpty() || description.isEmpty() || rawDate.isEmpty() || location.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fix Date Format: Convert "16/03/2025" to "2025-03-16"
        String[] dateParts = rawDate.split("/");
        if (dateParts.length != 3) {
            Toast.makeText(requireContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }
        String formattedDate = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];

        // Extract Latitude & Longitude
        String[] coordinates = location.split(",");
        if (coordinates.length != 2) {
            Toast.makeText(requireContext(), "Invalid location format", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude, longitude;
        try {
            latitude = Double.parseDouble(coordinates[0].trim());
            longitude = Double.parseDouble(coordinates[1].trim());
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid location coordinates", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch Username from Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("FirebaseAuth", "getCurrentUser() is NULL!");
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use email instead of display name
        String username = user.getEmail();
        if (username == null || username.isEmpty()) {
            Toast.makeText(requireContext(), "User email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the request object
        CrimeReportRequest request = new CrimeReportRequest(username, crimeType, description, formattedDate, latitude, longitude);

        // Make API Call
        crimeService.reportCrime(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ReportResponse> call, @NonNull Response<ReportResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Crime reported successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("RetrofitError", "Response Code: " + response.code());
                    Log.e("RetrofitError", "Response Message: " + response.message());
                    try {
                        assert response.errorBody() != null;
                        Log.e("RetrofitError", "Error Body: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e("RetrofitError", "Exception: " + e.getMessage());
                    }
                    Toast.makeText(requireContext(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReportResponse> call, @NonNull Throwable t) {
                Log.e("RetrofitError", "Failure: " + t.getMessage());
                Toast.makeText(requireContext(), "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("FirebaseAuth", "User not logged in!");
            Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("FirebaseAuth", "User is logged in: " + user.getEmail());
        }
    }
}
