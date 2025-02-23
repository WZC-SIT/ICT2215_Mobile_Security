package com.example.sitdoctors.ui.appointments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sitdoctors.R;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PastAppointmentsFragment extends Fragment {

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

        appointmentList = new ArrayList<>();
        adapter = new DoctorAppointmentAdapter(appointmentList, null); // Pass null for ViewPager
        recyclerView.setAdapter(adapter);

        appointmentsRef = FirebaseDatabase.getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("appointments");

        loadPastAppointments();
        return view;
    }

    private void loadPastAppointments() {
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

                        if (appointment != null && "Accepted".equals(appointment.getStatus())) { // ✅ Only Accepted
                            try {
                                Date appointmentDate = dateFormat.parse(appointment.getDate());

                                // ✅ Only show past accepted appointments
                                if (appointmentDate != null && appointmentDate.before(todayDate)) {
                                    appointment.setId(appointmentSnapshot.getKey()); // Ensure ID is set
                                    appointmentList.add(appointment);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
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
