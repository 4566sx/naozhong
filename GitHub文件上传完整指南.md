# 📁 GitHub 文件上传完整指南

## 🎯 问题说明
GitHub 网页界面不能直接上传文件夹，只能上传单个文件。但我们有多种解决方案！

## 🚀 解决方案（按推荐程度排序）

### 方案1：GitHub Desktop（最推荐）⭐⭐⭐⭐⭐

#### 优点：
- ✅ 图形界面，操作简单
- ✅ 自动处理文件夹结构
- ✅ 支持大文件上传
- ✅ 可以批量操作

#### 步骤：
1. **下载安装 GitHub Desktop**
   - 访问：https://desktop.github.com
   - 下载并安装到电脑

2. **登录 GitHub 账户**
   - 打开 GitHub Desktop
   - 点击 "Sign in to GitHub.com"
   - 输入用户名和密码

3. **克隆仓库**
   - 点击 "Clone a repository from the Internet"
   - 选择您创建的 `bible-psalm-alarm` 仓库
   - 选择本地保存位置，点击 "Clone"

4. **复制项目文件**
   - 打开本地仓库文件夹
   - 将所有项目文件复制到此文件夹
   - 保持原有的文件夹结构

5. **提交并推送**
   - 回到 GitHub Desktop
   - 会自动检测到新文件
   - 在左下角输入提交信息："添加圣经诗篇闹钟应用"
   - 点击 "Commit to main"
   - 点击 "Push origin" 上传到 GitHub

### 方案2：逐个创建文件 ⭐⭐⭐⭐

#### 适用场景：
- 不想安装额外软件
- 文件数量不多
- 网络环境良好

#### 详细步骤：

**1. 创建根目录文件**
```
AndroidManifest.xml
MainActivity.java
AlarmActivity.java
AlarmReceiver.java
AlarmService.java
BootReceiver.java
AlarmUtils.java
PsalmManager.java
build.gradle
proguard-rules.pro
README.md
```

**2. 创建 .github 目录文件**
- 文件名：`.github/workflows/build-apk.yml`
- GitHub 会自动创建目录结构

**3. 创建 res 目录文件**
按以下顺序创建：
```
res/layout/activity_main.xml
res/layout/activity_alarm.xml
res/values/strings.xml
res/values/colors.xml
res/values/styles.xml
res/drawable/button_stop.xml
res/drawable/button_snooze.xml
res/drawable/ic_alarm.xml
```

**4. 上传音频文件**
```
res/raw/default_psalm.mp3
res/raw/psalm_001.mp3
res/raw/psalm_002.mp3
... (逐个上传所有音频文件)
```

#### 操作技巧：
- 每次创建文件时，在文件名中包含完整路径
- 例如：`res/layout/activity_main.xml`
- GitHub 会自动创建 `res` 和 `layout` 文件夹

### 方案3：压缩包上传 ⭐⭐⭐

#### 步骤：
1. **创建项目压缩包**
   - 选择所有项目文件和文件夹
   - 右键 → "发送到" → "压缩文件夹"
   - 或使用 WinRAR/7-Zip 创建 ZIP 文件

2. **上传到 GitHub**
   - 在 GitHub 仓库点击 "Upload files"
   - 拖拽 ZIP 文件到上传区域
   - 等待上传完成

3. **解压文件**
   - GitHub 不会自动解压
   - 需要手动下载 ZIP 文件
   - 在本地解压后重新上传文件

#### 注意事项：
- ⚠️ GitHub 不会自动解压 ZIP 文件
- ⚠️ 需要额外步骤处理
- ⚠️ 不推荐用于大量文件

### 方案4：Git 命令行 ⭐⭐⭐⭐⭐

#### 适用人群：
- 熟悉命令行操作
- 需要版本控制功能
- 处理大量文件

#### 详细步骤：

**1. 安装 Git**
- Windows：https://git-scm.com/download/win
- 安装时选择默认选项即可

**2. 配置 Git**
```bash
git config --global user.name "您的姓名"
git config --global user.email "您的邮箱"
```

