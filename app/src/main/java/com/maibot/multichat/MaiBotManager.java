package com.maibot.multichat;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
    private static final int POLL_INTERVAL_MS = 500; // 轮询间隔500ms
    
    private Python python;
    private PyObject maibotModule;
    private Gson gson;
    private ExecutorService executor;
    private Handler mainHandler;
    private Handler pollingHandler;
    private boolean initialized = false;
    private boolean isPolling = false;

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
        void onResponse(BotResponse response);
        void onError(String error);
    }

    public MaiBotManager(Context context) {
        this.gson = new Gson();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.pollingHandler = new Handler(Looper.getMainLooper());
        
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
                boolean success = maibotModule.callAttr("initialize_bots", apiKey, botCount).toBoolean();
                
                if (!success) {
                    mainHandler.post(() -> callback.onError("初始化失败"));
                    return;
                }
                
                // 获取Bot列表
                String botListJson = maibotModule.callAttr("get_bot_list").toString();
                Type listType = new TypeToken<List<BotInfo>>(){}.getType();
                List<BotInfo> bots = gson.fromJson(botListJson, listType);
                
                initialized = true;
                
                // 启动消息轮询
                startMessagePolling();
                
                mainHandler.post(() -> callback.onSuccess(bots));
                
            } catch (Exception e) {
                Log.e(TAG, "初始化失败", e);
                mainHandler.post(() -> callback.onError("初始化失败: " + e.getMessage()));
            }
        });
    }

    private MessageCallback currentCallback = null;
    
    public void sendMessage(String message, MessageCallback callback) {
        if (!initialized) {
            callback.onError("MaiBot未初始化");
            return;
        }

        this.currentCallback = callback;
        
        executor.execute(() -> {
            try {
                // 调用Python发送消息（异步触发）
                maibotModule.callAttr("send_message", message);
                
                // 消息已发送，回复会通过轮询机制异步返回
                Log.d(TAG, "消息已发送到MaiBot");
                
            } catch (Exception e) {
                Log.e(TAG, "发送消息失败", e);
                mainHandler.post(() -> {
                    if (currentCallback != null) {
                        currentCallback.onError("发送消息失败: " + e.getMessage());
                    }
                });
            }
        });
    }
    
    private void startMessagePolling() {
        if (isPolling) return;
        
        isPolling = true;
        pollingHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isPolling || !initialized) return;
                
                executor.execute(() -> {
                    try {
                        // 获取待处理的消息
                        String messagesJson = maibotModule.callAttr("get_pending_messages").toString();
                        Type listType = new TypeToken<List<BotResponse>>(){}.getType();
                        List<BotResponse> responses = gson.fromJson(messagesJson, listType);
                        
                        // 处理每个回复
                        for (BotResponse response : responses) {
                            mainHandler.post(() -> {
                                if (currentCallback != null) {
                                    currentCallback.onResponse(response);
                                }
                            });
                        }
                        
                    } catch (Exception e) {
                        Log.e(TAG, "轮询消息失败", e);
                    }
                });
                
                // 继续轮询
                pollingHandler.postDelayed(this, POLL_INTERVAL_MS);
            }
        });
    }
    
    private void stopMessagePolling() {
        isPolling = false;
        pollingHandler.removeCallbacksAndMessages(null);
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
        stopMessagePolling();
        executor.shutdown();
    }
}
