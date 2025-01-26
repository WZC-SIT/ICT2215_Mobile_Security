package com.example.sitdoctors;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    // UI Components
    private TextInputEditText editTextEmail, editTextPassword, editTextName, editTextAge, editTextPhone, editTextAddress;
    private Button buttonReg;
    private ProgressBar progressBar;
    private TextView textView;

    // Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Views
        editTextEmail = findViewById(R.id.Email);
        editTextPassword = findViewById(R.id.Password);
        editTextName = findViewById(R.id.Name);
        editTextAge = findViewById(R.id.Age);
        editTextPhone = findViewById(R.id.Phone);
        editTextAddress = findViewById(R.id.Address);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Redirect to Login page
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        // Test Firebase Database Connection
        DatabaseReference testRef = FirebaseDatabase
                .getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("test");
        testRef.setValue("Test data").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Register", "Test write to database successful");
            } else {
                Log.e("Register", "Test write failed", task.getException());
            }
        });

        // Registration Logic
        buttonReg.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String name = editTextName.getText().toString().trim();
            String age = editTextAge.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();
            String address = editTextAddress.getText().toString().trim();

            // Validate Input Fields
            if (validateInputs(email, password, name, age, phone, address)) {
                registerUser(email, password, name, age, phone, address);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // Validate Inputs
    private boolean validateInputs(String email, String password, String name, String age, String phone, String address) {
        if (TextUtils.isEmpty(email)) {
            showToast("Enter Email");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            showToast("Enter Password");
            return false;
        }
        if (password.length() < 6) {
            showToast("Password must be at least 6 characters");
            return false;
        }
        if (TextUtils.isEmpty(name)) {
            showToast("Enter Name");
            return false;
        }
        if (TextUtils.isEmpty(age)) {
            showToast("Enter Age");
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            showToast("Enter Phone Number");
            return false;
        }
        if (TextUtils.isEmpty(address)) {
            showToast("Enter Address");
            return false;
        }
        return true;
    }

    // Register User with Firebase
    private void registerUser(String email, String password, String name, String age, String phone, String address) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        String role = email.trim().endsWith("@hospital.com") ? "doctor" : "patient"; // Determine the role

                        saveUserDetails(userId, name, age, phone, address, email, role);

                        showToast("Account Created Successfully!");

                        // Redirect based on role
                        Intent intent;
                        if ("doctor".equalsIgnoreCase(role)) {
                            intent = new Intent(getApplicationContext(), DoctorMainActivity.class);
                        } else {
                            intent = new Intent(getApplicationContext(), PatientMainActivity.class);
                        }

                        startActivity(intent);
                        finish();
                    } else {
                        showToast("Registration Failed: " + task.getException().getMessage());
                        Log.e("Register", "Error: ", task.getException());
                    }
                });
    }


    // Save User Details in Firebase Realtime Database
    private void saveUserDetails(String userId, String name, String age, String phone, String address, String email, String role) {
        DatabaseReference userRef = FirebaseDatabase
                .getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(userId);

        HashMap<String, Object> userDetails = new HashMap<>();
        userDetails.put("name", name);
        userDetails.put("age", age);
        userDetails.put("phone", phone);
        userDetails.put("address", address);
        userDetails.put("email", email.trim());
        userDetails.put("role", role); // Save the determined role

        userRef.setValue(userDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Register", "User details successfully written to database");
            } else {
                Log.e("Register", "Failed to write user details: ", task.getException());
            }
        });
    }



    // Utility method to show Toast messages
    private void showToast(String message) {
        Toast.makeText(Register.this, message, Toast.LENGTH_SHORT).show();
    }
}
