package com.maibot.multichat;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import java.util.concurrent.TimeUnit;

public class WebSocketManager extends WebSocketListener {
    private static final String TAG = "WebSocketManager";
    private WebSocket webSocket;
    private OkHttpClient client;
    private Gson gson;
    private MessageCallback callback;
    private String serverUrl;
    private String userId;
    private String userName;
    private boolean isConnected = false;

    public interface MessageCallback {
        void onConnected(String sessionId);
        void onMessageReceived(ChatMessage message);
        void onHistoryReceived(ChatMessage[] messages);
        void onError(String error);
        void onDisconnected();
    }

    public WebSocketManager(Context context, String serverUrl) {
        this.serverUrl = serverUrl;
        this.gson = new Gson();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public void connect(String userId, String userName, MessageCallback callback) {
        this.userId = userId;
        this.userName = userName;
        this.callback = callback;

        String wsUrl = serverUrl + "/api/chat/ws?user_id=" + userId + "&user_name=" + userName;
        Request request = new Request.Builder()
                .url(wsUrl)
                .build();

        webSocket = client.newWebSocket(request, this);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        isConnected = true;
        Log.d(TAG, "WebSocket连接成功");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        try {
            JsonObject json = gson.fromJson(text, JsonObject.class);
            String type = json.get("type").getAsString();

            switch (type) {
                case "session_info":
                    String sessionId = json.get("session_id").getAsString();
                    if (callback != null) {
                        callback.onConnected(sessionId);
                    }
                    break;

                case "history":
                    ChatMessage[] messages = gson.fromJson(
                            json.getAsJsonArray("messages"),
                            ChatMessage[].class
                    );
                    if (callback != null) {
                        callback.onHistoryReceived(messages);
                    }
                    break;

                case "user_message":
                case "bot_message":
                    ChatMessage message = parseMessage(json);
                    if (callback != null && message != null) {
                        callback.onMessageReceived(message);
                    }
                    break;

                case "system":
                    String content = json.get("content").getAsString();
                    long timestamp = json.get("timestamp").getAsLong();
                    ChatMessage systemMsg = new ChatMessage(content, "系统", false, "#999999");
                    if (callback != null) {
                        callback.onMessageReceived(systemMsg);
                    }
                    break;

                case "error":
                    String error = json.get("content").getAsString();
                    if (callback != null) {
                        callback.onError(error);
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "解析消息失败", e);
        }
    }

    private ChatMessage parseMessage(JsonObject json) {
        try {
            String content = json.get("content").getAsString();
            long timestamp = (long) (json.get("timestamp").getAsDouble() * 1000);
            
            JsonObject sender = json.getAsJsonObject("sender");
            String senderName = sender.get("name").getAsString();
            boolean isBot = sender.get("is_bot").getAsBoolean();
            
            String color = isBot ? "#FF6B9D" : "#000000";
            
            return new ChatMessage(content, senderName, !isBot, color, timestamp);
        } catch (Exception e) {
            Log.e(TAG, "解析消息对象失败", e);
            return null;
        }
    }

    public void sendMessage(String content) {
        if (!isConnected || webSocket == null) {
            if (callback != null) {
                callback.onError("未连接到服务器");
            }
            return;
        }

        JsonObject message = new JsonObject();
        message.addProperty("type", "message");
        message.addProperty("content", content);
        message.addProperty("user_name", userName);

        webSocket.send(gson.toJson(message));
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        isConnected = false;
        Log.e(TAG, "WebSocket连接失败", t);
        if (callback != null) {
            callback.onError("连接失败: " + t.getMessage());
        }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        isConnected = false;
        Log.d(TAG, "WebSocket已关闭: " + reason);
        if (callback != null) {
            callback.onDisconnected();
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "用户断开连接");
            webSocket = null;
        }
        isConnected = false;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
