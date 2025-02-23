package com.example.sitdoctors.ui.appointments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.example.sitdoctors.R;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.viewpager2.widget.ViewPager2;

public class DoctorAppointmentAdapter extends RecyclerView.Adapter<DoctorAppointmentAdapter.ViewHolder> {

    private final List<DoctorAppointment> appointmentList;
    private final DatabaseReference appointmentsRef;
    private final ViewPager2 viewPager;

    public DoctorAppointmentAdapter(List<DoctorAppointment> appointmentList, ViewPager2 viewPager) {
        this.appointmentList = appointmentList;
        this.viewPager = viewPager;
        this.appointmentsRef = FirebaseDatabase.getInstance()
                .getReference("appointments");
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorAppointment appointment = appointmentList.get(position);
        holder.tvPatientId.setText("User ID: " + appointment.getUserId());
        holder.tvPatientName.setText("Email: " + appointment.getEmail());
        holder.tvDate.setText("Date: " + appointment.getDate());
        holder.tvReason.setText("Reason: " + appointment.getReason());
        holder.tvStatus.setText("Status: " + appointment.getStatus());

        if ("Accepted".equals(appointment.getStatus())) {
            holder.btnAccept.setVisibility(View.GONE);
        } else {
            holder.btnAccept.setVisibility(View.VISIBLE);
        }

        holder.btnAccept.setOnClickListener(v -> updateAppointmentStatus(appointment, "Accepted"));
        holder.btnReject.setOnClickListener(v -> updateAppointmentStatus(appointment, "Rejected"));
    }


    private void updateAppointmentStatus(DoctorAppointment appointment, String newStatus) {
        if (appointment.getId() == null) {
            Toast.makeText(viewPager.getContext(), "Error: Appointment ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the currently logged-in doctor's UID
        String doctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference to the Firebase "users" table
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(doctorId);


        // Fetch the doctor's details
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String doctorName = snapshot.child("name").getValue(String.class);
                    String doctorEmail = snapshot.child("email").getValue(String.class);

                    // Create an update map
                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put("status", newStatus);

                    if ("Accepted".equals(newStatus)) {
                        updateMap.put("doctorId", doctorId);
                        updateMap.put("doctorEmail", doctorEmail);
                        updateMap.put("doctorName", doctorName);
                    }

                    // Update the appointment in Firebase
                    appointmentsRef.child(appointment.getUserId()).child(appointment.getId())
                            .updateChildren(updateMap)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(viewPager.getContext(), "Appointment marked as " + newStatus, Toast.LENGTH_SHORT).show();
                                if ("Accepted".equals(newStatus)) {
                                    viewPager.setCurrentItem(1); // Move to Accepted tab
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(viewPager.getContext(), "Failed to update appointment", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(viewPager.getContext(), "Doctor details not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(viewPager.getContext(), "Failed to fetch doctor details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientId, tvPatientName, tvDate, tvReason, tvStatus;
        Button btnAccept, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientId = itemView.findViewById(R.id.tvPatientId);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
