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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PastPatientAppointmentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList;
    private DatabaseReference databaseReference;
    private SimpleDateFormat dateFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        appointmentList = new ArrayList<>();
        adapter = new AppointmentAdapter(getContext(), appointmentList, userId);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("appointments")
                .child(userId);

        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        loadPastAppointments();
        return view;
    }

    private void loadPastAppointments() {
        databaseReference.orderByChild("status").equalTo("Accepted")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        appointmentList.clear();
                        Date todayDate = new Date();

                        for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                            Appointment appointment = appointmentSnapshot.getValue(Appointment.class);

                            if (appointment != null) {
                                try {
                                    Date appointmentDate = dateFormat.parse(appointment.getDate());

                                    // ✅ Only show past appointments and mark them as "Past"
                                    if (appointmentDate != null && appointmentDate.before(todayDate)) {
                                        appointment.setStatus("Past"); // 🔹 Ensure status is set to "Past"
                                        String doctorId = appointmentSnapshot.child("doctorId").getValue(String.class);

                                        if (doctorId != null) {
                                            fetchDoctorName(appointment, doctorId);
                                        } else {
                                            appointmentList.add(appointment);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        System.out.println("Firebase Error: " + error.getMessage());
                    }
                });
    }


    private void fetchDoctorName(Appointment appointment, String doctorId) {
        DatabaseReference doctorRef = FirebaseDatabase.getInstance()
                .getReference("users") // Assuming doctors are stored in "users"
                .child(doctorId)
                .child("name"); // Assuming doctor's name is stored under "name"

        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String doctorName = snapshot.getValue(String.class);
                    appointment.setReason(appointment.getReason() + "\nDoctor: " + doctorName);
                } else {
                    appointment.setReason(appointment.getReason() + "\nDoctor: Unknown");
                }
                appointmentList.add(appointment);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Firebase Error: " + error.getMessage());
            }
        });
    }
}
