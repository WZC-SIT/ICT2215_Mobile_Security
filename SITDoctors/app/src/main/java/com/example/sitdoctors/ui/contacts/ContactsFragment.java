package com.example.sitdoctors.ui.contacts;
import com.example.sitdoctors.ContactsAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;


import com.example.sitdoctors.PatientChatActivity;
import com.example.sitdoctors.databinding.FragmentContactsBinding;
import java.util.ArrayList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.sitdoctors.R;
import java.util.List;
public class ContactsFragment extends Fragment {
    private FragmentContactsBinding binding;
    private List<String> contactsList;
    private ContactsAdapter contactsAdapter;
    private DatabaseReference usersRef;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ContactsViewModel contactsViewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        binding = FragmentContactsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        contactsList = new ArrayList<>();
        RecyclerView contactsRecyclerView = binding.getRoot().findViewById(R.id.contactsRecyclerView);
        contactsAdapter = new ContactsAdapter(contactsList, position -> {
            String selectedDoctor = contactsList.get(position);
            Toast.makeText(getContext(), "Chatting with " + selectedDoctor, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), PatientChatActivity.class);
            intent.putExtra("doctor_name", selectedDoctor);
            startActivity(intent);
        });

        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contactsRecyclerView.setAdapter(contactsAdapter);

        // Initialize Firebase reference to "users"
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Fetch doctors from Firebase
        fetchDoctors();

        return root;
    }

    private void fetchDoctors() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactsList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String role = userSnapshot.child("role").getValue(String.class);
                    String doctorName = userSnapshot.child("name").getValue(String.class);
                    String doctorEmail = userSnapshot.child("email").getValue(String.class);

                    // Check if the user is a doctor and has an email ending with "@hospital.com"
                    if ("doctor".equals(role) && doctorEmail != null && doctorEmail.endsWith("@hospital.com")) {
                        contactsList.add("Dr." + doctorName); // Add only the doctor's name
                    }
                }
                contactsAdapter.notifyDataSetChanged(); // Update RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load doctors.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}