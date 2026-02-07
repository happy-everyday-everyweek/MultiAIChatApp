package com.maibot.multichat;

import java.util.Random;

public class AIBot {
    private String name;
    private String personality;
    private String color;
    private Random random;

    public AIBot(String name, String personality, String color) {
        this.name = name;
        this.personality = personality;
        this.color = color;
        this.random = new Random();
    }

    public String getName() {
        return name;
    }

    public String getPersonality() {
        return personality;
    }

    public String getColor() {
        return color;
    }

    public String generateResponse(String userMessage) {
        String[] responses = getResponsesByPersonality(userMessage);
        return responses[random.nextInt(responses.length)];
    }

    private String[] getResponsesByPersonality(String userMessage) {
        switch (name) {
            case "麦麦":
                return new String[]{
                    "哇！" + userMessage + "，这个话题好有趣呀~",
                    "嘿嘿，让我想想...关于" + userMessage + "，我有很多想法呢！",
                    "哎呀，你说的" + userMessage + "真的很棒！",
                    "emmm...关于这个，我觉得超级有意思！"
                };
            case "小智":
                return new String[]{
                    "从技术角度来看，" + userMessage + "确实值得深入探讨。",
                    "让我分析一下：" + userMessage + "涉及到多个层面的问题。",
                    "关于" + userMessage + "，我建议可以这样理解...",
                    "这是个好问题！" + userMessage + "的核心在于..."
                };
            case "诗诗":
                return new String[]{
                    userMessage + "...多么富有诗意的表达啊~",
                    "你的话让我想起了一句诗：" + userMessage + "如诗如画。",
                    "从艺术的角度，" + userMessage + "充满了美感。",
                    "真美！" + userMessage + "让我感受到了文字的力量。"
                };
            case "阿乐":
                return new String[]{
                    "哈哈哈！" + userMessage + "？这让我想起一个笑话...",
                    "你知道吗？" + userMessage + "其实挺搞笑的，哈哈！",
                    "嘿！关于" + userMessage + "，我有个段子要讲~",
                    "笑死我了！" + userMessage + "真是太逗了！"
                };
            default:
                return new String[]{"收到：" + userMessage};
        }
    }

    public boolean shouldRespond(String message) {
        return random.nextInt(100) < 70;
    }
}
