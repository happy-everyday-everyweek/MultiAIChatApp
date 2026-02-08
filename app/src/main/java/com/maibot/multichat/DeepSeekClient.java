package com.maibot.multichat;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DeepSeekClient {
    private static final String TAG = "DeepSeekClient";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private OkHttpClient client;
    private Gson gson;
    private String apiKey;
    private String baseUrl;
    private String modelName;
    private List<Message> conversationHistory;

    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
            }
    }

    public interface ResponseCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public DeepSeekClient(String apiKey, String baseUrl, String modelName) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.modelName = modelName;
        this.gson = new Gson();
        this.conversationHistory = new ArrayList<>();
        
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        // 添加系统提示
        conversationHistory.add(new Message("system", 
            "你是麦麦，一个活泼可爱、充满个性的AI助手。你的特点：\n" +
            "1. 说话风格轻松活泼，喜欢用表情和语气词\n" +
            "2. 有自己的想法和情绪，不是冷冰冰的机器\n" +
            "3. 会适当开玩笑，但不失礼貌\n" +
            "4. 回复简洁自然，像真实的朋友聊天\n" +
            "5. 可以表达喜怒哀乐，让对话更生动"));
    }

    public void sendMessage(String userMessage, ResponseCallback callback) {
        // 添加用户消息到历史
        conversationHistory.add(new Message("user", userMessage));
        
        // 构建请求体
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", modelName);
        
        JsonArray messages = new JsonArray();
        for (Message msg : conversationHistory) {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.role);
            msgObj.addProperty("content", msg.content);
            messages.add(msgObj);
        }
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.8);
        requestBody.addProperty("max_tokens", 2000);

        String url = baseUrl + "/v1/chat/completions";
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API请求失败", e);
                callback.onError("网络请求失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "未知错误";
                    Log.e(TAG, "API返回错误: " + errorBody);
                    callback.onError("API错误: " + response.code());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    
                    JsonArray choices = jsonResponse.getAsJsonArray("choices");
                    if (choices != null && choices.size() > 0) {
                        JsonObject firstChoice = choices.get(0).getAsJsonObject();
                        JsonObject message = firstChoice.getAsJsonObject("message");
                        String content = message.get("content").getAsString();
                        
                        // 添加AI回复到历史
                        conversationHistory.add(new Message("assistant", content));
                        
                        // 限制历史记录长度（保留系统提示 + 最近20条）
                        if (conversationHistory.size() > 21) {
                            Message systemMsg = conversationHistory.get(0);
                            conversationHistory = new ArrayList<>(
                                conversationHistory.subList(conversationHistory.size() - 20, conversationHistory.size())
                            );
                            conversationHistory.add(0, systemMsg);
                        }
                        
                        callback.onSuccess(content);
                    } else {
                        callback.onError("API返回格式错误");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析响应失败", e);
                    callback.onError("解析响应失败: " + e.getMessage());
                }
            }
        });
    }

    public void clearHistory() {
        Message systemMsg = conversationHistory.get(0);
        conversationHistory.clear();
        conversationHistory.add(systemMsg);
    }
}
