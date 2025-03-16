package com.example.sitdoctors;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PatientChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private PatientChatAdapter patientChatAdapter;
    private List<String> messagesList;
    private String doctorName;

    // Firebase References
    private DatabaseReference messagesRef;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_chat);

        // Initialize Firebase references
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        // Retrieve the selected doctor's name
        Intent intent = getIntent();
        if (intent != null) {
            doctorName = intent.getStringExtra("doctor_name");
        }
        // Set the doctor's name as the activity's title
        if (doctorName != null) {
            setTitle(doctorName);  // Set the title to the doctor's name
        }

        // Initialize views
        TextView doctorNameTextView = findViewById(R.id.doctorNameTextView);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        Button sendButton = findViewById(R.id.sendButton);

        // Set doctor name
        if (doctorName != null) {
            doctorNameTextView.setText(getString(R.string.chat_with, doctorName));
        }
        // Initialize RecyclerView
        messagesList = new ArrayList<>();
        patientChatAdapter = new PatientChatAdapter(messagesList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(patientChatAdapter);

        // Send message logic
        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessageToFirebase(message);
                messagesList.add("You: " + message);
                patientChatAdapter.notifyItemInserted(messagesList.size() - 1); // Notify the adapter of the new item
                chatRecyclerView.smoothScrollToPosition(messagesList.size() - 1);

                messageEditText.setText("");  // Clear input field
            }
            else {
                Toast.makeText(PatientChatActivity.this, R.string.empty_message_warning, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void sendMessageToFirebase(String message) {
        // Check if currentUser is null (user not logged in)
        if (currentUser != null) {
            // Create message object
            String messageId = messagesRef.push().getKey();  // Generate unique ID for the message
            String doctorId = getIntent().getStringExtra("doctor_id");
            Message newMessage = new Message(message, currentUser.getUid(), "doctorId", currentUser.getDisplayName());

            // Save message to Firebase under 'messages' node
            if (messageId != null) {
                messagesRef.child(messageId).setValue(newMessage);
            }
        }
}
    // Message model class
    public static class Message {
        public String message;
        public String senderId;
        public String receiverId;
        public String senderName;

        public Message(String message, String senderId, String receiverId, String senderName) {
            this.message = message;
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.senderName = senderName;
        }
    }
}