package com.example.myapplicationooo;

import com.google.firebase.Timestamp;

public class ChatMessage {
    private String message;
    private boolean isUser;
    private Timestamp timestamp;

    public ChatMessage() {
        // Required for Firestore
    }

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = Timestamp.now();
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isUser() { return isUser; }
    public void setUser(boolean user) { isUser = user; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
