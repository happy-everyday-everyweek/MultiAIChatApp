package com.maibot.multichat;

import android.content.Context;
import android.util.Log;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MaiBotManager {
    private static final String TAG = "MaiBotManager";
    private Python python;
    private PyObject maibotModule;
    private Gson gson;
    private ExecutorService executor;
    private boolean initialized = false;

    public static class BotInfo {
        public String id;
        public String name;
        public String color;
    }

    public static class BotResponse {
        public String bot_id;
        public String bot_name;
        public String color;
        public String content;
    }

    public interface InitCallback {
        void onSuccess(List<BotInfo> bots);
        void onError(String error);
    }

    public interface MessageCallback {
        void onResponse(List<BotResponse> responses);
        void onError(String error);
    }

    public MaiBotManager(Context context) {
        this.gson = new Gson();
        this.executor = Executors.newSingleThreadExecutor();
        
        // 初始化Python环境
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(context));
        }
        python = Python.getInstance();
    }

    public void initialize(String apiKey, int botCount, InitCallback callback) {
        executor.execute(() -> {
            try {
                // 加载Python模块
                maibotModule = python.getModule("maibot_bridge");
                
                // 初始化Bot实例
                maibotModule.callAttr("initialize_bots", apiKey, botCount);
                
                // 获取Bot列表
                String botListJson = maibotModule.callAttr("get_bot_list").toString();
                Type listType = new TypeToken<List<BotInfo>>(){}.getType();
                List<BotInfo> bots = gson.fromJson(botListJson, listType);
                
                initialized = true;
                callback.onSuccess(bots);
                
            } catch (Exception e) {
                Log.e(TAG, "初始化失败", e);
                callback.onError("初始化失败: " + e.getMessage());
            }
        });
    }

    public void sendMessage(String message, MessageCallback callback) {
        if (!initialized) {
            callback.onError("MaiBot未初始化");
            return;
        }

        executor.execute(() -> {
            try {
                // 调用Python发送消息
                String responsesJson = maibotModule.callAttr("send_message", message).toString();
                
                // 解析响应
                Type listType = new TypeToken<List<BotResponse>>(){}.getType();
                List<BotResponse> responses = gson.fromJson(responsesJson, listType);
                
                callback.onResponse(responses);
                
            } catch (Exception e) {
                Log.e(TAG, "发送消息失败", e);
                callback.onError("发送消息失败: " + e.getMessage());
            }
        });
    }

    public void clearHistory() {
        if (!initialized) return;
        
        executor.execute(() -> {
            try {
                maibotModule.callAttr("clear_history");
            } catch (Exception e) {
                Log.e(TAG, "清空历史失败", e);
            }
        });
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void shutdown() {
        executor.shutdown();
    }
}
