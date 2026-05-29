package com.example.myapplicationooo;

import java.util.List;

public class DeepSeekResponse {
    public List<Choice> choices;

    public String getResponseText() {
        if (choices != null && !choices.isEmpty()) {
            Choice choice = choices.get(0);
            if (choice.message != null) {
                return choice.message.content;
            }
        }
        return "DeepSeek không thể đưa ra câu trả lời lúc này.";
    }

    public static class Choice {
        public Message message;
    }

    public static class Message {
        public String content;
    }
}
