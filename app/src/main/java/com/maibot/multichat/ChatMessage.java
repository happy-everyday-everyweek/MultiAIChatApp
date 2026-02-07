package com.maibot.multichat;

public class ChatMessage {
    private String content;
    private String senderName;
    private boolean isUser;
    private String color;
    private long timestamp;

    public ChatMessage(String content, String senderName, boolean isUser, String color) {
        this.content = content;
        this.senderName = senderName;
        this.isUser = isUser;
        this.color = color;
        this.timestamp = System.currentTimeMillis();
    }

    public String getContent() {
        return content;
    }

    public String getSenderName() {
        return senderName;
    }

    public boolean isUser() {
        return isUser;
    }

    public String getColor() {
        return color;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
