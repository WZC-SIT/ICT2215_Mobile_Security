package com.example.sitdoctors.ui.nearbyClinics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClinicAdapter extends RecyclerView.Adapter<ClinicAdapter.ViewHolder> {

    public interface OnClinicClickListener {
        void onClinicClick(Clinic clinic);
    }

    private final List<Clinic> clinics;
    private final OnClinicClickListener listener;

    public ClinicAdapter(List<Clinic> clinics, OnClinicClickListener listener) {
        this.clinics = clinics;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView clinicText;

        public ViewHolder(View view) {
            super(view);
            clinicText = view.findViewById(android.R.id.text1);
        }
    }

    @Override
    public ClinicAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Clinic clinic = clinics.get(position);
        holder.clinicText.setText(clinic.title + " - " + Math.round(clinic.distance) + "m");

        holder.itemView.setOnClickListener(v -> listener.onClinicClick(clinic));
    }

    @Override
    public int getItemCount() {
        return clinics.size();
    }
}
