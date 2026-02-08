package com.maibot.multichat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
    private MaiBotManager botManager;
    private SharedPreferences prefs;
    private boolean isProcessing = false;
    private boolean isInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = getSharedPreferences("maibot_settings", MODE_PRIVATE);
        
        initViews();
        setupRecyclerView();
        setupListeners();
        
        botManager = new MaiBotManager(this);
        checkAndInitialize();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
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

    private void checkAndInitialize() {
        String apiKey = prefs.getString("api_key", "");
        
        if (apiKey.isEmpty()) {
            showWelcomeDialog();
        } else {
            initializeBots();
        }
    }

    private void showWelcomeDialog() {
        new AlertDialog.Builder(this)
            .setTitle("欢迎使用麦麦群聊")
            .setMessage("这是一个多AI群聊应用，您可以与多个AI机器人同时对话！\n\n" +
                       "首次使用需要配置DeepSeek API Key：\n\n" +
                       "1. 访问 https://platform.deepseek.com\n" +
                       "2. 注册并获取API Key\n" +
                       "3. 在设置中填入API Key\n" +
                       "4. 选择Bot数量（1-5个）")
            .setPositiveButton("去设置", (dialog, which) -> {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            })
            .setNegativeButton("稍后", null)
            .setCancelable(false)
            .show();
    }

    private void initializeBots() {
        String apiKey = prefs.getString("api_key", "");
        int botCount = prefs.getInt("bot_count", 3);
        
        addSystemMessage("正在初始化" + botCount + "个AI Bot...");
        
        botManager.initialize(apiKey, botCount, new MaiBotManager.InitCallback() {
            @Override
            public void onSuccess(List<MaiBotManager.BotInfo> bots) {
                runOnUiThread(() -> {
                    isInitialized = true;
                    StringBuilder botNames = new StringBuilder("群聊成员：");
                    for (int i = 0; i < bots.size(); i++) {
                        if (i > 0) botNames.append("、");
                        botNames.append(bots.get(i).name);
                    }
                    addSystemMessage(botNames.toString());
                    addSystemMessage("初始化完成！开始聊天吧~");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    addSystemMessage("初始化失败: " + error);
                    Toast.makeText(MainActivity.this, "初始化失败，请检查设置", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void sendMessage() {
        if (!isInitialized) {
            Toast.makeText(this, "Bot未初始化，请先配置API Key", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = inputMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        if (isProcessing) {
            Toast.makeText(this, "AI们正在思考中，请稍候...", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示用户消息
        ChatMessage userMessage = new ChatMessage(text, "我", true, "#000000");
        addMessage(userMessage);
        inputMessage.setText("");

        // 显示正在输入状态
        isProcessing = true;
        sendButton.setEnabled(false);
        
        addSystemMessage("AI们正在思考...");

        // 调用所有Bot
        botManager.sendMessage(text, new MaiBotManager.MessageCallback() {
            @Override
            public void onResponse(List<MaiBotManager.BotResponse> responses) {
                runOnUiThread(() -> {
                    // 移除"正在思考"消息
                    if (!messages.isEmpty() && messages.get(messages.size() - 1).getSenderName().equals("系统")) {
                        messages.remove(messages.size() - 1);
                        chatAdapter.notifyItemRemoved(messages.size());
                    }
                    
                    // 添加所有Bot的回复
                    for (MaiBotManager.BotResponse response : responses) {
                        ChatMessage botMessage = new ChatMessage(
                            response.content,
                            response.bot_name,
                            false,
                            response.color
                        );
                        addMessage(botMessage);
                    }
                    
                    isProcessing = false;
                    sendButton.setEnabled(true);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // 移除"正在思考"消息
                    if (!messages.isEmpty() && messages.get(messages.size() - 1).getSenderName().equals("系统")) {
                        messages.remove(messages.size() - 1);
                        chatAdapter.notifyItemRemoved(messages.size());
                    }
                    
                    addSystemMessage("错误: " + error);
                    isProcessing = false;
                    sendButton.setEnabled(true);
                    
                    Toast.makeText(MainActivity.this, "发送失败: " + error, Toast.LENGTH_LONG).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_clear) {
            clearChat();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void clearChat() {
        new AlertDialog.Builder(this)
            .setTitle("清空聊天")
            .setMessage("确定要清空所有聊天记录吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                messages.clear();
                chatAdapter.notifyDataSetChanged();
                if (botManager != null) {
                    botManager.clearHistory();
                }
                addSystemMessage("聊天记录已清空");
            })
            .setNegativeButton("取消", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从设置返回时检查是否需要重新初始化
        String apiKey = prefs.getString("api_key", "");
        if (!apiKey.isEmpty() && !isInitialized) {
            initializeBots();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (botManager != null) {
            botManager.shutdown();
        }
    }
}

