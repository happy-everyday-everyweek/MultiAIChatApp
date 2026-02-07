# 多AI聊天室 - Android原生应用

## 项目简介

这是一个基于MaiBot项目的Android原生群聊应用，通过WebSocket连接到MaiBot后端服务器，实现一个用户与多个AI机器人的实时群聊功能。

### 特性

- 🤖 **真实AI对话**: 连接到MaiBot后端，使用真实的大语言模型
- 💬 **实时通信**: 基于WebSocket的实时双向通信
- 🎨 **精美UI**: 采用Material Design设计风格
- 📱 **原生开发**: 纯Android原生开发，无任何第三方框架
- 💾 **持久化**: 自动保存用户ID，保持聊天历史
- 🔄 **自动重连**: 支持断线重连机制

### 架构说明

```
Android App (WebSocket Client)
        ↓
MaiBot WebUI Server (WebSocket Server)
        ↓
MaiBot Core (AI处理引擎)
        ↓
大语言模型 API
```

## 技术栈

- **语言**: Java
- **最低SDK**: Android 7.0 (API 24)
- **目标SDK**: Android 14 (API 34)
- **网络库**: OkHttp 4.12.0 (WebSocket)
- **JSON解析**: Gson 2.10.1
- **依赖库**:
  - AndroidX AppCompat
  - Material Components
  - RecyclerView

## 项目结构

```
MultiAIChatApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/maibot/multichat/
│   │       │   ├── MainActivity.java          # 主界面
│   │       │   ├── ChatMessage.java           # 消息模型
│   │       │   ├── WebSocketManager.java      # WebSocket管理器
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

## 部署说明

### 前置要求

1. **MaiBot后端服务器**
   - 按照 [MaiBot部署教程](https://docs.mai-mai.org/manual/deployment/mmc_deploy_windows.html) 部署MaiBot
   - 确保WebUI服务器正常运行（默认端口8001）
   - 记录服务器IP地址

2. **Android开发环境**
   - Android Studio Hedgehog (2023.1.1) 或更高版本
   - JDK 8 或更高版本
   - Android SDK 34

### 配置步骤

1. **修改服务器地址**
   
   打开 `MainActivity.java`，修改服务器地址：
   ```java
   private static final String SERVER_URL = "ws://你的服务器IP:8001";
   ```
   
   例如：
   - 本地测试: `ws://127.0.0.1:8001`
   - 局域网: `ws://192.168.1.100:8001`
   - 公网: `ws://your-domain.com:8001`

2. **构建应用**
   ```bash
   # 使用Android Studio打开项目
   # 或使用命令行构建
   ./gradlew assembleDebug
   ```

3. **安装到设备**
   ```bash
   ./gradlew installDebug
   ```

### MaiBot服务器配置

确保MaiBot的`.env`文件中WebUI配置正确：

```env
# WebUI服务器配置
WEBUI_HOST=0.0.0.0  # 允许外部访问
WEBUI_PORT=8001
```

如果使用防火墙，需要开放8001端口：
```bash
# Linux
sudo ufw allow 8001

# Windows
netsh advfirewall firewall add rule name="MaiBot WebUI" dir=in action=allow protocol=TCP localport=8001
```

## 使用说明

1. **启动MaiBot服务器**
   ```bash
   cd MaiBot
   python bot.py
   ```

2. **启动Android应用**
   - 打开应用会自动连接到服务器
   - 首次使用会生成唯一用户ID
   - 连接成功后会加载历史消息

3. **开始聊天**
   - 在底部输入框输入消息
   - 点击发送按钮
   - AI会自动回复（可能有延迟）

## WebSocket消息格式

### 客户端发送

```json
{
  "type": "message",
  "content": "你好",
  "user_name": "Android用户"
}
```

### 服务器响应

```json
{
  "type": "bot_message",
  "content": "你好！我是麦麦~",
  "timestamp": 1234567890.123,
  "sender": {
    "name": "麦麦",
    "user_id": "bot",
    "is_bot": true
  }
}
```

## 功能扩展

当前版本支持的功能：
- [x] WebSocket实时通信
- [x] 消息收发
- [x] 历史记录加载
- [x] 用户ID持久化
- [x] 错误处理

可以扩展的功能：
- [ ] 断线自动重连
- [ ] 消息发送状态显示
- [ ] 图片和表情包支持
- [ ] 语音输入
- [ ] 多群聊切换
- [ ] 消息本地缓存
- [ ] 推送通知
- [ ] 主题切换

## 常见问题

### 1. 无法连接到服务器

- 检查服务器地址是否正确
- 确认MaiBot服务器正在运行
- 检查防火墙设置
- 确认手机和服务器在同一网络（局域网）

### 2. 连接后无响应

- 检查MaiBot的大语言模型配置
- 查看MaiBot服务器日志
- 确认API密钥配置正确

### 3. 历史消息不显示

- 首次使用没有历史消息是正常的
- 检查数据库文件是否正常

## 基于项目

本项目基于 [MaiBot](https://github.com/Mai-with-u/MaiBot) 项目，通过WebSocket API实现Android客户端。

## 开源协议

GPL-3.0 License

## 作者

基于MaiBot项目开发的Android原生客户端应用。
