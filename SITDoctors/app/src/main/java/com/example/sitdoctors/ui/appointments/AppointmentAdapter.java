package com.example.sitdoctors.ui.appointments;

import android.content.Context;
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
import androidx.appcompat.app.AlertDialog;


public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<Appointment> appointmentList;
    private Context context;
    private DatabaseReference databaseReference;

    public AppointmentAdapter(Context context, List<Appointment> appointmentList, String userId) {
        this.context = context;
        this.appointmentList = appointmentList;
        this.databaseReference = FirebaseDatabase.getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("appointments").child(userId);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        holder.tvDate.setText("Date: " + appointment.getDate());
        holder.tvReason.setText("Reason: " + appointment.getReason());
        holder.tvStatus.setText("Status: " + appointment.getStatus());

        // 🔹 Hide Delete button for Past Appointments
        if ("Past".equals(appointment.getStatus())) {
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnDelete.setVisibility(View.VISIBLE);
        }

        // ✅ Delete button logic for Pending and Accepted Appointments
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Appointment?")
                    .setMessage("Are you sure you want to delete this appointment?\n\n" +
                            "If you want to change the date, you need to create a new appointment.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        String appointmentId = appointment.getAppointmentId();
                        if (appointmentId != null) {
                            databaseReference.child(appointmentId).removeValue()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "Appointment Deleted", Toast.LENGTH_SHORT).show();
                                            appointmentList.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, appointmentList.size());
                                        } else {
                                            Toast.makeText(context, "Failed to Delete", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }


    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvReason, tvStatus;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvReason = itemView.findViewById(R.id.tv_reason);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnDelete = itemView.findViewById(R.id.btn_delete_appointment);
        }
    }
}
