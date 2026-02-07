# 多AI聊天室 - Android原生应用

## 项目简介

这是一个基于MaiBot项目改造的Android原生应用，实现了一个用户与多个AI机器人的群聊功能。

### 特性

- 🤖 **多AI机器人**: 内置4个不同性格的AI机器人（麦麦、小智、诗诗、阿乐）
- 💬 **实时聊天**: 流畅的聊天体验，AI机器人会随机响应用户消息
- 🎨 **精美UI**: 采用Material Design设计风格
- 📱 **原生开发**: 纯Android原生开发，无任何第三方框架
- 🎭 **个性化**: 每个AI都有独特的性格和回复风格

### AI机器人介绍

1. **麦麦** - 活泼可爱的AI助手（粉色）
2. **小智** - 擅长技术问题解答（蓝色）
3. **诗诗** - 喜欢文学和艺术（紫色）
4. **阿乐** - 幽默风趣的段子手（橙色）

## 技术栈

- **语言**: Java
- **最低SDK**: Android 7.0 (API 24)
- **目标SDK**: Android 14 (API 34)
- **依赖库**:
  - AndroidX AppCompat
  - Material Components
  - RecyclerView
  - OkHttp (预留网络功能)
  - Gson (预留数据解析)

## 项目结构

```
MultiAIChatApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/maibot/multichat/
│   │       │   ├── MainActivity.java          # 主界面
│   │       │   ├── ChatMessage.java           # 消息模型
│   │       │   ├── AIBot.java                 # AI机器人类
│   │       │   ├── AIBotManager.java          # AI管理器
│   │       │   └── ChatAdapter.java           # 聊天适配器
│   │       ├── res/
│   │       │   ├── layout/                    # 布局文件
│   │       │   ├── drawable/                  # 图形资源
│   │       │   └── values/                    # 配置资源
│   │       └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── README.md
```

## 构建说明

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 8 或更高版本
- Android SDK 34

### 构建步骤

1. 克隆项目到本地
2. 使用Android Studio打开项目
3. 等待Gradle同步完成
4. 连接Android设备或启动模拟器
5. 点击运行按钮

### 命令行构建

```bash
# 构建Debug版本
./gradlew assembleDebug

# 构建Release版本
./gradlew assembleRelease

# 安装到设备
./gradlew installDebug
```

## 使用说明

1. 启动应用后会看到欢迎消息
2. 在底部输入框输入消息
3. 点击发送按钮
4. AI机器人会随机响应（70%概率）
5. 每个AI的回复会有不同的延迟（0.5-2.5秒）

## 功能扩展

当前版本是基础版本，可以扩展以下功能：

- [ ] 接入真实的大语言模型API
- [ ] 添加语音输入功能
- [ ] 支持图片和表情包
- [ ] 消息持久化存储
- [ ] 自定义AI机器人
- [ ] 群聊设置和管理
- [ ] 主题切换

## 基于项目

本项目基于 [MaiBot](https://github.com/Mai-with-u/MaiBot) 项目改造而来。

## 开源协议

GPL-3.0 License

## 作者

改造自MaiBot项目，适配为Android原生应用。
