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

        // ✅ Hide "Accept" button if the appointment is already accepted
        if ("Accepted".equals(appointment.getStatus())) {
            holder.btnAccept.setVisibility(View.GONE);
        } else {
            holder.btnAccept.setVisibility(View.VISIBLE);
        }

        // ✅ Hide "Reject" button in Pending tab
        if ("Pending".equals(appointment.getStatus())) {
            holder.btnReject.setVisibility(View.GONE);
        } else {
            holder.btnReject.setVisibility(View.VISIBLE);
        }

        holder.btnAccept.setOnClickListener(v -> updateAppointmentStatus(appointment, "Accepted"));
        holder.btnReject.setOnClickListener(v -> updateAppointmentStatus(appointment, "Pending")); // 🔹 Move to Pending if rejected
    }



    private void updateAppointmentStatus(DoctorAppointment appointment, String newStatus) {
        if (appointment.getId() == null) {
            Toast.makeText(viewPager.getContext(), "Error: Appointment ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String doctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference usersRef = FirebaseDatabase.getInstance()
                .getReference("users").child(doctorId);

        // ✅ Fetch the doctor's details from Firebase before updating
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String doctorName = snapshot.child("name").getValue(String.class);
                    String doctorEmail = snapshot.child("email").getValue(String.class);

                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put("status", newStatus);

                    // ✅ If status is "Accepted", add doctor details
                    if ("Accepted".equals(newStatus)) {
                        if (doctorId != null && doctorName != null && doctorEmail != null) {
                            updateMap.put("doctorId", doctorId);
                            updateMap.put("doctorEmail", doctorEmail);
                            updateMap.put("doctorName", doctorName);
                        } else {
                            Toast.makeText(viewPager.getContext(), "Doctor details missing!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else if ("Pending".equals(newStatus)) {
                        // ✅ If moved back to Pending, remove doctor details
                        updateMap.put("doctorId", null);
                        updateMap.put("doctorEmail", null);
                        updateMap.put("doctorName", null);
                    }

                    // ✅ Update the appointment in Firebase
                    appointmentsRef.child(appointment.getUserId()).child(appointment.getId())
                            .updateChildren(updateMap)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(viewPager.getContext(), "Appointment moved to " + newStatus, Toast.LENGTH_SHORT).show();

                                // ✅ Move to the correct tab after updating
                                if ("Accepted".equals(newStatus)) {
                                    viewPager.setCurrentItem(1); // Switch to Accepted tab
                                } else if ("Pending".equals(newStatus)) {
                                    viewPager.setCurrentItem(0); // Switch to Pending tab
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
