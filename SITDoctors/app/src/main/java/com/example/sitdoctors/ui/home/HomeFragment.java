package com.example.sitdoctors.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.sitdoctors.R;
import com.example.sitdoctors.ui.appointments.AppointmentOverviewActivity;
import com.example.sitdoctors.ui.appointments.DoctorManageAppointmentsActivity;
import com.example.sitdoctors.ui.chat.ChatActivity; // ✅ Import ChatActivity
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.sitdoctors.ui.nearbyClinics.NearbyClinicsActivity;

public class HomeFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference userRef;
    private String userRole = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        userRef = FirebaseDatabase.getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");

        // Find the Appointments CardView
        CardView cardAppointments = root.findViewById(R.id.card_appointments);
        fetchUserRole(cardAppointments);

        // Find the card and set click listener
        CardView findClinicsCard = root.findViewById(R.id.card_find_clinics);
        findClinicsCard.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NearbyClinicsActivity.class);
            startActivity(intent);
        });


        // ✅ Find the Chat CardView
        CardView cardChat = root.findViewById(R.id.card_chat);

        // Set visibility for the chat card based on user role
        if (cardChat != null) {
            if ("doctor".equals(userRole)) {
                // Allow chat for doctors (card visible)
                cardChat.setVisibility(View.VISIBLE);

                cardChat.setOnClickListener(view -> {
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    startActivity(intent);
                });
            } else {
                // Remove chat card for patients (card hidden)
                cardChat.setVisibility(View.GONE);
            }
        }

        return root;
    }

    private void fetchUserRole(CardView cardAppointments) {
        if (user != null) {
            userRef.child(user.getUid()).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userRole = snapshot.getValue(String.class);

                        // Set up Appointments Card click listener based on role
                        cardAppointments.setOnClickListener(view -> {
                            if ("doctor".equals(userRole)) {
                                Intent intent = new Intent(getActivity(), DoctorManageAppointmentsActivity.class);
                                startActivity(intent);
                            } else if ("patient".equals(userRole)) {
                                Intent intent = new Intent(getActivity(), AppointmentOverviewActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getContext(), "User role unknown", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to fetch user role", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
