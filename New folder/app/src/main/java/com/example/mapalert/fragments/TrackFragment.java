package com.example.mapalert.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.mapalert.NotificationActivity;
import com.example.mapalert.dialogs.AddMemberDialog;
import com.example.mapalert.databinding.FragmentTrackBinding;

public class TrackFragment extends Fragment {

    private FragmentTrackBinding binding;
    private int clickCount = 0;
    private final Handler clickHandler = new Handler(Looper.getMainLooper());
    private static final long CLICK_TIMEOUT = 800;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrackBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ Add Member Button Click
        binding.addMemberIcon.setOnClickListener(v -> {
            AddMemberDialog dialog = new AddMemberDialog();
            dialog.show(getParentFragmentManager(), "AddMemberDialog");
        });

        // ✅ Notifications Button Click
        binding.notificationsIcon.setOnClickListener(v -> startActivity(new Intent(getContext(), NotificationActivity.class)));

        // ✅ Start Sharing Button Click
        binding.startSharingBtn.setOnClickListener(v -> {
            clickCount++;
            clickHandler.postDelayed(() -> {
                if (clickCount == 3) {
                    activateSOS();
                } else if (clickCount >= 4) {
                    triggerEmergencyProtocol();
                }
                clickCount = 0;
            }, CLICK_TIMEOUT);
        });
    }

    private void activateSOS() {
        Toast.makeText(requireContext(), "SOS Activated!", Toast.LENGTH_SHORT).show();
        Log.d("TrackFragment", "SOS Alarm and Flashlight should start here.");
    }

    private void triggerEmergencyProtocol() {
        Toast.makeText(requireContext(), "Emergency Protocol Triggered!", Toast.LENGTH_SHORT).show();
        Log.d("TrackFragment", "Emergency call logic should be implemented here.");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
