package com.example.sitdoctors.ui.appointments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sitdoctors.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AppointmentOverviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appointment_overview);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Appointment");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerView_appointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid(); // Get user ID

            adapter = new AppointmentAdapter(AppointmentOverviewActivity.this, appointmentList, userId); // Correct parameters
            recyclerView.setAdapter(adapter);

            databaseReference = FirebaseDatabase.getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("appointments")
                    .child(userId);

            fetchAppointments(); // Fetch appointments
        }

        Button btnCreateAppointment = findViewById(R.id.btn_create_appointment);
        btnCreateAppointment.setOnClickListener(view -> startActivity(new Intent(AppointmentOverviewActivity.this, ManageAppointmentsActivity.class)));
    }

    private void fetchAppointments() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Appointment appointment = data.getValue(Appointment.class);
                    if (appointment != null) {
                        appointmentList.add(appointment);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
