package com.example.sitdoctors.ui.appointments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sitdoctors.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.viewpager2.widget.ViewPager2;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class AcceptedAppointmentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private DoctorAppointmentAdapter adapter;
    private List<DoctorAppointment> appointmentList;
    private DatabaseReference appointmentsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ViewPager2 viewPager = getActivity().findViewById(R.id.viewPager); // Get ViewPager from activity
        appointmentList = new ArrayList<>();
        adapter = new DoctorAppointmentAdapter(appointmentList, viewPager); // Pass ViewPager to adapter
        recyclerView.setAdapter(adapter);

        appointmentsRef = FirebaseDatabase.getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("appointments");

        loadAcceptedAppointments();

        return view;
    }


    private void loadAcceptedAppointments() {
        String currentDoctorId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get logged-in doctor ID

        appointmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) { // Loop through each user
                    for (DataSnapshot appointmentSnapshot : userSnapshot.getChildren()) { // Loop through their appointments
                        DoctorAppointment appointment = appointmentSnapshot.getValue(DoctorAppointment.class);

                        if (appointment != null && "Accepted".equals(appointment.getStatus())) {
                            // ✅ Only show appointments where the logged-in doctor is the assigned doctor
                            if (appointment.getDoctorId() != null && appointment.getDoctorId().equals(currentDoctorId)) {
                                appointment.setId(appointmentSnapshot.getKey()); // Ensure ID is set
                                appointmentList.add(appointment);
                            }
                        }
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Firebase Error: " + error.getMessage());
            }
        });
    }



}
