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
    private DeepSeekClient aiClient;
    private SharedPreferences prefs;
    private boolean isProcessing = false;

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
        checkApiKey();
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

    private void checkApiKey() {
        String apiKey = prefs.getString("api_key", "");
        
        if (apiKey.isEmpty()) {
            showWelcomeDialog();
        } else {
            initAIClient();
            addSystemMessage("欢迎来到多AI聊天室！开始和麦麦聊天吧~");
        }
    }

    private void showWelcomeDialog() {
        new AlertDialog.Builder(this)
            .setTitle("欢迎使用")
            .setMessage("首次使用需要配置DeepSeek API Key\n\n" +
                       "1. 访问 https://platform.deepseek.com\n" +
                       "2. 注册并获取API Key\n" +
                       "3. 在设置中填入API Key")
            .setPositiveButton("去设置", (dialog, which) -> {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            })
            .setNegativeButton("稍后", null)
            .setCancelable(false)
            .show();
    }

    private void initAIClient() {
        String apiKey = prefs.getString("api_key", "");
        String baseUrl = prefs.getString("base_url", "https://api.deepseek.com");
        String modelName = prefs.getString("model_name", "deepseek-chat");
        
        aiClient = new DeepSeekClient(apiKey, baseUrl, modelName);
    }

    private void sendMessage() {
        if (aiClient == null) {
            Toast.makeText(this, "请先在设置中配置API Key", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = inputMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        if (isProcessing) {
            Toast.makeText(this, "AI正在思考中，请稍候...", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示用户消息
        ChatMessage userMessage = new ChatMessage(text, "我", true, "#000000");
        addMessage(userMessage);
        inputMessage.setText("");

        // 显示正在输入状态
        isProcessing = true;
        sendButton.setEnabled(false);
        
        ChatMessage typingMessage = new ChatMessage("正在思考...", "麦麦", false, "#FF6B9D");
        addMessage(typingMessage);
        final int typingPosition = messages.size() - 1;

        // 调用AI
        aiClient.sendMessage(text, new DeepSeekClient.ResponseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 移除"正在思考"消息
                        messages.remove(typingPosition);
                        chatAdapter.notifyItemRemoved(typingPosition);
                        
                        // 添加AI回复
                        ChatMessage aiMessage = new ChatMessage(response, "麦麦", false, "#FF6B9D");
                        addMessage(aiMessage);
                        
                        isProcessing = false;
                        sendButton.setEnabled(true);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 移除"正在思考"消息
                        messages.remove(typingPosition);
                        chatAdapter.notifyItemRemoved(typingPosition);
                        
                        // 显示错误消息
                        ChatMessage errorMessage = new ChatMessage(
                            "抱歉，出错了: " + error, 
                            "系统", 
                            false, 
                            "#FF0000"
                        );
                        addMessage(errorMessage);
                        
                        isProcessing = false;
                        sendButton.setEnabled(true);
                        
                        Toast.makeText(MainActivity.this, "错误: " + error, Toast.LENGTH_LONG).show();
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
                if (aiClient != null) {
                    aiClient.clearHistory();
                }
                addSystemMessage("聊天记录已清空");
            })
            .setNegativeButton("取消", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从设置返回时重新初始化AI客户端
        String apiKey = prefs.getString("api_key", "");
        if (!apiKey.isEmpty() && aiClient == null) {
            initAIClient();
            addSystemMessage("API配置已更新，可以开始聊天了~");
        }
    }
}

