package com.maibot.multichat;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText inputMessage;
    private ImageButton sendButton;
    private List<ChatMessage> messages;
    private AIBotManager botManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initBots();
        setupRecyclerView();
        setupListeners();
        
        addSystemMessage("欢迎来到多AI聊天室！");
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
    }

    private void initBots() {
        messages = new ArrayList<>();
        botManager = new AIBotManager(this);
        
        botManager.addBot(new AIBot("麦麦", "我是麦麦，一个活泼可爱的AI助手！", "#FF6B9D"));
        botManager.addBot(new AIBot("小智", "我是小智，擅长技术问题解答。", "#4A90E2"));
        botManager.addBot(new AIBot("诗诗", "我是诗诗，喜欢文学和艺术。", "#9B59B6"));
        botManager.addBot(new AIBot("阿乐", "我是阿乐，幽默风趣的段子手。", "#F39C12"));
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupListeners() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String text = inputMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        ChatMessage userMessage = new ChatMessage(text, "用户", true, "#000000");
        addMessage(userMessage);
        inputMessage.setText("");

        botManager.processUserMessage(text, new AIBotManager.ResponseCallback() {
            @Override
            public void onResponse(String botName, String response, String color) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ChatMessage botMessage = new ChatMessage(response, botName, false, color);
                        addMessage(botMessage);
                    }
                });
            }
        });
    }

    private void addMessage(ChatMessage message) {
        messages.add(message);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        recyclerView.smoothScrollToPosition(messages.size() - 1);
    }

    private void addSystemMessage(String text) {
        ChatMessage systemMessage = new ChatMessage(text, "系统", false, "#999999");
        addMessage(systemMessage);
    }
}
