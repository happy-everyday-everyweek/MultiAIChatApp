# 麦麦聊天室 - Android原生应用

## 项目简介

这是一个集成了AI对话功能的Android原生应用，无需部署后端服务器，用户只需配置DeepSeek API Key即可使用。应用内置了麦麦的个性化AI助手，提供自然流畅的对话体验。

### 特性

- 🤖 **内置AI引擎**: 直接集成DeepSeek API，无需额外服务器
- 💬 **个性化对话**: 麦麦拥有独特的性格和对话风格
- 🎨 **精美UI**: 采用Material Design设计风格
- 📱 **原生开发**: 纯Android原生开发，性能优异
- 💾 **对话记忆**: 自动保持上下文，支持连续对话
- ⚙️ **简单配置**: 只需填入API Key即可开始使用

### 麦麦的特点

- 活泼可爱的对话风格
- 有自己的想法和情绪
- 回复简洁自然，像真实朋友
- 会适当开玩笑但不失礼貌

## 技术栈

- **语言**: Java
- **最低SDK**: Android 7.0 (API 24)
- **目标SDK**: Android 14 (API 34)
- **AI模型**: DeepSeek Chat
- **网络库**: OkHttp 4.12.0
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
│   │       │   ├── SettingsActivity.java      # 设置界面
│   │       │   ├── DeepSeekClient.java        # AI客户端
│   │       │   ├── ChatMessage.java           # 消息模型
│   │       │   └── ChatAdapter.java           # 聊天适配器
│   │       ├── res/
│   │       │   ├── layout/                    # 布局文件
│   │       │   ├── drawable/                  # 图形资源
│   │       │   ├── menu/                      # 菜单资源
│   │       │   └── values/                    # 配置资源
│   │       └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── README.md
```

## 使用说明

### 1. 获取DeepSeek API Key

1. 访问 [DeepSeek开放平台](https://platform.deepseek.com)
2. 注册并登录账号
3. 在控制台创建API Key
4. 复制生成的API Key（格式：sk-xxxxxxxxxxxxxxxx）

### 2. 配置应用

1. 首次打开应用会提示配置API Key
2. 点击"去设置"或通过菜单进入设置页面
3. 填入API Key
4. （可选）修改API地址和模型名称
5. 点击"保存设置"

### 3. 开始聊天

- 配置完成后返回主界面
- 在底部输入框输入消息
- 点击发送按钮
- 等待麦麦回复

### 4. 其他功能

- **清空聊天**: 菜单 → 清空聊天
- **修改设置**: 菜单 → 设置

## 构建说明

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 8 或更高版本
- Android SDK 34

### 构建步骤

1. 克隆项目到本地
   ```bash
   git clone https://github.com/happy-everyday-everyweek/MultiAIChatApp.git
   cd MultiAIChatApp
   ```

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

## API配置说明

### 默认配置

- **API地址**: https://api.deepseek.com
- **模型名称**: deepseek-chat
- **温度**: 0.8（控制回复的创造性）
- **最大Token**: 2000

### 自定义配置

如果需要使用其他兼容OpenAI格式的API，可以在设置中修改：

1. **API地址**: 修改为你的API服务地址
2. **模型名称**: 修改为对应的模型名称
3. **API Key**: 填入对应服务的密钥

支持的API服务：
- DeepSeek
- OpenAI
- 其他兼容OpenAI格式的API服务

## 对话记忆机制

应用会自动保存最近20轮对话作为上下文，确保：
- AI能记住之前的对话内容
- 对话更加连贯自然
- 不会因为历史过长导致API调用失败

清空聊天后会重置对话历史。

## 费用说明

- 应用本身完全免费
- 使用DeepSeek API会产生费用
- DeepSeek价格：约￥1/百万tokens（非常便宜）
- 新用户通常有免费额度

## 常见问题

### 1. API Key无效

- 检查API Key是否正确复制
- 确认API Key未过期
- 检查账户余额是否充足

### 2. 网络请求失败

- 检查手机网络连接
- 确认API地址正确
- 检查防火墙设置

### 3. AI回复很慢

- DeepSeek API响应时间通常在2-5秒
- 网络状况会影响速度
- 复杂问题可能需要更长时间

### 4. 回复内容不符合预期

- 尝试清空聊天重新开始
- 调整提问方式
- 检查是否使用了正确的模型

## 隐私说明

- 应用不会收集任何用户数据
- API Key仅保存在本地设备
- 对话内容仅发送到DeepSeek服务器
- 不会上传到其他第三方服务器

## 功能扩展

当前版本支持的功能：
- ✅ DeepSeek API集成
- ✅ 对话上下文管理
- ✅ 个性化AI助手
- ✅ 设置界面
- ✅ 清空聊天

可以扩展的功能：
- [ ] 多个AI角色切换
- [ ] 对话历史本地保存
- [ ] 语音输入输出
- [ ] 图片识别（多模态）
- [ ] 自定义AI性格
- [ ] 对话导出
- [ ] 主题切换
- [ ] 消息搜索

## 基于项目

本项目灵感来源于 [MaiBot](https://github.com/Mai-with-u/MaiBot)，将其核心AI对话功能封装为独立的Android应用。

## 开源协议

GPL-3.0 License

## 贡献

欢迎提交Issue和Pull Request！

## 作者

基于MaiBot项目开发的Android原生AI聊天应用。

---

**享受与麦麦的对话吧！** 🎉
