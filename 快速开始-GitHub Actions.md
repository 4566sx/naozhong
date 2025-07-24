# ⚡ 快速开始：5分钟搞定在线编译

## 🎯 超简单 3 步骤

### 步骤 1：上传到 GitHub（2分钟）
1. 访问 https://github.com ，登录或注册账户
2. 点击右上角 "+" → "New repository"
3. 仓库名：`bible-psalm-alarm`，设为 Public，点击 "Create"
4. 上传项目文件（详见下方文件夹上传方法）

#### 📁 文件夹上传解决方案

**方法A：逐个创建文件（推荐）**
1. 点击 "Create new file"
2. 文件名输入：`res/layout/activity_main.xml`
   - GitHub会自动创建 `res/layout/` 文件夹
3. 复制粘贴文件内容，点击 "Commit new file"
4. 重复创建所有文件：
   ```
   AndroidManifest.xml
   MainActivity.java
   AlarmActivity.java
   ... (所有 .java 文件)
   build.gradle
   res/layout/activity_main.xml
   res/layout/activity_alarm.xml
   res/values/strings.xml
   res/values/colors.xml
   res/values/styles.xml
   res/drawable/button_stop.xml
   res/drawable/button_snooze.xml
   res/drawable/ic_alarm.xml
   .github/workflows/build-apk.yml
   ```

**方法B：压缩包上传**
1. 将整个项目打包成 ZIP 文件
2. 在 GitHub 仓库点击 "Upload files"
3. 上传 ZIP 文件
4. GitHub 会自动解压并保持文件夹结构

**方法C：使用 GitHub Desktop（最简单）**
1. 下载安装 GitHub Desktop：https://desktop.github.com
2. 登录 GitHub 账户
3. Clone 您创建的仓库到本地
4. 将所有项目文件复制到本地仓库文件夹
5. 在 GitHub Desktop 中提交并推送所有更改

**方法D：Git 命令行**
```bash
# 1. 克隆空仓库
git clone https://github.com/您的用户名/bible-psalm-alarm.git
cd bible-psalm-alarm

# 2. 复制所有项目文件到此文件夹

# 3. 添加所有文件
git add .

# 4. 提交
git commit -m "添加圣经诗篇闹钟应用"

# 5. 推送
git push origin main
```

**⚠️ 音频文件特殊处理**
由于音频文件较大（GitHub单文件限制100MB），建议：
1. 先上传代码文件
2. 音频文件分批上传，每次上传10-20个
3. 或使用 Git LFS 处理大文件：
   ```bash
   git lfs track "*.mp3"
   git add .gitattributes
   git add res/raw/*.mp3
   git commit -m "添加音频文件"
   git push
   ```

### 步骤 2：启用自动编译（1分钟）
1. 在仓库页面点击 "Actions" 标签
2. 点击 "I understand my workflows, go ahead and enable them"
3. 确认看到 "构建 Android APK" 工作流

### 步骤 3：开始编译（2分钟）
1. 点击 "构建 Android APK" 工作流
2. 点击右侧 "Run workflow" → "Run workflow"
3. 等待 5-10 分钟编译完成

## 📱 下载 APK
编译完成后：
1. 进入完成的工作流运行
2. 滚动到底部 "Artifacts" 部分
3. 下载 "圣经诗篇闹钟-debug-apk"
4. 解压 ZIP 文件得到 APK

## 🔄 后续使用
以后只需要：
- 修改任何文件并提交 → 自动编译
- 或手动点击 "Run workflow" → 手动编译

就这么简单！🎉

---

**需要详细教程？** 查看 `GitHub Actions详细教程.md`
**遇到问题？** 查看 `在线编译指南.md` 的故障排除部分