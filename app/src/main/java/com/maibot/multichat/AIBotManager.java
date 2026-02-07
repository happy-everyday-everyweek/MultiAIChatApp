package com.maibot.multichat;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIBotManager {
    private List<AIBot> bots;
    private Context context;
    private Handler handler;
    private Random random;

    public interface ResponseCallback {
        void onResponse(String botName, String response, String color);
    }

    public AIBotManager(Context context) {
        this.context = context;
        this.bots = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
    }

    public void addBot(AIBot bot) {
        bots.add(bot);
    }

    public void processUserMessage(String message, ResponseCallback callback) {
        for (final AIBot bot : bots) {
            if (bot.shouldRespond(message)) {
                int delay = random.nextInt(2000) + 500;
                
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String response = bot.generateResponse(message);
                        callback.onResponse(bot.getName(), response, bot.getColor());
                    }
                }, delay);
            }
        }
    }

    public List<AIBot> getBots() {
        return bots;
    }
}
