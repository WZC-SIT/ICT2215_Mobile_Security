package com.example.sitdoctors.ui.appointments;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class DoctorAppointment {
    private String id;
    private String appointmentId;
    private String userId;  // ✅ Ensure this field exists
    private String email;
    private String date;
    private String reason;
    private String status;
    private String doctorId;
    private String doctorEmail;
    private String doctorName;

    // ✅ Default constructor required for Firebase
    public DoctorAppointment() {}

    public DoctorAppointment(String id, String appointmentId, String userId, String email, String date, String reason, String status, String doctorId, String doctorEmail, String doctorName) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.userId = userId;
        this.email = email;
        this.date = date;
        this.reason = reason;
        this.status = status;
        this.doctorId = doctorId;
        this.doctorEmail = doctorEmail;
        this.doctorName = doctorName;
    }

    // ✅ Add Getter and Setter for `userId`
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorEmail() { return doctorEmail; }
    public void setDoctorEmail(String doctorEmail) { this.doctorEmail = doctorEmail; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
}

