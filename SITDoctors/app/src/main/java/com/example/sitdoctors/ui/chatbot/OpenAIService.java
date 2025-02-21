package com.example.sitdoctors.ui.chatbot;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import java.io.IOException;

public class OpenAIService {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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";
    public static String getAIResponse(String userMessage) {
        OkHttpClient client = new OkHttpClient();

        try {
            // Construct request payload
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "gpt-3.5-turbo");

            // Creating message array with user input
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "user").put("content", userMessage));
            jsonBody.put("messages", messages);

            jsonBody.put("max_tokens", 150);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                Log.e("OpenAIService", "API Error: " + response.code() + " - " + response.message());
                return "Error: " + response.message() + " (Code " + response.code() + ")";
            }

            // Get response body
            String responseBody = response.body().string();
            Log.d("OpenAIService", "Response: " + responseBody);

            JSONObject jsonResponse = new JSONObject(responseBody);
            return jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

        } catch (IOException e) {
            Log.e("OpenAIService", "IOException: " + e.getMessage(), e);
            return "Error connecting to OpenAI API.";
        } catch (JSONException e) {
            Log.e("OpenAIService", "JSON Parsing Error: " + e.getMessage(), e);
            return "Error parsing AI response.";
        } catch (Exception e) {
            Log.e("OpenAIService", "Unexpected Error: " + e.getMessage(), e);
            return "Something went wrong.";
        }
    }
}
