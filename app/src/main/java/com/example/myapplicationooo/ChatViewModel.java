package com.example.myapplicationooo;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository repository;
    private final MutableLiveData<Boolean> isTyping = new MutableLiveData<>(false);
    private final MutableLiveData<List<ChatMessage>> messagesLiveData = new MutableLiveData<>(new ArrayList<>());
    private ListenerRegistration listenerRegistration;

    // API Key của DeepSeek
    private final String DEEPSEEK_KEY = "sk-838d13ef67b94cebb3681cc42771dd07";

    public ChatViewModel(@NonNull Application application) {
        super(application);
        repository = new ChatRepository(application);
        observeMessages();
    }

    private void observeMessages() {
        if (repository.getChatHistory() == null) return;

        listenerRegistration = repository.getChatHistory()
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ChatViewModel", "Lỗi Firestore: ", error);
                        return;
                    }
                    if (value != null) {
                        List<ChatMessage> messages = value.toObjects(ChatMessage.class);
                        
                        // Sắp xếp tin nhắn theo thứ tự thời gian
                        try {
                            messages.sort((m1, m2) -> {
                                if (m1.getTimestamp() == null || m2.getTimestamp() == null) return 0;
                                return m1.getTimestamp().compareTo(m2.getTimestamp());
                            });
                        } catch (Exception e) {
                            Log.e("ChatViewModel", "Lỗi sắp xếp: " + e.getMessage());
                        }

                        messagesLiveData.setValue(messages);
                    }
                });
    }

    public LiveData<Boolean> getIsTyping() { return isTyping; }
    public LiveData<List<ChatMessage>> getMessagesLiveData() { return messagesLiveData; }

    public void askAI(String userPrompt, String weatherContext) {
        if (userPrompt.trim().isEmpty()) return;

        isTyping.setValue(true);

        // 1. Lưu tin nhắn người dùng lên Firestore
        repository.saveMessage(new ChatMessage(userPrompt, true));

        // 2. Tạo nội dung yêu cầu (Prompt)
        String fullPrompt = "Bạn là trợ lý AI thông minh tích hợp trong ứng dụng thời tiết.\n\n"
                + "Thông tin thời tiết hiện tại: " + weatherContext + "\n"
                + "Câu hỏi hoặc yêu cầu của người dùng: " + userPrompt + "\n\n"
                + "Hãy trả lời bằng tiếng Việt một cách tự nhiên, thân thiện, ngắn gọn và tập trung vào câu hỏi.";

        // 3. Khởi tạo Request DeepSeek
        DeepSeekRequest request = new DeepSeekRequest(fullPrompt);
        String authHeader = "Bearer " + DEEPSEEK_KEY;

        // 4. Gọi API DeepSeek
        repository.getApi().getResponse(authHeader, request).enqueue(new Callback<DeepSeekResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeepSeekResponse> call, @NonNull Response<DeepSeekResponse> response) {
                isTyping.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    String aiText = response.body().getResponseText();
                    repository.saveMessage(new ChatMessage(aiText, false));
                } else {
                    Log.e("ChatViewModel", "DeepSeek API Error: " + response.code());
                    repository.saveMessage(new ChatMessage("DeepSeek đang bận (Lỗi " + response.code() + "). Vui lòng thử lại sau.", false));
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeepSeekResponse> call, @NonNull Throwable t) {
                isTyping.setValue(false);
                Log.e("ChatViewModel", "Kết nối DeepSeek thất bại", t);
                repository.saveMessage(new ChatMessage("Mất kết nối mạng. Hãy kiểm tra lại Wi-Fi hoặc dữ liệu di động.", false));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
