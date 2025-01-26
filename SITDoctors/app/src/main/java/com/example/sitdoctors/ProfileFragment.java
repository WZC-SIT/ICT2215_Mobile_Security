package com.example.sitdoctors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;  // Import TextView
import android.widget.Toast;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileFragment extends Fragment {

    // UI components
    private EditText etProfileName, etProfileAge, etProfilePhone, etProfileAddress;
    private TextView tvProfileEmail;  // Email is now displayed as TextView
    private Button btnSaveProfile;
    Button logoutButton;
    // Firebase instances
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();  // Get current user's UID
        userRef = FirebaseDatabase.getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(userId); // Reference to the user's node

        // Initialize UI components
        etProfileName = root.findViewById(R.id.etProfileName);
        etProfileAge = root.findViewById(R.id.etProfileAge);
        etProfilePhone = root.findViewById(R.id.etProfilePhone);
        etProfileAddress = root.findViewById(R.id.etProfileAddress);
        tvProfileEmail = root.findViewById(R.id.tvProfileEmail);  // Initialize TextView for email
        btnSaveProfile = root.findViewById(R.id.btnSaveProfile);

        // Load user data from Firebase
        loadUserData();

        // Save Changes when button is clicked
        btnSaveProfile.setOnClickListener(v -> saveUserData());

        return root;
    }

    // Method to load user data from Firebase
    private void loadUserData() {
        // Add a listener to get data from Firebase
        userRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                // Check if data exists
                if (dataSnapshot.exists()) {
                    // Retrieve the user data
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String age = dataSnapshot.child("age").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    String address = dataSnapshot.child("address").getValue(String.class);

                    // Populate the EditText fields with the user's data
                    etProfileName.setText(name);
                    tvProfileEmail.setText(email);  // Set email in the TextView
                    etProfileAge.setText(age);
                    etProfilePhone.setText(phone);
                    etProfileAddress.setText(address);
                } else {
                    // If the user data doesn't exist, show a message
                    Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                // Handle possible errors
                Toast.makeText(getContext(), "Failed to load user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to save the user data to Firebase
    private void saveUserData() {
        // Get the updated data from the EditText fields
        String name = etProfileName.getText().toString().trim();
        String age = etProfileAge.getText().toString().trim();
        String phone = etProfilePhone.getText().toString().trim();
        String address = etProfileAddress.getText().toString().trim();

        // Check if all fields are filled
        if (name.isEmpty() || age.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a HashMap to update the user data
        java.util.HashMap<String, Object> userUpdates = new java.util.HashMap<>();
        userUpdates.put("name", name);
        userUpdates.put("age", age);
        userUpdates.put("phone", phone);
        userUpdates.put("address", address);

        // Save the updated data to Firebase
        userRef.updateChildren(userUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update profile: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
