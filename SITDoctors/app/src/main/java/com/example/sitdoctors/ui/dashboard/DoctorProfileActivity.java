package com.example.sitdoctors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class DoctorProfileActivity extends AppCompatActivity {

    private TextView nameTextView, ageTextView, emailTextView, phoneTextView, addressTextView;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);

        // Link UI
        nameTextView = findViewById(R.id.profileNameTextView);
        ageTextView = findViewById(R.id.profileAgeTextView);
        emailTextView = findViewById(R.id.profileEmailTextView);
        phoneTextView = findViewById(R.id.profilePhoneTextView);
        addressTextView = findViewById(R.id.profileAddressTextView);

        // Doctor email from Intent
        String doctorEmail = getIntent().getStringExtra("doctorEmail");

        usersRef = FirebaseDatabase.getInstance(
                        "https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");

        // Load the details
        loadDoctorDetails(doctorEmail);
    }

    private void loadDoctorDetails(String email) {
        Query query = usersRef.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(DoctorProfileActivity.this,
                            "No doctor found with email: " + email,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot ds : snapshot.getChildren()) {
                    UserProfile doctor = ds.getValue(UserProfile.class);
                    if (doctor != null) {
                        nameTextView.setText(doctor.getName());
                        ageTextView.setText(doctor.getAge());
                        emailTextView.setText(doctor.getEmail());
                        phoneTextView.setText(doctor.getPhone());
                        addressTextView.setText(doctor.getAddress());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DoctorProfileActivity.this,
                        "Failed to load details: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
