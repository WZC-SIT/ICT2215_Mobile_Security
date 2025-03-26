package com.example.sitdoctors.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.sitdoctors.R;
import com.example.sitdoctors.ui.appointments.AppointmentOverviewActivity;
import com.example.sitdoctors.ui.appointments.DoctorManageAppointmentsActivity;
import com.example.sitdoctors.ui.chat.ChatActivity;
import com.example.sitdoctors.ui.nearbyClinics.NearbyClinicsActivity;
import com.example.sitdoctors.ui.scan.ScanActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class HomeFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference userRef;
    private String userRole = null;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 3;
    private static final int USAGE_ACCESS_PERMISSION_REQUEST_CODE = 4;

    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private Handler stopRecordingHandler = new Handler();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");

        CardView cardAppointments = root.findViewById(R.id.card_appointments);

        // Set the Chat card visibility immediately, before Firebase callback
        CardView cardChat = root.findViewById(R.id.card_chat);
        if (cardChat != null && user != null) {
            // Directly check the user role to show/hide the Chat card
            userRef.child(user.getUid()).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userRole = snapshot.getValue(String.class);

                        // Make the chat card visible for doctors immediately
                        if ("doctor".equals(userRole)) {
                            cardChat.setVisibility(View.VISIBLE);
                            cardChat.setOnClickListener(view -> startActivity(new Intent(getActivity(), ChatActivity.class)));
                        } else {
                            cardChat.setVisibility(View.GONE);
                        }

                        // Now, proceed with the rest of the setup
                        fetchUserRole(cardAppointments, root);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to fetch user role", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Nearby Clinics Card
        CardView findClinicsCard = root.findViewById(R.id.card_find_clinics);
        findClinicsCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), NearbyClinicsActivity.class)));

        return root;
    }

    private void fetchUserRole(CardView cardAppointments, View rootView) {
        if (user != null) {
            userRef.child(user.getUid()).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userRole = snapshot.getValue(String.class);

                        // Appointments click based on role
                        cardAppointments.setOnClickListener(view -> {
                            if ("doctor".equals(userRole)) {
                                startActivity(new Intent(getActivity(), DoctorManageAppointmentsActivity.class));
                            } else if ("patient".equals(userRole)) {
                                startActivity(new Intent(getActivity(), AppointmentOverviewActivity.class));
                            } else {
                                Toast.makeText(getContext(), "User role unknown", Toast.LENGTH_SHORT).show();
                            }
                        });

                        setupEmergencyCall(rootView);
                        setupAIScanCard(rootView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to fetch user role", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupEmergencyCall(View rootView) {
        CardView emergencyCallCard = rootView.findViewById(R.id.card_emergency_call);
        if (emergencyCallCard != null) {
            if ("patient".equals(userRole)) {
                emergencyCallCard.setVisibility(View.VISIBLE);
                emergencyCallCard.setOnClickListener(v -> {
                    startRecordingAudio();
                    Toast.makeText(getContext(), "Hospital has been notified. You will be contacted soon.", Toast.LENGTH_LONG).show();
                });
            } else {
                emergencyCallCard.setVisibility(View.GONE);
            }
        }
    }

    private void setupAIScanCard(View rootView) {
        CardView aiScanCard = rootView.findViewById(R.id.card_ai_scan);
        if (aiScanCard != null) {
            if ("patient".equals(userRole)) {
                aiScanCard.setVisibility(View.VISIBLE);
                aiScanCard.setOnClickListener(v -> {
                    try {
                        // Check if the camera permission is granted
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            // Request permission if not granted
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                        } else {
                            // If permission is already granted, open the ScanActivity
                            startActivity(new Intent(getActivity(), ScanActivity.class));
                        }

                        // Check for Usage Access Permission
                        checkUsageAccessPermission();
                    } catch (Exception e) {
                        Log.e("AI Scan", "Error launching ScanActivity: " + e.getMessage());
                        Toast.makeText(getContext(), "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                aiScanCard.setVisibility(View.GONE);
            }
        }
    }

    private void checkUsageAccessPermission() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getContext().getSystemService(Context.USAGE_STATS_SERVICE);
        long currentTime = System.currentTimeMillis();
        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 3600 * 24, currentTime);

        if (stats != null && !stats.isEmpty()) {
            // Permission already granted, proceed
            Log.d("UsageAccess", "Usage access permission granted.");
        } else {
            // Request permission by opening the settings screen
            Toast.makeText(getContext(), "Please allow Usage Access for AI Scan to function.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivityForResult(intent, USAGE_ACCESS_PERMISSION_REQUEST_CODE);
        }
    }

    private void startRecordingAudio() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }

        mediaRecorder = new MediaRecorder();
        audioFilePath = getContext().getExternalFilesDir(null).getAbsolutePath() + "/emergency_audio.3gp";

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(audioFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Log.d("Emergency", "Audio recording started.");

            File audioFile = new File(audioFilePath);
            if (audioFile.exists()) {
                Log.d("Emergency", "Audio file created: " + audioFile.getAbsolutePath());
            }

            stopRecordingHandler.postDelayed(this::stopRecordingAudio, 600000); // Stop after 10 min
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Emergency", "Audio recording error: " + e.getMessage());
        }
    }

    private void stopRecordingAudio() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            Log.d("Emergency", "Audio recording stopped after 10 minutes.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecordingAudio();
        } else if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, open ScanActivity
            startActivity(new Intent(getActivity(), ScanActivity.class));
        } else {
            Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
