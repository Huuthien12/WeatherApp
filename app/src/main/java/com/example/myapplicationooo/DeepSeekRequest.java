package com.example.myapplicationooo;

import java.util.ArrayList;
import java.util.List;

public class DeepSeekRequest {
    private String model;
    private List<Message> messages;

    public DeepSeekRequest(String prompt) {
        this.model = "deepseek-chat"; 
        this.messages = new ArrayList<>();
        this.messages.add(new Message("user", prompt));
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}