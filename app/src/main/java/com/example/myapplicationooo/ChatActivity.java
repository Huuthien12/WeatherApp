package com.example.myapplicationooo;

import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

public class ChatActivity extends AppCompatActivity {

    private ChatViewModel viewModel;

    private ChatAdapter adapter;

    private EditText etMessage;

    private TextView tvTyping;

    private RecyclerView rvChat;

    private String weatherContext;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(
                LocaleHelper.onAttach(newBase)
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        // =========================
        // WEATHER CONTEXT
        // =========================

        weatherContext =
                getIntent().getStringExtra(
                        "weather_context"
                );

        if (weatherContext == null) {

            weatherContext =
                    "No weather data available.";
        }

        // =========================
        // VIEWMODEL
        // =========================

        viewModel =
                new ViewModelProvider(this)
                        .get(ChatViewModel.class);

        // =========================
        // VIEWS
        // =========================

        MaterialToolbar toolbar =
                findViewById(R.id.toolbar);

        rvChat =
                findViewById(R.id.rvChat);

        etMessage =
                findViewById(R.id.etMessage);

        ImageButton btnSend =
                findViewById(R.id.btnSend);

        tvTyping =
                findViewById(R.id.tvTyping);

        // =========================
        // TOOLBAR
        // =========================

        toolbar.setNavigationOnClickListener(
                v -> finish()
        );

        // =========================
        // RECYCLERVIEW
        // =========================

        adapter = new ChatAdapter();

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(true);

        rvChat.setLayoutManager(layoutManager);

        rvChat.setAdapter(adapter);

        // =========================
        // OBSERVE MESSAGES
        // =========================

        viewModel.getMessagesLiveData()
                .observe(this, messages -> {

                    adapter.setMessages(messages);

                    if (messages != null
                            && !messages.isEmpty()) {

                        rvChat.smoothScrollToPosition(
                                messages.size() - 1
                        );
                    }
                });

        // =========================
        // OBSERVE TYPING
        // =========================

        viewModel.getIsTyping()
                .observe(this, isTyping -> {

                    tvTyping.setVisibility(
                            isTyping
                                    ? TextView.VISIBLE
                                    : TextView.GONE
                    );
                });

        // =========================
        // SEND MESSAGE
        // =========================

        btnSend.setOnClickListener(v -> {

            String text =
                    etMessage.getText()
                            .toString()
                            .trim();

            if (!text.isEmpty()) {

                viewModel.askAI(
                        text,
                        weatherContext
                );

                etMessage.setText("");
            }
        });
    }
}