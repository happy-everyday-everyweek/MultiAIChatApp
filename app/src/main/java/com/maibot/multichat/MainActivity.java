package com.maibot.multichat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText inputMessage;
    private ImageButton sendButton;
    private List<ChatMessage> messages;
    private WebSocketManager wsManager;
    private String userId;
    private String userName = "Android用户";
    
    // MaiBot服务器地址 - 请修改为你的服务器地址
    private static final String SERVER_URL = "ws://192.168.1.100:8001";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadUserId();
        setupRecyclerView();
        setupListeners();
        connectToServer();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
    }

    private void loadUserId() {
        SharedPreferences prefs = getSharedPreferences("maibot_chat", MODE_PRIVATE);
        userId = prefs.getString("user_id", null);
        
        if (userId == null) {
            userId = UUID.randomUUID().toString();
            prefs.edit().putString("user_id", userId).apply();
        }
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
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

    private void connectToServer() {
        wsManager = new WebSocketManager(this, SERVER_URL);
        
        wsManager.connect(userId, userName, new WebSocketManager.MessageCallback() {
            @Override
            public void onConnected(String sessionId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "已连接到服务器", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onMessageReceived(ChatMessage message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addMessage(message);
                    }
                });
            }

            @Override
            public void onHistoryReceived(ChatMessage[] historyMessages) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (ChatMessage msg : historyMessages) {
                            addMessage(msg);
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "错误: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onDisconnected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "已断开连接", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void sendMessage() {
        String text = inputMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        if (!wsManager.isConnected()) {
            Toast.makeText(this, "未连接到服务器", Toast.LENGTH_SHORT).show();
            return;
        }

        wsManager.sendMessage(text);
        inputMessage.setText("");
    }

    private void addMessage(ChatMessage message) {
        messages.add(message);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        recyclerView.smoothScrollToPosition(messages.size() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wsManager != null) {
            wsManager.disconnect();
        }
    }
}
