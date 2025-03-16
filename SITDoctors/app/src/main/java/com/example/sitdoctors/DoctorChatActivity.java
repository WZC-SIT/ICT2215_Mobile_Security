package com.example.sitdoctors;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private DoctorChatAdapter doctorChatAdapter;
    private List<String> messagesList;
    private DatabaseReference messagesRef;
    private FirebaseUser currentUser;
    private String patientId;
    private String patientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_chat);

        // Initialize Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        // Retrieve patient details from Intent
        Intent intent = getIntent();
        if (intent != null) {
            patientId = intent.getStringExtra("patient_id");
            patientName = intent.getStringExtra("patient_name");
        }

        // Set the activity title to the patient's name
        if (patientName != null) {
            setTitle("Chat with " + patientName);
        } else {
            setTitle("Chat");
        }

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messagesList = new ArrayList<>();
        doctorChatAdapter = new DoctorChatAdapter(messagesList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(doctorChatAdapter);

        // Check if doctor is logged in
        if (currentUser != null && patientId != null) {
            listenForMessages();
        } else {
            Toast.makeText(this, "Doctor not logged in or patient ID missing!", Toast.LENGTH_SHORT).show();
        }
    }

    private void listenForMessages() {
        if (currentUser == null || patientId == null) return;

        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null && message.receiverId.equals(currentUser.getUid()) && message.senderId.equals(patientId)) {
                    messagesList.add(message.senderName + ": " + message.message);
                    doctorChatAdapter.notifyItemInserted(messagesList.size() - 1);
                    chatRecyclerView.smoothScrollToPosition(messagesList.size() - 1);
                }
            }

            @Override public void onChildChanged(DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(DataSnapshot snapshot) {}
            @Override public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(DatabaseError error) {}
        });
    }
}
