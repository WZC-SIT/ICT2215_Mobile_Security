package com.example.sitdoctors.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.example.sitdoctors.R;
import com.example.sitdoctors.ui.appointments.AppointmentOverviewActivity;
import com.example.sitdoctors.ui.appointments.ManageAppointmentsActivity;
import com.example.sitdoctors.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Find the Appointments CardView and set click listener
        CardView cardAppointments = root.findViewById(R.id.card_appointments);
        cardAppointments.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AppointmentOverviewActivity.class);
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
