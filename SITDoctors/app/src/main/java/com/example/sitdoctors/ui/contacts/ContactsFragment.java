package com.example.sitdoctors.ui.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.content.Intent;
import com.example.sitdoctors.ui.chat.ChatActivity;
import com.example.sitdoctors.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ContactsFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference userRef;
    private String userRole = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_contacts, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        userRef = FirebaseDatabase.getInstance("https://sitdoctors-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");

        fetchUserRoleAndOpenChat();

        return root;
    }

    private void fetchUserRoleAndOpenChat() {
        if (user != null) {
            userRef.child(user.getUid()).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userRole = snapshot.getValue(String.class);

                        // Directly open ChatActivity if user is a patient
                        if ("patient".equals(userRole)) {
                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                            startActivity(intent);
                            // Remove this ContactsFragment from the back stack so back goes home
                            requireActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(getContext(), "User role unknown", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to fetch user role", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
