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

public class PendingPatientAppointmentsFragment extends Fragment {

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

        loadPendingAppointments();
        return view;
    }

    private void loadPendingAppointments() {
        databaseReference.orderByChild("status").equalTo("Pending")
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

                                    // ✅ Show only future appointments
                                    if (appointmentDate != null && appointmentDate.after(todayDate)) {
                                        appointmentList.add(appointment);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
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
