# 部署说明

## Git仓库已创建

仓库地址: https://github.com/happy-everyday-everyweek/MultiAIChatApp

## 推送代码

由于网络问题，请手动推送代码：

```bash
cd MultiAIChatApp
git push origin master
```

如果推送失败，可以尝试：

1. 检查网络连接
2. 配置代理（如果需要）
3. 使用SSH方式推送

## 项目说明

这是一个Android原生应用，通过WebSocket连接到MaiBot后端服务器，实现多AI群聊功能。

### 主要文件

- `MainActivity.java` - 主界面，处理UI和WebSocket连接
- `WebSocketManager.java` - WebSocket管理器，处理与服务器的通信
- `ChatMessage.java` - 消息数据模型
- `ChatAdapter.java` - RecyclerView适配器

### 使用前配置

1. 部署MaiBot后端服务器
2. 修改`MainActivity.java`中的`SERVER_URL`为你的服务器地址
3. 构建并安装应用

详细说明请查看 README.md
