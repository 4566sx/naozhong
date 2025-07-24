# 🚀 GitHub Actions 自动编译详细教程

## 📋 方案概述

GitHub Actions 是 GitHub 提供的免费 CI/CD 服务，可以自动编译您的 Android 项目并生成 APK 文件。这种方法的最大优势是：
- ✅ **完全免费**（每月2000分钟免费额度）
- ✅ **无需本地环境**（不用安装 Android Studio）
- ✅ **自动化编译**（代码更新后自动编译）
- ✅ **多平台支持**（Linux、Windows、macOS）
- ✅ **版本管理**（每次编译都有记录）

## 🎯 详细操作步骤

### 第一步：准备 GitHub 账户

1. **注册 GitHub 账户**
   - 访问 https://github.com
   - 点击 "Sign up" 注册新账户
   - 验证邮箱地址

2. **创建新仓库**
   - 登录后点击右上角的 "+" 号
   - 选择 "New repository"
   - 仓库名称：`bible-psalm-alarm`
   - 设置为 Public（公开仓库有更多免费额度）
   - 勾选 "Add a README file"
   - 点击 "Create repository"

### 第二步：上传项目文件

**方法A：通过网页界面上传**

1. **上传单个文件**
   - 在仓库主页点击 "Add file" → "Upload files"
   - 拖拽或选择以下文件：
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
     ```

2. **创建目录结构**
   - 点击 "Create new file"
   - 在文件名中输入 `res/layout/activity_main.xml`
   - GitHub 会自动创建 `res/layout/` 目录
   - 复制粘贴 `activity_main.xml` 的内容
   - 重复此步骤创建所有资源文件

3. **上传音频文件**
   - 创建 `res/raw/` 目录
   - 逐个上传所有 `psalm_*.mp3` 文件
   - 注意：GitHub 单个文件限制 100MB

**方法B：使用 Git 命令行（推荐）**

```bash
# 1. 克隆仓库到本地
git clone https://github.com/您的用户名/bible-psalm-alarm.git
cd bible-psalm-alarm

# 2. 复制所有项目文件到此目录

# 3. 添加所有文件
git add .

# 4. 提交更改
git commit -m "添加圣经诗篇闹钟应用源码"

# 5. 推送到 GitHub
git push origin main
```

### 第三步：创建 GitHub Actions 工作流

1. **创建工作流目录**
   - 在仓库中创建 `.github/workflows/` 目录
   - 注意：目录名必须完全一致，包括前面的点

2. **创建工作流文件**
   - 在 `.github/workflows/` 目录下创建 `build-apk.yml` 文件
   - 复制我们提供的工作流配置内容

3. **工作流文件详解**
   ```yaml
   name: 构建 Android APK  # 工作流名称
   
   on:  # 触发条件
     push:
       branches: [ main, master ]  # 推送到主分支时触发
     pull_request:
       branches: [ main, master ]  # 创建PR时触发
     workflow_dispatch:  # 允许手动触发
   
   jobs:  # 任务定义
     build:
       runs-on: ubuntu-latest  # 使用Ubuntu环境
       
       steps:  # 执行步骤
       - name: 检出代码
         uses: actions/checkout@v3  # 下载源码
         
       - name: 设置 JDK 11
         uses: actions/setup-java@v3  # 安装Java
         with:
           java-version: '11'
           distribution: 'temurin'
           
       # ... 其他步骤
   ```

### 第四步：启用 GitHub Actions

1. **进入 Actions 页面**
   - 在仓库主页点击 "Actions" 标签
   - 如果是第一次使用，会看到欢迎页面

2. **启用工作流**
   - 点击 "I understand my workflows, go ahead and enable them"
   - 或者点击绿色的 "Enable GitHub Actions" 按钮

3. **验证工作流文件**
   - GitHub 会自动检测 `.github/workflows/` 目录下的 YAML 文件
   - 在 Actions 页面应该能看到 "构建 Android APK" 工作流

### 第五步：触发编译

**方法A：自动触发（推荐）**
1. 对项目进行任何修改（如编辑 README.md）
2. 提交并推送更改：
   ```bash
   git add .
   git commit -m "触发自动编译"
   git push
   ```
3. GitHub Actions 会自动开始编译

**方法B：手动触发**
1. 进入 Actions 页面
2. 点击左侧的 "构建 Android APK" 工作流
3. 点击右侧的 "Run workflow" 按钮
4. 选择分支（通常是 main）
5. 点击绿色的 "Run workflow" 确认

### 第六步：监控编译过程

1. **查看运行状态**
   - 在 Actions 页面可以看到正在运行的工作流
   - 黄色圆点表示正在运行
   - 绿色勾号表示成功
   - 红色叉号表示失败

2. **查看详细日志**
   - 点击具体的工作流运行
   - 可以看到每个步骤的执行情况
   - 点击步骤名称查看详细日志

3. **编译时间**
   - 首次编译：10-15分钟（需要下载依赖）
   - 后续编译：5-8分钟（有缓存加速）

### 第七步：下载 APK 文件

1. **等待编译完成**
   - 编译成功后，工作流状态变为绿色勾号
   - 失败则显示红色叉号，需要查看日志排错

2. **下载 APK**
   - 点击进入成功的工作流运行详情
   - 滚动到页面底部的 "Artifacts" 部分
   - 点击 "圣经诗篇闹钟-debug-apk" 下载
   - 下载的是 ZIP 文件，解压后得到 APK

3. **APK 文件信息**
   - 文件名：`app-debug.apk`
   - 大小：约 50-100MB（包含音频文件）
   - 类型：Debug 版本（用于测试）

## 🔧 高级配置

### 自定义编译选项

**修改应用信息：**
```yaml
- name: 设置应用信息
  run: |
    # 修改应用名称
    sed -i 's/圣经诗篇闹钟/我的诗篇闹钟/g' res/values/strings.xml
    
    # 修改包名
    sed -i 's/com.biblealarm.app/com.myapp.psalmalarm/g' AndroidManifest.xml
