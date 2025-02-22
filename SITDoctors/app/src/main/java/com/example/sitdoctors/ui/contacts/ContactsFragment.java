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
import com.example.sitdoctors.R;
import java.util.List;

public class ContactsFragment extends Fragment {

    private FragmentContactsBinding binding;

    private List<String> contactsList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Set up ViewModel and binding
        ContactsViewModel contactsViewModel =
                new ViewModelProvider(this).get(ContactsViewModel.class);

        binding = FragmentContactsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize the contacts list and RecyclerView
        contactsList = new ArrayList<>();
        contactsList.add("Dr. Smith");
        contactsList.add("Dr. Johnson");
        contactsList.add("Dr. Brown");
        contactsList.add("Dr. Singh");
        contactsList.add("Dr. Adam");
        contactsList.add("Dr. Cassandra");

        RecyclerView contactsRecyclerView = binding.getRoot().findViewById(R.id.contactsRecyclerView);
        ContactsAdapter contactsAdapter = new ContactsAdapter(contactsList, position -> {
            String selectedDoctor = contactsList.get(position);
            Toast.makeText(getContext(), "Chatting with " + selectedDoctor, Toast.LENGTH_SHORT).show();

            // Add the try-catch block here to catch potential errors
            try {
                Intent intent = new Intent(getActivity(), PatientChatActivity.class);
                intent.putExtra("doctor_name", selectedDoctor);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contactsRecyclerView.setAdapter(contactsAdapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;  // Ensure binding is null to avoid memory leaks
    }
}