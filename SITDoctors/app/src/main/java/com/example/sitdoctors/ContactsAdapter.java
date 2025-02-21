package com.example.sitdoctors;
import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private final List<String> contactsList;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public ContactsAdapter(List<String> contactsList, OnItemClickListener onItemClickListener) {
        this.contactsList = contactsList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        String doctorName = contactsList.get(position);
        holder.contactNameTextView.setText(doctorName);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactNameTextView;

        public ContactViewHolder(View itemView) {
            super(itemView);
            contactNameTextView = itemView.findViewById(R.id.contactNameTextView);
        }
    }
}
