package com.example.mapalert.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mapalert.R;
import com.example.mapalert.activities.EditProfileActivity;
import com.example.mapalert.activities.ZonePreferenceActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView usernameText, emailText;
    private MaterialCardView profileSection, zonePreferenceSection;
    private Switch darkModeSwitch;
    private MaterialButton logoutButton;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI components
        profileImage = view.findViewById(R.id.profile_image);
        ImageView editIcon = view.findViewById(R.id.edit_icon);
        usernameText = view.findViewById(R.id.username_text);
        emailText = view.findViewById(R.id.email_text);
        profileSection = view.findViewById(R.id.profile_section);
        darkModeSwitch = view.findViewById(R.id.dark_mode_switch);
        zonePreferenceSection = view.findViewById(R.id.zone_preference_section);
        logoutButton = view.findViewById(R.id.logout_button);

        // Fetch user details (example, replace with Firebase logic)
        usernameText.setText("JohnDoe"); // Fetch from Firebase
        emailText.setText("johndoe@example.com"); // Fetch from Firebase

        // Open Edit Profile Activity when profile section or edit icon is clicked
        View.OnClickListener editProfileListener = v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        };
        profileSection.setOnClickListener(editProfileListener);
        editIcon.setOnClickListener(editProfileListener);

        // Open Zone Preference Activity
        zonePreferenceSection.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ZonePreferenceActivity.class);
            startActivity(intent);
        });

        // Logout Functionality
        logoutButton.setOnClickListener(v -> {
            // Implement Firebase logout logic
            // FirebaseAuth.getInstance().signOut();
            getActivity().finish(); // Close activity after logout
        });

        return view;
    }
}
