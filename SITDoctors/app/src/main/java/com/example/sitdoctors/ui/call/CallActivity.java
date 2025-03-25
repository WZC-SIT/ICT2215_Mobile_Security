package com.example.sitdoctors.ui.call;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sitdoctors.R;

public class CallActivity extends AppCompatActivity {

    private TextView callStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_call);

        callStatusText = findViewById(R.id.call_status_text);
        callStatusText.setText("Calling emergency services...");

        // Simulate a call for 10 seconds
        new Handler().postDelayed(() -> {
            callStatusText.setText("Your emergency has been logged. We'll get back to you ASAP.");
        }, 10000); // 10 seconds delay
    }
}