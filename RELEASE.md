# 发布说明

## 如何获取APK

### 方法1: 从GitHub Releases下载（推荐）

1. 访问 [Releases页面](https://github.com/happy-everyday-everyweek/MultiAIChatApp/releases)
2. 下载最新版本的APK文件
3. 在Android设备上安装

### 方法2: 从GitHub Actions下载

1. 访问 [Actions页面](https://github.com/happy-everyday-everyweek/MultiAIChatApp/actions)
2. 点击最新的成功构建
3. 在Artifacts部分下载APK

### 方法3: 本地构建

```bash
# 克隆仓库
git clone https://github.com/happy-everyday-everyweek/MultiAIChatApp.git
cd MultiAIChatApp

# 构建Debug版本
./gradlew assembleDebug

# 构建Release版本
./gradlew assembleRelease

# APK位置
# Debug: app/build/outputs/apk/debug/app-debug.apk
# Release: app/build/outputs/apk/release/app-release-unsigned.apk
```

## APK版本说明

### app-debug.apk
- **用途**: 开发测试版本
- **特点**: 
  - 可直接安装
  - 包含调试信息
  - 文件较大
  - 适合日常使用和测试

### app-release-unsigned.apk
- **用途**: 发布版本（未签名）
- **特点**:
  - 代码优化
  - 文件较小
  - 未签名，部分设备可能无法安装
  - 需要签名后才能发布到应用商店

## 安装说明

### Android设备安装

1. **下载APK文件**
   - 使用浏览器下载到手机
   - 或通过电脑下载后传输到手机

2. **允许安装未知来源应用**
   - 设置 → 安全 → 允许安装未知来源应用
   - 或在安装时根据提示允许

3. **安装APK**
   - 点击下载的APK文件
   - 按照提示完成安装

4. **首次配置**
   - 打开应用
   - 点击"去设置"
   - 填入DeepSeek API Key
   - 保存设置

### 常见问题

**Q: 提示"应用未安装"**
A: 检查是否允许安装未知来源应用，或尝试卸载旧版本后重新安装

**Q: 提示"解析包时出现问题"**
A: APK文件可能损坏，请重新下载

**Q: 无法安装Release版本**
A: 使用Debug版本，或等待签名版本发布

## 自动构建说明

本项目使用GitHub Actions自动构建APK：

### 触发条件
- 推送到master分支
- 创建新的tag（v*格式）
- Pull Request到master分支

### 构建流程
1. 检出代码
2. 设置JDK 17环境
3. 授予Gradle执行权限
4. 构建Debug和Release APK
5. 上传APK到Artifacts
6. 如果是tag推送，创建GitHub Release

### 创建新版本

```bash
# 创建tag
git tag -a v1.0.0 -m "版本 1.0.0"

# 推送tag
git push origin v1.0.0

# GitHub Actions会自动构建并创建Release
```

## 版本历史

### v1.0.0 (2026-02-07)
- ✅ 初始版本发布
- ✅ DeepSeek API集成
- ✅ 基础聊天功能
- ✅ 设置界面
- ✅ 对话上下文管理

## 下载链接

- **最新版本**: [Releases](https://github.com/happy-everyday-everyweek/MultiAIChatApp/releases/latest)
- **所有版本**: [Releases](https://github.com/happy-everyday-everyweek/MultiAIChatApp/releases)
- **构建记录**: [Actions](https://github.com/happy-everyday-everyweek/MultiAIChatApp/actions)

## 技术支持

- **问题反馈**: [Issues](https://github.com/happy-everyday-everyweek/MultiAIChatApp/issues)
- **功能建议**: [Issues](https://github.com/happy-everyday-everyweek/MultiAIChatApp/issues)
- **贡献代码**: [Pull Requests](https://github.com/happy-everyday-everyweek/MultiAIChatApp/pulls)

---

**享受与麦麦的对话吧！** 🎉
