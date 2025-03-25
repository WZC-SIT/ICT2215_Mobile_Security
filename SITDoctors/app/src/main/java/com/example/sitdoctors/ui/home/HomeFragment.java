package com.example.sitdoctors.ui.home;

import android.Manifest;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.content.pm.PackageManager;
import java.io.File;
import java.io.IOException;

public class HomeFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference userRef;
    private String userRole = null;

    // Define a request code for permission request
    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 2;

    private MediaRecorder mediaRecorder;
    private String audioFilePath;

    // Handler to stop the recording after 10 minutes (600,000 ms)
    private Handler stopRecordingHandler = new Handler();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        userRef = FirebaseDatabase.getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");

        // Find the Appointments CardView
        CardView cardAppointments = root.findViewById(R.id.card_appointments);
        fetchUserRole(cardAppointments, root);

        // Find the card and set click listener for Nearby Clinics
        CardView findClinicsCard = root.findViewById(R.id.card_find_clinics);
        findClinicsCard.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NearbyClinicsActivity.class);
            startActivity(intent);
        });

        // Chat CardView
        CardView cardChat = root.findViewById(R.id.card_chat);

        // Set visibility for the chat card based on user role
        if (cardChat != null) {
            if ("doctor".equals(userRole)) {
                cardChat.setVisibility(View.VISIBLE);

                cardChat.setOnClickListener(view -> {
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    startActivity(intent);
                });
            } else {
                cardChat.setVisibility(View.GONE);
            }
        }

        // Emergency Call CardView
        CardView emergencyCallCard = root.findViewById(R.id.card_emergency_call);

        if (emergencyCallCard != null) {
            // Only show Emergency Call card for patients
            if ("patient".equals(userRole)) {
                emergencyCallCard.setVisibility(View.VISIBLE);

                emergencyCallCard.setOnClickListener(v -> {
                    // Start audio recording
                    startRecordingAudio();

                    // Log to Logcat that the emergency button was triggered
                    Log.d("Emergency", "Emergency button triggered. Audio recording started.");

                    // Show a Toast to inform the user that their emergency message has been sent
                    Toast.makeText(getContext(), "Hospital has been notified. You will be contacted soon.", Toast.LENGTH_LONG).show();
                });
            } else {
                emergencyCallCard.setVisibility(View.GONE);
            }
        }

        return root;
    }

    private void fetchUserRole(CardView cardAppointments, View rootView) {
        if (user != null) {
            userRef.child(user.getUid()).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userRole = snapshot.getValue(String.class);

                        // Set up Appointments Card click listener based on role
                        cardAppointments.setOnClickListener(view -> {
                            if ("doctor".equals(userRole)) {
                                Intent intent = new Intent(getActivity(), DoctorManageAppointmentsActivity.class);
                                startActivity(intent);
                            } else if ("patient".equals(userRole)) {
                                Intent intent = new Intent(getActivity(), AppointmentOverviewActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getContext(), "User role unknown", Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Set the visibility of the Emergency Call Card based on user role
                        CardView emergencyCallCard = rootView.findViewById(R.id.card_emergency_call);
                        if (emergencyCallCard != null) {
                            if ("patient".equals(userRole)) {
                                emergencyCallCard.setVisibility(View.VISIBLE);
                                emergencyCallCard.setOnClickListener(v -> {
                                    // Start recording audio
                                    startRecordingAudio();
                                    Toast.makeText(getContext(), "Hospital has been notified. You will be contacted soon.", Toast.LENGTH_LONG).show();
                                });
                            } else {
                                emergencyCallCard.setVisibility(View.GONE);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to fetch user role", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Start recording audio in the background
    private void startRecordingAudio() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Request audio permission if not granted
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }

        // Initialize MediaRecorder
        mediaRecorder = new MediaRecorder();
        audioFilePath = getContext().getExternalFilesDir(null).getAbsolutePath() + "/emergency_audio.3gp";

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(audioFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start(); // Start recording
            Log.d("Emergency", "Audio recording started.");

            // Check if the file was created successfully
            File audioFile = new File(audioFilePath);
            if (audioFile.exists()) {
                Log.d("Emergency", "Audio file created successfully at: " + audioFile.getAbsolutePath());
            } else {
                Log.e("Emergency", "Audio file not created.");
            }

            // Schedule stopping the recording after 10 minutes (600,000 ms)
            stopRecordingHandler.postDelayed(this::stopRecordingAudio, 600000);  // 600,000 ms = 10 minutes

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Emergency", "Error starting audio recording: " + e.getMessage());
        }
    }

    // Stop the audio recording
    private void stopRecordingAudio() {
        if (mediaRecorder != null) {
            mediaRecorder.stop(); // Stop the recording
            mediaRecorder.release(); // Release the MediaRecorder resources
            mediaRecorder = null; // Nullify the recorder
            Log.d("Emergency", "Audio recording stopped after 10 minutes.");
        }
    }

    // Handle the result of permission request (for RECORD_AUDIO permission)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start recording audio
                startRecordingAudio();
            } else {
                Toast.makeText(getContext(), "Permission denied to record audio", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
