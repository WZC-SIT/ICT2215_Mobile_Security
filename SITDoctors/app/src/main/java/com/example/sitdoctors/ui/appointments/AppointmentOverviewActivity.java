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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appointment_overview);

        // Overwrite the ActionBar title while keeping the existing style (color, padding, etc.)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Appointment");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set up RecyclerView for displaying appointments
        recyclerView = findViewById(R.id.recyclerView_appointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();
        adapter = new AppointmentAdapter(appointmentList);
        recyclerView.setAdapter(adapter);

        // Fetch appointments from Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("appointments").child(user.getUid());
            fetchAppointments();
        }

        // Find the button in the layout
        Button btnCreateAppointment = findViewById(R.id.btn_create_appointment);
        btnCreateAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ManageAppointmentsActivity when button is clicked
                Intent intent = new Intent(AppointmentOverviewActivity.this, ManageAppointmentsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchAppointments() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            databaseReference = FirebaseDatabase.getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("appointments")
                    .child(user.getUid());

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
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish(); // This will close the activity and return to the previous page
        return true;
    }
}
