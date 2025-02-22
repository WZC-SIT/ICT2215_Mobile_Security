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

public class PatientChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private PatientChatAdapter patientChatAdapter;
    private List<String> messagesList;
    private String doctorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_activity_chat);

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
}
