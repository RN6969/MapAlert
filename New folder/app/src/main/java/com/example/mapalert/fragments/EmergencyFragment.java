package com.example.mapalert.fragments;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.mapalert.R;

public class EmergencyFragment extends Fragment {

    private MediaPlayer sosAlarm;
    private Vibrator vibrator;
    private Handler flashHandler = new Handler();

    public EmergencyFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency, container, false);

        TextView alertText = view.findViewById(R.id.emergencyAlertText);
        ImageView flashIcon = view.findViewById(R.id.emergencyFlashIcon);
        Button cancelEmergencyBtn = view.findViewById(R.id.cancelEmergencyBtn);

        sosAlarm = MediaPlayer.create(requireContext(), R.raw.sos_buzzer);
        vibrator = (Vibrator) requireActivity().getSystemService(requireContext().VIBRATOR_SERVICE);

        startEmergencyActions();

        cancelEmergencyBtn.setOnClickListener(v -> stopEmergencyActions());

        return view;
    }

    private void startEmergencyActions() {
        // Start SOS Sound
        if (!sosAlarm.isPlaying()) {
            sosAlarm.start();
        }

        // Start Vibration
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(2000);
        }

        // Start Flashlight
        flashSOS();
    }

    private void flashSOS() {
        CameraManager cameraManager = (CameraManager) requireContext().getSystemService(requireContext().CAMERA_SERVICE);
        try {
            for (int i = 0; i < 5; i++) {
                if (cameraManager != null) {
                    cameraManager.setTorchMode(cameraManager.getCameraIdList()[0], true);
                    Thread.sleep(200);
                    cameraManager.setTorchMode(cameraManager.getCameraIdList()[0], false);
                    Thread.sleep(200);
                }
            }
        } catch (CameraAccessException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stopEmergencyActions() {
        if (sosAlarm.isPlaying()) {
            sosAlarm.stop();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
        boolean isFlashing = false;
        requireActivity().finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sosAlarm != null) {
            sosAlarm.release();
        }
    }

    public Handler getFlashHandler() {
        return flashHandler;
    }

    public void setFlashHandler(Handler flashHandler) {
        this.flashHandler = flashHandler;
    }
}
