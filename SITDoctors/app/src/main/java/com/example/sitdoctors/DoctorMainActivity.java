package com.example.sitdoctors;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DoctorMainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    TextView welcomeMessage, userDetails;
    Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_main);

        // Initialize Firebase Auth and check the current user
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Redirect to Login if no user is logged in
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        // Initialize UI components
        welcomeMessage = findViewById(R.id.welcome_message);
        userDetails = findViewById(R.id.user_details);
        logoutButton = findViewById(R.id.logout);

        // Set the welcome message
        welcomeMessage.setText("Welcome, Doctor!");

        // Set the email of the current user in the TextView
        if (user != null && user.getEmail() != null) {
            userDetails.setText("Logged in as: " + user.getEmail());
        }

        // Set up logout button functionality
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log out the user
                auth.signOut();
                // Redirect to the Login activity
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        // Initialize Bottom Navigation
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }
}
