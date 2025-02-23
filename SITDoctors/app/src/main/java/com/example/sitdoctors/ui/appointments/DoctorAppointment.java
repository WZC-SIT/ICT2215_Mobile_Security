package com.example.sitdoctors.ui.appointments;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties // ✅ Prevents Firebase from throwing errors on unknown fields
public class DoctorAppointment {
    private String id;
    private String appointmentId;
    private String userId;
    private String email;
    private String date;
    private String reason;
    private String status;

    // ✅ Default constructor required for Firebase
    public DoctorAppointment() {}

    public DoctorAppointment(String id, String appointmentId, String userId, String email, String date, String reason, String status) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.userId = userId;
        this.email = email;
        this.date = date;
        this.reason = reason;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
