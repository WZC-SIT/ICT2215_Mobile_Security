package com.example.sitdoctors.ui.nearbyClinics;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Make sure you import THIS, not android.widget.Toolbar
import com.example.sitdoctors.R;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.core.content.ContextCompat;

import org.osmdroid.views.MapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;


import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class NearbyClinicsActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference databaseRef;
    private MapView mapView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_clinics);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nearby Clinics");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        databaseRef = FirebaseDatabase.getInstance().getReference("locations");

        Configuration.getInstance().setUserAgentValue(getPackageName());

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Center the map on Singapore
        GeoPoint singapore = new GeoPoint(1.3521, 103.8198);
        mapView.getController().setZoom(12.0);
        mapView.getController().setCenter(singapore);

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Ask for both permissions so the user sees the choice
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        boolean fineGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
            Toast.makeText(this, "Enable location sharing to use this feature", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!fineGranted && coarseGranted) {
            Toast.makeText(this, "Enable precise location for better results.", Toast.LENGTH_LONG).show();
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                    return;
                }

                String email = user.getEmail();
                String uid = user.getUid();

                Map<String, Object> userLocationData = new HashMap<>();
                userLocationData.put("email", email != null ? email : "Not provided");
                userLocationData.put("userId", uid);
                userLocationData.put("latitude", lat);
                userLocationData.put("longitude", lng);
                userLocationData.put("timestamp", System.currentTimeMillis());

                // Push data under a new unique ID inside "locations"
                databaseRef.push().setValue(userLocationData)
                        .addOnFailureListener(e -> Toast.makeText(this, "Check your internet connection and allow location sharing again.", Toast.LENGTH_SHORT).show());

                GeoPoint userLocation = new GeoPoint(lat, lng);

                // Step 1: Define the same clinicLocations list inside this method
                List<GeoPoint> clinicLocations = Arrays.asList(
                        new GeoPoint(1.3000, 103.8000), // Kallang
                        new GeoPoint(1.3000, 103.9000), // Parkway
                        new GeoPoint(1.3500, 103.8500), // Pek Kio
                        new GeoPoint(1.4300, 103.7700), // Woodlands
                        new GeoPoint(1.3000, 103.8600), // Beach Rd
                        new GeoPoint(1.3200, 103.9100), // Cereza
                        new GeoPoint(1.3300, 103.8900), // Grantral
                        new GeoPoint(1.2900, 103.8600), // Suntec
                        new GeoPoint(1.3800, 103.7600), // Choa Chu Kang
                        new GeoPoint(1.4300, 103.8300), // Yishun Grove
                        new GeoPoint(1.3400, 103.9600), // Simei
                        new GeoPoint(1.3700, 103.8400), // Ang Mo Kio
                        new GeoPoint(1.4100, 103.8200), // Sembawang
                        new GeoPoint(1.2950, 103.8500), // Tanjong Pagar
                        new GeoPoint(1.3150, 103.7650), // Clementi
                        new GeoPoint(1.3700, 103.7600), // Bukit Panjang
                        new GeoPoint(1.3100, 103.7900), // Bukit Timah
                        new GeoPoint(1.3500, 103.8200), // Toa Payoh
                        new GeoPoint(1.3600, 103.9100), // Hougang
                        new GeoPoint(1.3000, 103.7500)  // West Coast
                );

                // Step 2: Sort clinics by distance to user
                List<GeoPoint> sortedClinics = new ArrayList<>(clinicLocations);
                sortedClinics.sort(Comparator.comparingDouble(c -> calculateDistance(userLocation, c)));

                // Step 3: Pick top 3
                List<GeoPoint> top3 = sortedClinics.subList(0, Math.min(3, sortedClinics.size()));

                // Step 4: Add markers for top 3
                for (int i = 0; i < top3.size(); i++) {
                    GeoPoint point = top3.get(i);
                    double distance = calculateDistance(userLocation, point);

                    Marker marker = new Marker(mapView);
                    marker.setPosition(point);
                    marker.setTitle("Nearest Clinic " + (i + 1));
                    marker.setSubDescription(
                            Math.round(distance) + "m away\nLat: " + point.getLatitude() + "\nLng: " + point.getLongitude()
                    );
                    marker.setImage(null);
                    marker.setIcon(getResources().getDrawable(org.osmdroid.library.R.drawable.marker_default));
                    mapView.getOverlays().add(marker);
                }

                // Step 5: Add the rest of the clinics
                for (GeoPoint point : clinicLocations) {
                    if (!top3.contains(point)) {
                        double distance = calculateDistance(userLocation, point);

                        Marker marker = new Marker(mapView);
                        marker.setPosition(point);
                        marker.setTitle("Clinic Location");
                        marker.setSubDescription(
                                Math.round(distance) + "m away\nLat: " + point.getLatitude() + "\nLng: " + point.getLongitude()
                        );
                        marker.setImage(null);
                        marker.setIcon(getResources().getDrawable(org.osmdroid.library.R.drawable.marker_default));
                        mapView.getOverlays().add(marker);
                    }
                }


                // Step 6: Create data for RecyclerView
                List<Clinic> top3Clinics = new ArrayList<>();
                for (int i = 0; i < top3.size(); i++) {
                    GeoPoint point = top3.get(i);
                    double distance = calculateDistance(userLocation, point);
                    top3Clinics.add(new Clinic("Clinic " + (i + 1), distance, point));
                }

                // Step 7: Setup RecyclerView with click-to-zoom
                RecyclerView recyclerView = findViewById(R.id.topClinicsList);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(new ClinicAdapter(top3Clinics, clinic -> {
                    mapView.getController().setZoom(17.0);
                    mapView.getController().animateTo(clinic.location);
                }));

                mapView.invalidate();


            } else {
                Toast.makeText(this, "Unable to retrieve location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            getCurrentLocation();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private double calculateDistance(GeoPoint a, GeoPoint b) {
        float[] results = new float[1];
        Location.distanceBetween(
                a.getLatitude(), a.getLongitude(),
                b.getLatitude(), b.getLongitude(),
                results
        );
        return results[0]; // distance in meters
    }

    private Drawable resizeDrawable(@DrawableRes int drawableId, int width, int height) {
        Drawable image = ContextCompat.getDrawable(this, drawableId);

        if (image != null) {
            Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
            return new BitmapDrawable(getResources(), scaledBitmap);
        }
        return null;
    }
}