**3. 克隆仓库**
```bash
# 打开命令提示符或 PowerShell
# 导航到您想要保存项目的目录
cd D:\Projects

# 克隆仓库
git clone https://github.com/您的用户名/bible-psalm-alarm.git
cd bible-psalm-alarm
```

**4. 复制文件**
```bash
# 将所有项目文件复制到当前目录
# 保持文件夹结构不变
```

**5. 提交并推送**
```bash
# 添加所有文件
git add .

# 提交更改
git commit -m "添加圣经诗篇闹钟应用源码和音频文件"

# 推送到 GitHub
git push origin main
```

## 🎵 音频文件特殊处理

### 问题：音频文件太大
- GitHub 单文件限制：100MB
- 仓库总大小建议：< 1GB

### 解决方案：

**方案A：Git LFS（推荐）**
```bash
# 安装 Git LFS
git lfs install

# 跟踪 MP3 文件
git lfs track "*.mp3"

# 添加 .gitattributes 文件
git add .gitattributes

# 添加音频文件
git add res/raw/*.mp3

# 提交
git commit -m "添加音频文件（使用 Git LFS）"

# 推送
git push origin main
```

**方案B：分批上传**
```bash
# 每次上传 10-20 个音频文件
git add res/raw/psalm_001.mp3 res/raw/psalm_002.mp3 ... res/raw/psalm_020.mp3
git commit -m "添加诗篇音频文件 1-20"
git push

# 继续上传下一批
git add res/raw/psalm_021.mp3 ... res/raw/psalm_040.mp3
git commit -m "添加诗篇音频文件 21-40"
git push
```

**方案C：外部存储**
- 将音频文件上传到云存储（如百度网盘、阿里云OSS）
- 在代码中使用 URL 链接加载音频
- 修改 `PsalmManager.java` 中的音频加载逻辑

## 🔧 常见问题解决

### 问题1：上传失败
**错误信息：** "Upload failed"
**解决方案：**
- 检查网络连接
- 文件大小是否超过限制
- 尝试分批上传

### 问题2：文件夹结构错误
**现象：** 所有文件都在根目录
**解决方案：**
- 重新创建文件时包含完整路径
- 使用 GitHub Desktop 重新上传

### 问题3：音频文件无法上传
**错误信息：** "File too large"
**解决方案：**
- 使用 Git LFS
- 压缩音频文件
- 分批上传

### 问题4：权限问题
**错误信息：** "Permission denied"
**解决方案：**
- 检查 GitHub 登录状态
- 确认仓库权限设置
- 重新生成访问令牌

## 📋 上传检查清单

上传完成后，确认以下文件结构：

```
bible-psalm-alarm/
├── .github/
│   └── workflows/
│       └── build-apk.yml
├── res/
│   ├── drawable/
│   │   ├── button_snooze.xml
│   │   ├── button_stop.xml
│   │   └── ic_alarm.xml
│   ├── layout/
│   │   ├── activity_alarm.xml
│   │   └── activity_main.xml
│   ├── raw/
│   │   ├── default_psalm.mp3
│   │   ├── psalm_001.mp3
│   │   ├── psalm_002.mp3
│   │   └── ... (所有音频文件)
│   └── values/
│       ├── colors.xml
│       ├── strings.xml
│       └── styles.xml
├── AlarmActivity.java
├── AlarmReceiver.java
├── AlarmService.java
├── AlarmUtils.java
├── AndroidManifest.xml
├── BootReceiver.java
├── build.gradle
├── MainActivity.java
├── proguard-rules.pro
├── PsalmManager.java
└── README.md
```

## 🎉 总结

**最推荐的方法：**
1. **GitHub Desktop**（最简单）
2. **Git 命令行**（最专业）
3. **逐个创建文件**（最直接）

选择适合您的方法，开始上传您的圣经诗篇闹钟项目吧！🙏

---

**遇到问题？** 
- 查看 GitHub 官方帮助：https://docs.github.com
- 或参考我们的详细教程文档