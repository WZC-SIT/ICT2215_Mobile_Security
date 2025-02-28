package com.example.sitdoctors.ui.appointments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.sitdoctors.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AppointmentOverviewActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appointment_overview);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Appointment");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        // Set up ViewPager Adapter
        PatientAppointmentsPagerAdapter adapter = new PatientAppointmentsPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Pending"); break;
                case 1: tab.setText("Upcoming"); break;
                case 2: tab.setText("Past"); break;
            }
        }).attach();

        // Firebase setup
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            databaseReference = FirebaseDatabase.getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("appointments")
                    .child(userId);

            fetchAppointments(); // Fetch appointments
        }

        // Button for creating a new appointment
        Button btnCreateAppointment = findViewById(R.id.btn_create_appointment);
        btnCreateAppointment.setOnClickListener(view ->
                startActivity(new Intent(AppointmentOverviewActivity.this, ManageAppointmentsActivity.class))
        );
    }

    private void fetchAppointments() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Appointment> appointmentList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Appointment appointment = data.getValue(Appointment.class);
                    if (appointment != null) {
                        appointmentList.add(appointment);
                    }
                }
                Log.d("Firebase", "Fetched " + appointmentList.size() + " appointments.");
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
