package com.example.sitdoctors;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PatientChatAdapter extends RecyclerView.Adapter<PatientChatAdapter.ChatViewHolder> {

    private final List<String> messagesList;

    public PatientChatAdapter(List<String> messagesList) {
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        String message = messagesList.get(position);
        holder.messageTextView.setText(message);
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }
    }
}