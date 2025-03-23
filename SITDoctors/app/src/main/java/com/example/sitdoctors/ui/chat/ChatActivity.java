package com.example.sitdoctors.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sitdoctors.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashSet;

public class ChatActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private DatabaseReference usersRef, messagesRef;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    private ArrayList<String> userList = new ArrayList<>();
    private ArrayList<String> userUids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        listView = findViewById(R.id.chat_user_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        listView.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        if (userEmail != null && userEmail.contains("@hospital.com")) {
            loadPatientsWhoMessaged();
        } else {
            loadAllDoctors();
        }

        // ✅ Item click opens MessageActivity
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= userUids.size()) {
                Log.d("ChatActivity", "Clicked an invalid list item");
                return;
            }

            String selectedEntry = userList.get(position);
            String selectedUid = userUids.get(position);

            Log.d("ChatActivity", "Clicked: " + selectedEntry + ", UID: " + selectedUid);
            Toast.makeText(ChatActivity.this, "Opening chat with: " + selectedEntry, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ChatActivity.this, MessageActivity.class);
            intent.putExtra("chatDisplayName", selectedEntry);
            intent.putExtra("chatUid", selectedUid);
            startActivity(intent);
        });


    }

    private void loadAllDoctors() {
        userList.clear();
        userUids.clear();

        usersRef.orderByChild("role").equalTo("doctor").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String name = userSnap.child("name").getValue(String.class);
                    String email = userSnap.child("email").getValue(String.class);
                    String uid = userSnap.getKey();

                    userList.add(name + " (" + email + ")");
                    userUids.add(uid);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load doctors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPatientsWhoMessaged() {
        userList.clear();
        userUids.clear();

        final String doctorUid = currentUser.getUid();
        final HashSet<String> seenUids = new HashSet<>();

        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot roomSnap : snapshot.getChildren()) {
                    String roomId = roomSnap.getKey();
                    if (roomId != null && roomId.contains(doctorUid)) {
                        for (DataSnapshot msgSnap : roomSnap.getChildren()) {
                            String senderUid = msgSnap.child("sender").getValue(String.class);
                            if (senderUid != null && !senderUid.equals(doctorUid) && !seenUids.contains(senderUid)) {
                                seenUids.add(senderUid);  // ✅ Mark as seen early

                                usersRef.child(senderUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot patientSnapshot) {
                                        String name = patientSnapshot.child("name").getValue(String.class);
                                        String email = patientSnapshot.child("email").getValue(String.class);
                                        if (name != null && email != null) {
                                            userList.add(name + " (" + email + ")");
                                            userUids.add(senderUid);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("ChatActivity", "Failed to load patient info");
                                    }
                                });
                            }
                        }
                    }
                }

                if (seenUids.isEmpty()) {
                    userList.clear();
                    userList.add("No patients have messaged you yet.");
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

