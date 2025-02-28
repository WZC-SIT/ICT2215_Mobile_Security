package com.example.sitdoctors.ui.appointments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PatientAppointmentsPagerAdapter extends FragmentStateAdapter {

    public PatientAppointmentsPagerAdapter(@NonNull AppointmentOverviewActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new PendingPatientAppointmentsFragment();
            case 1: return new AcceptedPatientAppointmentsFragment();
            case 2: return new PastPatientAppointmentsFragment();
            default: return new PendingPatientAppointmentsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Three tabs: Pending, Accepted, Past
    }
}
