package com.example.sitdoctors.ui.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sitdoctors.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MessageActivity extends AppCompatActivity {

    private TextView chatTitle;
    private LinearLayout messagesContainer;
    private EditText inputMessage;
    private Button sendButton;

    private FirebaseUser currentUser;
    private DatabaseReference messagesRef;

    private String otherUserName;
    private String otherUserUid; // UID of the other person (doctor or patient)
    private String chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        chatTitle = findViewById(R.id.chat_with_title);
        messagesContainer = findViewById(R.id.messages_container);
        inputMessage = findViewById(R.id.input_message);
        sendButton = findViewById(R.id.send_button);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        // Get other user info (you should pass UID from ChatActivity)
        otherUserName = getIntent().getStringExtra("chatDisplayName");
        otherUserUid = getIntent().getStringExtra("chatUid"); // MUST pass from ChatActivity

        chatTitle.setText("Chat with " + otherUserName);

        // Build chat room ID (sorted for consistency)
        chatRoomId = buildChatRoomId(currentUser.getUid(), otherUserUid);

        // Send button logic
        sendButton.setOnClickListener(view -> {
            String message = inputMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                Log.d("MessageActivity", "Sending message to chatRoomId: " + chatRoomId);
                Log.d("MessageActivity", "Sender UID: " + currentUser.getUid());
                Log.d("MessageActivity", "Receiver UID: " + otherUserUid);

                sendMessage(message);
                inputMessage.setText("");
            }
        });

        // Real-time message updates
        listenForMessages();

        // Add TextWatcher to log keystrokes
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // You can log the current state before changes are made (optional)
                Log.d("KeyListener", "Before Text Changed: " + charSequence.toString());
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Log the changes as the user types
                Log.d("KeyListener", "Text Changed: " + charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Log after the text has changed
                Log.d("KeyListener", "After Text Changed: " + editable.toString());
            }
        });
    }

    private String buildChatRoomId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    private void sendMessage(String messageText) {
        String messageId = messagesRef.child(chatRoomId).push().getKey();
        if (messageId == null) return;

        HashMap<String, Object> messageData = new HashMap<>();
        messageData.put("sender", currentUser.getUid());
        messageData.put("text", messageText);
        messageData.put("timestamp", ServerValue.TIMESTAMP);

        messagesRef.child(chatRoomId).child(messageId).setValue(messageData)
                .addOnSuccessListener(aVoid -> Log.d("MessageActivity", "Message sent"))
                .addOnFailureListener(e -> Log.e("MessageActivity", "Message failed to send", e));
    }

    private void listenForMessages() {
        messagesRef.child(chatRoomId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesContainer.removeAllViews(); // Clear old messages
                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                    String text = msgSnap.child("text").getValue(String.class);
                    String senderUid = msgSnap.child("sender").getValue(String.class);
                    Long timestamp = msgSnap.child("timestamp").getValue(Long.class);

                    if (text != null && senderUid != null) {
                        boolean isSentByMe = senderUid.equals(currentUser.getUid());
                        addMessageToUI(isSentByMe ? "You: " + text : otherUserName + ": " + text);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMessageToUI(String messageText) {
        TextView messageView = new TextView(this);
        messageView.setText(messageText);
        messageView.setTextSize(16f);
        messageView.setPadding(12, 6, 12, 6);
        messagesContainer.addView(messageView);
    }
}
