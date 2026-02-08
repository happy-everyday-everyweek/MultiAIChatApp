# 麦麦群聊室 - Android原生应用

## 项目简介

这是一个创新的多AI群聊应用，使用**Chaquopy**在Android中运行Python，实现了多个AI机器人实例的群聊功能。用户可以同时与多个不同性格的AI对话，体验真正的AI群聊。

### 核心特性

- 🤖 **多AI实例**: 支持1-5个AI机器人同时在线
- 🎭 **个性化角色**: 每个Bot都有独特的性格和对话风格
- 🐍 **Python驱动**: 使用Chaquopy在Android中运行Python代码
- 💬 **群聊体验**: 一条消息，多个AI同时回复
- ⚙️ **灵活配置**: 可自定义Bot数量和API设置
- 💾 **上下文记忆**: 每个Bot独立维护对话历史

### AI角色介绍

1. **麦麦** - 活泼可爱的AI助手（粉色）
2. **小智** - 理性严谨的技术专家（蓝色）
3. **诗诗** - 温柔文艺的诗人（紫色）
4. **阿乐** - 幽默风趣的段子手（橙色）
5. **小月** - 温暖贴心的倾听者（粉红）

## 技术架构

```
用户输入
    ↓
Android UI (Java)
    ↓
MaiBotManager (Java)
    ↓
maibot_bridge.py (Python)
    ↓
多个MaiBotInstance (Python)
    ↓
DeepSeek API (云端)
```

## 技术栈

- **Android**: Java + Material Design
- **Python环境**: Chaquopy 14.0.2
- **Python版本**: 3.8
- **AI服务**: DeepSeek API
- **最低SDK**: Android 7.0 (API 24)
- **目标SDK**: Android 14 (API 34)

## 核心依赖

### Android依赖
- AndroidX AppCompat
- Material Components
- RecyclerView
- OkHttp
- Gson

### Python依赖（通过Chaquopy自动安装）
- httpx (异步HTTP客户端)
- asyncio (异步IO)

## 使用说明

### 1. 获取DeepSeek API Key

1. 访问 [DeepSeek开放平台](https://platform.deepseek.com)
2. 注册并登录账号
3. 创建API Key
4. 复制生成的API Key

### 2. 配置应用

1. 首次打开应用会显示欢迎对话框
2. 点击"去设置"
3. 填入API Key
4. 选择Bot数量（1-5个）
5. 点击保存设置
6. 返回主界面

### 3. 开始群聊

- 应用会自动初始化所有Bot
- 输入消息后，所有Bot会同时思考
- 每个Bot会根据自己的性格给出不同的回复
- 享受多AI群聊的乐趣！

## 构建说明

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- 稳定的网络连接（首次构建需下载Python环境）

### 构建步骤

1. 克隆项目
   ```bash
   git clone https://github.com/happy-everyday-everyweek/MultiAIChatApp.git
   cd MultiAIChatApp
   ```

2. 使用Android Studio打开项目

3. 等待Gradle同步（首次会下载Chaquopy和Python环境，需要较长时间）

4. 连接设备或启动模拟器

5. 运行应用

### 命令行构建

```bash
# 构建Debug版本
./gradlew assembleDebug

# 构建Release版本
./gradlew assembleRelease
```

**注意**: 首次构建会下载约100MB的Python运行环境，请耐心等待。

## APK大小说明

由于集成了Python运行环境，APK大小约为：
- Debug版本: ~50MB
- Release版本: ~40MB

这是正常的，因为包含了：
- Python 3.8解释器
- 必要的Python标准库
- httpx等第三方库

## 项目结构

```
MultiAIChatApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/maibot/multichat/
│   │   │   ├── MainActivity.java          # 主界面
│   │   │   ├── SettingsActivity.java      # 设置界面
│   │   │   ├── MaiBotManager.java         # Bot管理器
│   │   │   ├── ChatMessage.java           # 消息模型
│   │   │   └── ChatAdapter.java           # 列表适配器
│   │   ├── python/
│   │   │   └── maibot_bridge.py           # Python桥接层
│   │   └── res/                            # 资源文件
│   └── build.gradle                        # 应用构建配置
├── build.gradle                            # 项目构建配置
└── README.md
```

## 工作原理

### 1. Python环境初始化
应用启动时，Chaquopy会初始化Python环境，加载`maibot_bridge.py`模块。

### 2. Bot实例创建
根据用户设置的数量，创建多个`MaiBotInstance`对象，每个都有：
- 独立的对话历史
- 独特的性格设定
- 独立的API调用

### 3. 消息处理流程
```
用户发送消息
    ↓
Java层接收
    ↓
调用Python的send_message()
    ↓
Python并发调用所有Bot
    ↓
每个Bot调用DeepSeek API
    ↓
收集所有回复
    ↓
返回Java层
    ↓
UI显示所有回复
```

## 性能优化

- ✅ 异步并发调用所有Bot（不是串行）
- ✅ 对话历史限制（每个Bot最多20轮）
- ✅ 单线程执行器避免竞态
- ✅ 错误隔离（单个Bot失败不影响其他）

## 费用说明

- **应用本身**: 完全免费开源
- **DeepSeek API**: 约￥1/百万tokens
- **单次群聊**: 约￥0.001-0.005（取决于Bot数量）
- **新用户**: 通常有免费额度

**建议**: 从2-3个Bot开始，既能体验群聊，又不会消耗太多额度。

## 常见问题

### 1. 首次构建很慢
这是正常的，Chaquopy需要下载Python环境（约100MB）。

### 2. APK很大
集成Python环境导致的，这是必要的开销。

### 3. Bot回复很慢
- 多个Bot并发调用API需要时间
- 网络状况会影响速度
- Bot数量越多，整体耗时越长

### 4. 初始化失败
- 检查API Key是否正确
- 确认网络连接正常
- 查看错误提示信息

## 技术限制

由于使用Chaquopy，存在以下限制：
- APK体积较大（40-50MB）
- 首次启动较慢（初始化Python环境）
- 不支持所有Python库（仅支持纯Python库）
- 内存占用较高

## 未来计划

- [ ] 支持自定义Bot性格
- [ ] 添加Bot头像
- [ ] 支持语音输入
- [ ] 对话历史本地保存
- [ ] 支持更多AI模型
- [ ] Bot之间的互动对话

## 开源协议

GPL-3.0 License

## 致谢

- **Chaquopy**: 让Python在Android中运行成为可能
- **DeepSeek**: 提供优质的AI服务
- **MaiBot项目**: 提供灵感和参考

## 联系方式

- **GitHub**: https://github.com/happy-everyday-everyweek/MultiAIChatApp
- **Issues**: 欢迎提交问题和建议

---

**享受多AI群聊的乐趣吧！** 🎉
