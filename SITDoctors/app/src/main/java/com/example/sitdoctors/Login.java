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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin;
    ProgressBar progressBar;
    TextView textView;

    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d("Login", "User already logged in. Checking role...");
            progressBar.setVisibility(View.VISIBLE);
            checkUserRole(currentUser.getUid());
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.Email);
        editTextPassword = findViewById(R.id.Password);
        buttonLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.registerNow);

        mAuth = FirebaseAuth.getInstance();

        textView.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Register.class));
            finish();
        });

        buttonLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = String.valueOf(editTextEmail.getText()).trim();
            String password = String.valueOf(editTextPassword.getText()).trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Login.this, "Enter Email", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "Enter Password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                progressBar.setVisibility(View.VISIBLE);
                                checkUserRole(user.getUid());
                            }
                        } else {
                            Toast.makeText(Login.this, "Authentication failed: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void checkUserRole(String userId) {
        DatabaseReference userRef = FirebaseDatabase
                .getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(userId);

        Log.d("Login", "Checking user role for userId: " + userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                Log.d("Login", "Snapshot exists: " + snapshot.exists());

                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    Log.d("Login", "User role: " + role);

                    Intent intent;
                    if ("doctor".equalsIgnoreCase(role)) {
                        intent = new Intent(Login.this, DoctorMainActivity.class);
                    } else {
                        intent = new Intent(Login.this, PatientMainActivity.class);
                    }
                    startActivity(intent);
                    finish();
                } else {
                    Log.d("Login", "User role not found in database.");
                    Toast.makeText(Login.this, "User role not found in database.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Log.e("Login", "Database error: " + error.getMessage());
                Toast.makeText(Login.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
