package com.example.sitdoctors.ui.chat;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.*;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sitdoctors.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MessageActivity extends AppCompatActivity {

    private TextView chatTitle;
    private LinearLayout messagesContainer;
    private EditText inputMessage;
    private Button sendButton;
    private ImageButton attachButton;

    private FirebaseUser currentUser;
    private DatabaseReference messagesRef;
    private FirebaseStorage storage;

    private String otherUserName;
    private String otherUserUid;
    private String chatRoomId;

    private static final int REQUEST_SELECT_IMAGE = 101;
    private static final int REQUEST_SELECT_AUDIO = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        chatTitle = findViewById(R.id.chat_with_title);
        messagesContainer = findViewById(R.id.messages_container);
        inputMessage = findViewById(R.id.input_message);
        sendButton = findViewById(R.id.send_button);
        attachButton = findViewById(R.id.ic_attach_files);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        storage = FirebaseStorage.getInstance();

        otherUserName = getIntent().getStringExtra("chatDisplayName");
        otherUserUid = getIntent().getStringExtra("chatUid");
        chatTitle.setText("Chat with " + otherUserName);

        chatRoomId = buildChatRoomId(currentUser.getUid(), otherUserUid);

        sendButton.setOnClickListener(view -> {
            String message = inputMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                sendMessage("text", message);
                inputMessage.setText("");
            }
        });

        // Attach button opens dialog
        attachButton.setOnClickListener(v -> showAttachmentOptions());

        listenForMessages();

        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                Log.d("KeyListener", "Text Changed: " + charSequence.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable editable) {}
        });
    }

    private String buildChatRoomId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    private void sendMessage(String type, String content) {
        String messageId = messagesRef.child(chatRoomId).push().getKey();
        if (messageId == null) return;

        HashMap<String, Object> messageData = new HashMap<>();
        messageData.put("sender", currentUser.getUid());
        messageData.put("type", type);
        messageData.put("content", content);
        messageData.put("timestamp", ServerValue.TIMESTAMP);

        messagesRef.child(chatRoomId).child(messageId).setValue(messageData)
                .addOnSuccessListener(aVoid -> Log.d("MessageActivity", "Message sent"))
                .addOnFailureListener(e -> Log.e("MessageActivity", "Failed to send", e));
    }
    private void listenForMessages() {
        messagesRef.child(chatRoomId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesContainer.removeAllViews();
                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                    String type = msgSnap.child("type").getValue(String.class);
                    String content = msgSnap.child("content").getValue(String.class);
                    String senderUid = msgSnap.child("sender").getValue(String.class);

                    if (type != null && content != null && senderUid != null) {
                        boolean isMe = senderUid.equals(currentUser.getUid());
                        displayMessage(type, content, isMe);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayMessage(String type, String content, boolean isMe) {
        TextView messageView = new TextView(this);
        String displayText;

        switch (type) {
            case "text":
                displayText = (isMe ? "You: " : otherUserName + ": ") + content;
                break;
            case "image":
                displayText = (isMe ? "You sent an image: " : otherUserName + " sent an image: ") + content;
                break;
            case "audio":
                displayText = (isMe ? "You sent an audio: " : otherUserName + " sent an audio: ") + content;
                break;
            default:
                displayText = "Unknown message type";
        }
        messageView.setText(displayText);
        messageView.setPadding(12, 6, 12, 6);
        messagesContainer.addView(messageView);
    }

    // Show attachment dialog
    private void showAttachmentOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an action")
                .setItems(new CharSequence[]{"Send Image", "Send Audio"}, (dialog, which) -> {
                    if (which == 0) {
                        pickImageFromGallery();
                    } else if (which == 1) {
                        pickAudioFile();
                    }
                })
                .show();
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    private void pickAudioFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, REQUEST_SELECT_AUDIO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (requestCode == REQUEST_SELECT_IMAGE && fileUri != null) {
                uploadFileToFirebase(fileUri, "image");
            } else if (requestCode == REQUEST_SELECT_AUDIO && fileUri != null) {
                uploadFileToFirebase(fileUri, "audio");
            }
        }
    }

    private void uploadFileToFirebase(Uri fileUri, String type) {
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + (type.equals("image") ? ".jpg" : ".mp3");
        StorageReference fileRef = storage.getReference().child("chat_uploads/" + fileName);

        fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                sendMessage(type, uri.toString());
                Toast.makeText(MessageActivity.this, type + " uploaded", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(MessageActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
