package com.example.sitdoctors.ui.scan;

import com.example.sitdoctors.R;
import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.List;

public class ScanActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int USAGE_PERMISSION_REQUEST_CODE = 101;
    private static final String TAG = "ScanActivity";

    private ImageView imageView;
    private Bitmap capturedImage;
    private Button backToHomeButton, retakeButton, okButton;

    private Handler usageMonitorHandler;
    private Runnable usageMonitorRunnable;
    private static final long MONITOR_INTERVAL = 10 * 1000; // 10 seconds

    private ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    capturedImage = (Bitmap) result.getData().getExtras().get("data");
                    if (capturedImage != null) {
                        imageView.setImageBitmap(capturedImage);
                        Log.d(TAG, "Image captured successfully.");
                    } else {
                        Log.e(TAG, "Captured image is null.");
                    }
                } else {
                    Log.e(TAG, "Camera activity result is not OK or data is null.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.imageView);
        backToHomeButton = findViewById(R.id.btn_ok);
        retakeButton = findViewById(R.id.btn_retake);
        okButton = findViewById(R.id.btn_ok);

        retakeButton.setVisibility(Button.VISIBLE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!hasUsageStatsPermission()) {
                requestUsageStatsPermission();
            }
        }

        backToHomeButton.setOnClickListener(v -> {
            Log.d(TAG, "Back to home button clicked, finishing activity.");
            finish();
        });

        retakeButton.setOnClickListener(v -> {
            Log.d(TAG, "Retake button clicked, restarting camera.");
            startCamera();
        });

        okButton.setOnClickListener(v -> {
            Toast.makeText(this, "Processing complete. You’ll receive an email with your scan report in 24 hours.", Toast.LENGTH_LONG).show();
            startMonitoringUsageInBackground();
            finish();
        });
    }

    private void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    private void startMonitoringUsageInBackground() {
        if (usageMonitorHandler == null) {
            usageMonitorHandler = new Handler(Looper.getMainLooper());
        }

        usageMonitorRunnable = new Runnable() {
            @Override
            public void run() {
                monitorAppUsage();
                // Schedule next run to keep it continuous
                usageMonitorHandler.postDelayed(this, MONITOR_INTERVAL);
            }
        };

        usageMonitorHandler.post(usageMonitorRunnable);
        Log.d(TAG, "Started background app usage monitoring.");
    }

    private void stopMonitoringUsage() {
        if (usageMonitorHandler != null && usageMonitorRunnable != null) {
            usageMonitorHandler.removeCallbacks(usageMonitorRunnable);
            Log.d(TAG, "Stopped background app usage monitoring.");
        }
    }

    private void monitorAppUsage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
            long currentTime = System.currentTimeMillis();
            long startTime = currentTime - MONITOR_INTERVAL;

            List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, startTime, currentTime
            );

            if (usageStatsList != null && !usageStatsList.isEmpty()) {
                for (UsageStats usageStats : usageStatsList) {
                    String appName = usageStats.getPackageName();
                    long lastUsed = usageStats.getLastTimeUsed();
                    Log.d(TAG, "App: " + appName + " | Last used: " + lastUsed);
                }
            } else {
                Log.d(TAG, "No app usage data found in this interval.");
            }
        }
    }

    private boolean hasUsageStatsPermission() {
        try {
            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
            long endTime = System.currentTimeMillis();
            long startTime = endTime - 1000 * 60;
            List<UsageStats> stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, startTime, endTime
            );
            return stats != null && !stats.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void requestUsageStatsPermission() {
        Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivityForResult(intent, USAGE_PERMISSION_REQUEST_CODE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMonitoringUsage(); // Clean up
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Make sure the usage monitor continues even if the app goes into the background
        startMonitoringUsageInBackground();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Optionally restart the monitoring in case it was interrupted
        startMonitoringUsageInBackground();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == USAGE_PERMISSION_REQUEST_CODE) {
            if (hasUsageStatsPermission()) {
                Log.d(TAG, "App usage stats permission granted.");
            } else {
                Log.e(TAG, "App usage stats permission denied.");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
        }
    }
}
