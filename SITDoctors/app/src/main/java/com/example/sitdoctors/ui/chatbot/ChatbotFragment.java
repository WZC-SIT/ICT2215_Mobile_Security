package com.example.sitdoctors.ui.chatbot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sitdoctors.R;
import java.util.ArrayList;
import java.util.List;

public class ChatbotFragment extends Fragment {
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);

        // Initialize UI elements
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);

        // Initialize chat message list and adapter
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);

        // Handle send button click
        sendButton.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void sendMessage() {
        String userMessage = messageInput.getText().toString().trim();
        if (!userMessage.isEmpty()) {
            // Add the new message at the **end** of the list
            chatMessages.add(new ChatMessage(userMessage, true));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);

            // Scroll to the latest message
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

            // Clear input field
            messageInput.setText("");
        }
    }

}
