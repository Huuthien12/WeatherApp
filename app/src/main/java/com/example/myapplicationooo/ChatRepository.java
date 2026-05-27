package com.example.myapplicationooo;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ChatRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getUid();

    private final GeminiInterface api;

    public ChatRepository(Context context) {
        api = ApiClient.getClient(context).create(GeminiInterface.class);
    }

    public GeminiInterface getApi() {
        return api;
    }

    public void saveMessage(ChatMessage message) {
        if (userId != null) {
            db.collection("users")
                    .document(userId)
                    .collection("ai_chats")
                    .add(message);
        }
    }

    public Query getChatHistory() {
        if (userId == null) return null;

        return db.collection("users")
                .document(userId)
                .collection("ai_chats")
                .orderBy("timestamp", Query.Direction.ASCENDING);
    }
}