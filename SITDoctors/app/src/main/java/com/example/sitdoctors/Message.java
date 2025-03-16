package com.example.sitdoctors;

public class Message {
    public String senderId;  // Add senderId
    public String senderName;
    public String message;
    public String receiverId;

    // Default constructor required for Firebase
    public Message() {}

    public Message(String senderId, String senderName, String message, String receiverId) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.receiverId = receiverId;
    }
}
