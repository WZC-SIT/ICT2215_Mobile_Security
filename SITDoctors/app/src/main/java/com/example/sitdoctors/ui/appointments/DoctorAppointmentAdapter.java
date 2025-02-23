package com.example.sitdoctors.ui.appointments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sitdoctors.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class DoctorAppointmentAdapter extends RecyclerView.Adapter<DoctorAppointmentAdapter.ViewHolder> {

    private final List<DoctorAppointment> appointmentList;
    private final DatabaseReference appointmentsRef;

    public DoctorAppointmentAdapter(List<DoctorAppointment> appointmentList) {
        this.appointmentList = appointmentList;
        this.appointmentsRef = FirebaseDatabase.getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("appointments");
    }

    @NonNull
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

        holder.btnAccept.setOnClickListener(v -> updateAppointmentStatus(appointment.getId(), "Accepted"));
        holder.btnReject.setOnClickListener(v -> updateAppointmentStatus(appointment.getId(), "Rejected"));
    }

    private void updateAppointmentStatus(String appointmentId, String status) {
        appointmentsRef.child(appointmentId).child("status").setValue(status)
                .addOnSuccessListener(aVoid -> Toast.makeText(appointmentsRef.getDatabase().getApp().getApplicationContext(),
                        "Updated Successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(appointmentsRef.getDatabase().getApp().getApplicationContext(),
                        "Update Failed", Toast.LENGTH_SHORT).show());
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
