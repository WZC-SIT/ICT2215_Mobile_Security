package com.example.sitdoctors.ui.appointments;

public class Appointment {
    private String appointmentId;
    private String date;
    private String reason;
    private String status;

    // Empty constructor for Firebase
    public Appointment() {}

    public Appointment(String appointmentId, String date, String reason, String status) {
        this.appointmentId = appointmentId;
        this.date = date;
        this.reason = reason;
        this.status = status;
    }

    // Getters
    public String getAppointmentId() { return appointmentId; }
    public String getDate() { return date; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
