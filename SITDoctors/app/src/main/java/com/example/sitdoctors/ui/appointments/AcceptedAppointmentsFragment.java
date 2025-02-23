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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

                        if (appointment != null && "Accepted".equals(appointment.getStatus())) {
                            try {
                                Date appointmentDate = dateFormat.parse(appointment.getDate());

                                // ✅ Only show appointments that are today or in the future
                                if (appointmentDate != null && !appointmentDate.before(todayDate)) {
                                    if (appointment.getDoctorId() != null && appointment.getDoctorId().equals(currentDoctorId)) {
                                        appointment.setId(appointmentSnapshot.getKey()); // Ensure ID is set
                                        appointmentList.add(appointment);
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                adapter.notifyDataSetChanged(); // ✅ Refresh the UI
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Firebase Error: " + error.getMessage());
            }
        });
    }

}
