package com.example.sitdoctors.ui.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.sitdoctors.R;
import com.example.sitdoctors.UserProfile;

import java.util.List;

public class DoctorsListAdapter extends BaseAdapter {

    private final Context context;
    private final List<UserProfile> doctors;

    public DoctorsListAdapter(Context context, List<UserProfile> doctors) {
        this.context = context;
        this.doctors = doctors;
    }

    @Override
    public int getCount() {
        return doctors.size();
    }

    @Override
    public UserProfile getItem(int position) {
        return doctors.get(position);
    }

    @Override
    public long getItemId(int position) {
        // You can return position if there's no unique ID
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // 1) Inflate the layout if needed
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_doctor, parent, false);

            holder = new ViewHolder();
            holder.nameTextView = convertView.findViewById(R.id.doctorNameTextView);
            holder.emailTextView = convertView.findViewById(R.id.doctorEmailTextView);

            convertView.setTag(holder);
        } else {
            // Use the existing view
            holder = (ViewHolder) convertView.getTag();
        }

        // 2) Get the current doctor
        UserProfile doctor = getItem(position);

        // 3) Bind data
        holder.nameTextView.setText(doctor.getName());
        holder.emailTextView.setText(doctor.getEmail());

        return convertView;
    }

    // ViewHolder pattern for performance
    private static class ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
    }
}