```

**生成发布版本：**
```yaml
- name: 构建 Release APK
  run: ./gradlew assembleRelease
  
- name: 签名 APK
  uses: r0adkll/sign-android-release@v1
  with:
    releaseDirectory: app/build/outputs/apk/release
    signingKeyBase64: ${{ secrets.SIGNING_KEY }}
    alias: ${{ secrets.ALIAS }}
    keyStorePassword: ${{ secrets.KEY_STORE_PASSWORD }}
    keyPassword: ${{ secrets.KEY_PASSWORD }}
```

### 多版本编译

**同时编译多个版本：**
```yaml
strategy:
  matrix:
    api-level: [21, 28, 34]
    
steps:
- name: 构建 API ${{ matrix.api-level }}
  run: |
    sed -i 's/targetSdkVersion 34/targetSdkVersion ${{ matrix.api-level }}/g' build.gradle
    ./gradlew assembleDebug
```

### 自动发布

**创建 GitHub Release：**
```yaml
- name: 创建发布版本
  if: startsWith(github.ref, 'refs/tags/')
  uses: actions/create-release@v1
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
  with:
    tag_name: ${{ github.ref }}
    release_name: 圣经诗篇闹钟 ${{ github.ref }}
    draft: false
    prerelease: false
```

## 🛠️ 故障排除

### 常见错误及解决方案

**错误1：Gradle 构建失败**
```
Error: Could not find or load main class org.gradle.wrapper.GradleWrapperMain
```
**解决方案：**
```yaml
- name: 创建 Gradle Wrapper
  run: gradle wrapper
```

**错误2：SDK 许可证问题**
```
Error: Failed to install the following Android SDK packages as some licences have not been accepted
```
**解决方案：**
```yaml
- name: 接受 SDK 许可证
  run: yes | sdkmanager --licenses
```

**错误3：内存不足**
```
Error: Java heap space
```
**解决方案：**
```yaml
- name: 设置 Gradle 选项
  run: |
    echo "org.gradle.jvmargs=-Xmx4g -XX:MaxPermSize=512m" >> gradle.properties
```

**错误4：文件路径问题**
```
Error: AndroidManifest.xml not found
```
**解决方案：**
确保文件结构正确：
```
项目根目录/
├── .github/workflows/build-apk.yml
├── AndroidManifest.xml
├── build.gradle
├── *.java
└── res/
    ├── layout/
    ├── values/
    ├── drawable/
    └── raw/
```

### 调试技巧

**1. 查看文件结构**
```yaml
- name: 列出文件
  run: |
    echo "=== 根目录文件 ==="
    ls -la
    echo "=== res 目录 ==="
    find res -type f | head -20
```

**2. 检查 Gradle 配置**
```yaml
- name: 验证 Gradle 配置
  run: |
    ./gradlew tasks --all
    ./gradlew dependencies
```

**3. 保存构建日志**
```yaml
- name: 上传构建日志
  if: failure()
  uses: actions/upload-artifact@v3
  with:
    name: build-logs
    path: |
      build/reports/
      app/build/reports/
```

## 📊 使用限制和配额

### GitHub Actions 免费额度
- **公开仓库**：无限制使用
- **私有仓库**：每月 2000 分钟
- **存储空间**：500MB artifacts 存储
- **并发任务**：最多 20 个

### 优化建议
1. **使用缓存**减少编译时间
2. **清理旧的 artifacts**节省存储空间
3. **合理设置触发条件**避免不必要的编译
4. **使用 matrix 策略**并行编译多个版本

## 🎉 总结

GitHub Actions 自动编译方案的优势：

✅ **零成本**：完全免费使用
✅ **零配置**：无需本地开发环境
✅ **自动化**：代码更新自动编译
✅ **可靠性**：GitHub 提供的稳定服务
✅ **可扩展**：支持复杂的 CI/CD 流程
✅ **版本控制**：每次编译都有完整记录

这种方案特别适合：
- 不想安装 Android Studio 的用户
- 需要自动化编译的团队项目
- 想要版本管理的个人开发者
- 需要多平台编译的项目

按照以上步骤，您就可以轻松实现在线自动编译您的圣经诗篇闹钟应用了！🙏