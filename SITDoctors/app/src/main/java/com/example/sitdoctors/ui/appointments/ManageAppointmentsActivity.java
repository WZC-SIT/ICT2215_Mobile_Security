package com.example.sitdoctors.ui.appointments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.sitdoctors.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.widget.Toast;


public class ManageAppointmentsActivity extends AppCompatActivity {

    private TextView tvSelectedDate;
    private EditText etReason;
    private String selectedDate = "";
    private DatabaseReference databaseReference; // Firebase Database Reference
    private FirebaseAuth mAuth; // Firebase Authentication

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_appointments);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Appointments");
        }

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("appointments");
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI Elements
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        Button btnPickDate = findViewById(R.id.btn_pick_date);
        etReason = findViewById(R.id.et_reason);
        Button btnSubmit = findViewById(R.id.btn_submit_appointment);

        // Date Picker Logic
        btnPickDate.setOnClickListener(view -> showDatePicker());

        // Submit Button Logic
        btnSubmit.setOnClickListener(view -> submitAppointment());
    }

    private void showDatePicker() {
        // Get today's date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create Date Picker Dialog with minDate set to today + 1
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    tvSelectedDate.setText(selectedDate);
                },
                year, month, day);

        // Prevent past dates from being selected
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis() + 86400000); // Add 1 day (24 hours)

        datePickerDialog.show();
    }

    private void submitAppointment() {
        String reason = etReason.getText().toString().trim();

        // Validate input
        if (selectedDate.isEmpty()) {
            showToast("Please select a date!");
            return;
        }
        if (reason.isEmpty()) {
            showToast("Please enter a reason!");
            return;
        }

        // Get the current user from Firebase Authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showToast("You must be logged in to submit an appointment");
            return;
        }

        // Get user details
        String userId = user.getUid();
        String userEmail = user.getEmail();

        // Reference to "appointments" node in Firebase
        DatabaseReference appointmentRef = FirebaseDatabase
                .getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("appointments")
                .child(userId); // Store under user ID

        // Generate a unique ID for each appointment
        String appointmentId = appointmentRef.push().getKey();
        if (appointmentId == null) {
            showToast("Failed to generate appointment ID!");
            return;
        }

        // Prepare appointment data using HashMap
        HashMap<String, Object> appointmentData = new HashMap<>();
        appointmentData.put("appointmentId", appointmentId);
        appointmentData.put("date", selectedDate);
        appointmentData.put("reason", reason);
        appointmentData.put("status", "Pending");
        appointmentData.put("email", userEmail);
        appointmentData.put("userId", userId);

        // Store appointment in Firebase under the user's node
        appointmentRef.child(appointmentId).setValue(appointmentData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Appointment Submitted Successfully!");
                        finish(); // Navigate back to Home after submission
                    } else {
                        showToast("Failed to submit appointment: " + task.getException().getMessage());
                    }
                });
    }

    // Utility method to show Toast messages
    private void showToast(String message) {
        Toast.makeText(ManageAppointmentsActivity.this, message, Toast.LENGTH_SHORT).show();
    }



    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
