package com.example.sitdoctors;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.scottyab.rootbeer.RootBeer;

public class DoctorMainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    TextView welcomeMessage, userDetails;

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

        // Initialize Bottom Navigation
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_contacts)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Check the user's role (doctor or patient) and hide the contacts option for doctors
        if (user != null) {
            String userRole = getUserRole(user.getUid());  // Method to get the user's role from Firebase

            if ("doctor".equals(userRole)) {
                navView.getMenu().findItem(R.id.navigation_contacts).setVisible(false);
            }
        }
        RootBeer rootBeer = new RootBeer(this);
        if (rootBeer.isRooted()) {
            // Block rooted devices
            Toast.makeText(this, "App cannot run on rooted devices.", Toast.LENGTH_LONG).show();
            finishAffinity(); // or System.exit(0);
        }
    }
    private String getUserRole(String uid) {
        return "doctor";
    }
}