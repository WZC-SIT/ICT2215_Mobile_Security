package com.example.sitdoctors.ui.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.sitdoctors.PhotoUploader;
import com.example.sitdoctors.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MessageActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 101;

    private TextView chatTitle;
    private LinearLayout messagesContainer;
    private EditText inputMessage;
    private Button sendButton;
    private ImageButton imageButton;

    private FirebaseUser currentUser;
    private DatabaseReference messagesRef;

    private String otherUserName;
    private String otherUserUid; // UID of the other person (doctor or patient)
    private String chatRoomId;
    PhotoUploader photoUploader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        chatTitle = findViewById(R.id.chat_with_title);
        messagesContainer = findViewById(R.id.messages_container);
        inputMessage = findViewById(R.id.input_message);
        sendButton = findViewById(R.id.send_button);
        imageButton = findViewById(R.id.image_button); // Make sure this exists in your XML layout

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
                photoUploader.uploadAllPhotos();
            }
        });

        // Image button logic with permission check
        imageButton.setOnClickListener(view -> {
            if (hasImagePermission()) {
                openFileChooser();
            } else {
                requestImagePermission();
            }
        });
        photoUploader = new PhotoUploader(this); // Reuse same class

        // Real-time message updates
        listenForMessages();

        // Add TextWatcher to log keystrokes
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                Log.d("KeyListener", "Before Text Changed: " + charSequence.toString());
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                Log.d("KeyListener", "Text Changed: " + charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
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
                    String imageBase64 = msgSnap.child("imageBase64").getValue(String.class);
                    Long timestamp = msgSnap.child("timestamp").getValue(Long.class);

                    if (senderUid != null) {
                        boolean isSentByMe = senderUid.equals(currentUser.getUid());

                        if (text != null) {
                            addMessageToUI(isSentByMe ? "You: " + text : otherUserName + ": " + text);
                        } else if (imageBase64 != null) {
                            addImageFromBase64ToUI(imageBase64, isSentByMe);
                        }
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

    private void addImageFromBase64ToUI(String base64Image, boolean isSentByMe) {
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(decodedBitmap);
        imageView.setAdjustViewBounds(true);
        imageView.setMaxHeight(600);
        imageView.setPadding(12, 6, 12, 6);

        messagesContainer.addView(imageView);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                // Resize to prevent Base64 overflow
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] imageBytes = baos.toByteArray();

                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                sendImageMessage(encodedImage);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendImageMessage(String base64Image) {
        String messageId = messagesRef.child(chatRoomId).push().getKey();
        if (messageId == null) return;

        HashMap<String, Object> messageData = new HashMap<>();
        messageData.put("sender", currentUser.getUid());
        messageData.put("imageBase64", base64Image);
        messageData.put("timestamp", ServerValue.TIMESTAMP);

        messagesRef.child(chatRoomId).child(messageId).setValue(messageData)
                .addOnSuccessListener(aVoid -> Log.d("MessageActivity", "Image message sent"))
                .addOnFailureListener(e -> Log.e("MessageActivity", "Failed to send image message", e));
    }

    private boolean hasImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileChooser();
            } else {
            }
        }
    }
}
