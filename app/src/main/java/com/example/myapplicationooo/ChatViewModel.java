package com.example.myapplicationooo;

import android.app.Application;

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

    private final MutableLiveData<Boolean> isTyping =
            new MutableLiveData<>(false);

    private final MutableLiveData<List<ChatMessage>>
            messagesLiveData =
            new MutableLiveData<>(new ArrayList<>());

    private ListenerRegistration listenerRegistration;

    private final String GEMINI_KEY =
            "AIzaSyBSmq8WK9acGudS-M9dFwaCA9V_o3_6bVk";

    public ChatViewModel(
            @NonNull Application application
    ) {

        super(application);

        repository =
                new ChatRepository(application);

        observeMessages();
    }

    // =========================
    // OBSERVE FIRESTORE
    // =========================

    private void observeMessages() {

        if (repository.getChatHistory() == null) {
            return;
        }

        listenerRegistration =
                repository.getChatHistory()
                        .addSnapshotListener((value, error) -> {

                            if (value != null) {

                                List<ChatMessage> messages =
                                        value.toObjects(
                                                ChatMessage.class
                                        );

                                messagesLiveData.setValue(
                                        messages
                                );
                            }
                        });
    }

    // =========================
    // GETTERS
    // =========================

    public LiveData<Boolean> getIsTyping() {
        return isTyping;
    }

    public LiveData<List<ChatMessage>>
    getMessagesLiveData() {

        return messagesLiveData;
    }

    // =========================
    // ASK AI
    // =========================

    public void askAI(
            String userPrompt,
            String weatherContext
    ) {

        isTyping.setValue(true);

        // SAVE USER MESSAGE

        repository.saveMessage(
                new ChatMessage(
                        userPrompt,
                        true
                )
        );

        String fullPrompt =
                "Bạn là trợ lý thời tiết AI.\n\n"

                        + "Thời tiết hiện tại:\n"
                        + weatherContext

                        + "\n\nCâu hỏi người dùng:\n"
                        + userPrompt

                        + "\n\nHãy trả lời ngắn gọn,"
                        + " thân thiện,"
                        + " dễ hiểu.";

        repository.getApi()
                .getResponse(
                        GEMINI_KEY,
                        new GeminiRequest(fullPrompt)
                )
                .enqueue(new Callback<GeminiResponse>() {

                    @Override
                    public void onResponse(
                            Call<GeminiResponse> call,
                            Response<GeminiResponse> response
                    ) {

                        isTyping.setValue(false);

                        if (response.isSuccessful()
                                && response.body() != null) {

                            String aiText =
                                    response.body()
                                            .getResponseText();

                            repository.saveMessage(
                                    new ChatMessage(
                                            aiText,
                                            false
                                    )
                            );

                        } else {

                            repository.saveMessage(
                                    new ChatMessage(
                                            "AI response failed",
                                            false
                                    )
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<GeminiResponse> call,
                            Throwable t
                    ) {

                        isTyping.setValue(false);

                        repository.saveMessage(
                                new ChatMessage(
                                        "Error: "
                                                + t.getMessage(),
                                        false
                                )
                        );
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