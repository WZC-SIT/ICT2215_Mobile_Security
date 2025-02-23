package com.example.sitdoctors.ui.appointments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DoctorAppointmentsPagerAdapter extends FragmentStateAdapter {

    public DoctorAppointmentsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PendingAppointmentsFragment();
            case 1:
                return new AcceptedAppointmentsFragment();
            case 2:
                return new PastAppointmentsFragment();
            default:
                return new PendingAppointmentsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Three tabs (Pending, Accepted, Past)
    }
}
