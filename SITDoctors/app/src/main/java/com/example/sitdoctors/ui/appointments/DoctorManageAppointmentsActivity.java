package com.example.sitdoctors.ui.appointments;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sitdoctors.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class DoctorManageAppointmentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DoctorAppointmentAdapter adapter;
    private List<DoctorAppointment> appointmentList;
    private DatabaseReference appointmentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_manage_appointments); // No toolbar change

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Appointment");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerViewDoctorAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        appointmentList = new ArrayList<>();
        adapter = new DoctorAppointmentAdapter(appointmentList);
        recyclerView.setAdapter(adapter);

        // ✅ Ensure correct Firebase Database URL
        appointmentsRef = FirebaseDatabase.getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("appointments");

        loadAppointments();
    }

    private void loadAppointments() {
        appointmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) { // Loop through all users
                    for (DataSnapshot appointmentSnapshot : userSnapshot.getChildren()) { // Loop through their appointments
                        DoctorAppointment appointment = appointmentSnapshot.getValue(DoctorAppointment.class);
                        if (appointment != null) {
                            appointment.setId(appointmentSnapshot.getKey()); // Set Firebase-generated ID
                            appointmentList.add(appointment);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DoctorManageAppointmentsActivity.this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
