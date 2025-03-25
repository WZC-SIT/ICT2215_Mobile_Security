package com.example.sitdoctors.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.sitdoctors.DoctorProfileActivity;
import com.example.sitdoctors.R;
import com.example.sitdoctors.UserProfile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private ListView doctorsListView;
    private DoctorsListAdapter adapter;
    private List<UserProfile> doctorsList = new ArrayList<>();
    private DatabaseReference usersRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // 1) Find your ListView
        doctorsListView = root.findViewById(R.id.doctorsListView);

        // 2) Initialize adapter
        adapter = new DoctorsListAdapter(requireContext(), doctorsList);
        doctorsListView.setAdapter(adapter);

        // 3) Initialize Firebase reference
        usersRef = FirebaseDatabase.getInstance(
                        "https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");

        // 4) Load the doctors from Firebase
        loadDoctors();

        // 5) Handle click events
        doctorsListView.setOnItemClickListener((parent, view, position, id) -> {
            UserProfile selectedDoctor = doctorsList.get(position);

            // Launch a new activity to show full details
            Intent intent = new Intent(getActivity(), DoctorProfileActivity.class);
            // Pass doctor’s email or ID to fetch details
            intent.putExtra("doctorEmail", selectedDoctor.getEmail());
            startActivity(intent);
        });

        return root;
    }

    private void loadDoctors() {
        // Query for users whose role == "doctor"
        Query query = usersRef.orderByChild("role").equalTo("doctor");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    UserProfile doctor = ds.getValue(UserProfile.class);
                    if (doctor != null) {
                        doctorsList.add(doctor);
                    }
                }
                // Update the list
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Failed to load doctors: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
