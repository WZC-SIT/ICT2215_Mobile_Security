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
import com.google.firebase.database.*;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PendingAppointmentsFragment extends Fragment {

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

        loadPendingAppointments();

        return view;
    }


    private void loadPendingAppointments() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Get today's date
        Calendar calendar = Calendar.getInstance();
        String todayDateStr = dateFormat.format(calendar.getTime());

        Date todayDate;
        try {
            todayDate = dateFormat.parse(todayDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        appointmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot appointmentSnapshot : userSnapshot.getChildren()) {
                        DoctorAppointment appointment = appointmentSnapshot.getValue(DoctorAppointment.class);

                        if (appointment != null && "Pending".equals(appointment.getStatus())) {
                            try {
                                Date appointmentDate = dateFormat.parse(appointment.getDate());

                                // ✅ Only show pending appointments that are today or in the future
                                if (appointmentDate != null && !appointmentDate.before(todayDate)) {
                                    appointment.setId(appointmentSnapshot.getKey()); // Ensure ID is set
                                    appointmentList.add(appointment);
                                } else {
                                    // ✅ Check if userId and appointmentId exist before deleting
                                    if (appointment.getUserId() != null && appointment.getId() != null) {
                                        appointmentsRef.child(appointment.getUserId())
                                                .child(appointment.getId()).removeValue()
                                                .addOnSuccessListener(aVoid -> System.out.println("Deleted past pending appointment"))
                                                .addOnFailureListener(e -> System.out.println("Failed to delete past pending appointment"));
                                    } else {
                                        System.out.println("Skipping deletion: userId or appointmentId is null");
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                adapter.notifyDataSetChanged(); // ✅ Refresh UI
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Firebase Error: " + error.getMessage());
            }
        });
    }




}
