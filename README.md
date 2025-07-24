# 圣经闹钟 Android 应用

一款专为Android 15系统设计的宗教主题闹钟应用，每天自动从诗篇1-150篇中随机选择一篇作为闹钟铃声。

## 📱 应用特色

### 核心功能
- **每日随机诗篇选择**：自动从诗篇1-150篇中随机选择当日闹钟铃声
- **音频资源库管理**：支持用户已下载的音频资源库，可播放和管理诗篇音频
- **标准闹钟功能**：支持多个闹钟设置、重复模式、贪睡功能、音量控制
- **Android 15权限适配**：完全适配最新的权限管理和后台运行限制
- **简洁宗教风格界面**：主屏幕显示当日诗篇编号、播放控制按钮和闹钟设置

### 设计特色
- 采用简洁宗教风格，米白色主背景配深棕色文字
- 金色强调元素，营造温和庄重的宗教氛围
- 界面使用圆润矩形设计，注重留白和信息层次
- Material Design 3 设计规范

## 🛠️ 技术架构

### 开发环境
- **开发语言**：Kotlin
- **目标平台**：Android 15 (API Level 35)
- **最低支持**：Android 8.0 (API Level 26)
- **架构模式**：MVVM + Repository Pattern

### 核心技术栈
- **UI框架**：Jetpack Compose + Material Design 3
- **数据库**：Room Database
- **后台任务**：WorkManager
- **音频播放**：MediaPlayer/ExoPlayer
- **闹钟管理**：AlarmManager
- **依赖注入**：Hilt
- **异步处理**：Kotlin Coroutines
- **导航**：Jetpack Navigation Compose

### 项目结构
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/biblealarm/app/
│   │   │   ├── data/           # 数据层
│   │   │   │   ├── database/   # 数据库相关
│   │   │   │   ├── model/      # 数据模型
│   │   │   │   └── repository/ # 数据仓库
│   │   │   ├── di/             # 依赖注入
│   │   │   ├── manager/        # 业务管理器
│   │   │   ├── service/        # 后台服务
│   │   │   ├── receiver/       # 广播接收器
│   │   │   ├── worker/         # 后台任务
│   │   │   └── ui/             # UI层
│   │   │       ├── components/ # 通用组件
│   │   │       ├── home/       # 主页面
│   │   │       ├── alarms/     # 闹钟管理
│   │   │       ├── settings/   # 设置页面
│   │   │       └── theme/      # 主题样式
│   │   └── res/                # 资源文件
│   ├── test/                   # 单元测试
│   └── androidTest/            # 集成测试
└── build.gradle.kts            # 构建配置
```

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 17 或更高版本
- Android SDK 35 (Android 15)
- Gradle 8.0 或更高版本

### 构建步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/your-username/bible-alarm-android.git
   cd bible-alarm-android
   ```

2. **配置签名**
   ```bash
   # 复制密钥库配置模板
   cp keystore.properties.template keystore.properties
   
   # 编辑 keystore.properties 文件，填入实际的密钥库信息
   ```

3. **同步项目**
   ```bash
   ./gradlew sync
   ```

4. **构建应用**
   ```bash
   # 调试版本
   ./gradlew assembleDebug
   
   # 发布版本
   ./gradlew assembleRelease
   ```

5. **运行测试**
   ```bash
   # 单元测试
   ./gradlew test
   
   # 集成测试
   ./gradlew connectedAndroidTest
   ```

### 安装和运行

1. **通过Android Studio运行**
   - 打开项目
   - 连接Android设备或启动模拟器
   - 点击运行按钮

2. **通过命令行安装**
   ```bash
   # 安装调试版本
   ./gradlew installDebug
   
   # 安装发布版本
   ./gradlew installRelease
   ```

## 📋 功能说明

### 主要页面

#### 主屏幕
- 显示当日选中的诗篇编号和标题
- 音频播放控制（播放/暂停、上一篇/下一篇）
- 音量控制滑块和静音按钮
- 下一个闹钟信息显示
- 快捷操作按钮

#### 闹钟管理
- 闹钟列表显示和管理
- 添加/编辑闹钟功能
- 重复设置（每日、工作日、周末、自定义）
- 贪睡模式配置
- 音量和振动设置

#### 设置页面
- 音频资源路径配置
- 默认音量和渐强播放设置
- 贪睡时长和振动开关
- 每日诗篇自动选择配置
- 权限管理和系统设置
- 应用信息和反馈

### 核心功能详解

#### 诗篇随机选择算法
- 基于日期的确定性随机算法
- 确保同一天多次调用返回相同诗篇
- 支持手动切换上一篇/下一篇
- 自动排除不可用的音频文件

#### 闹钟系统
- 支持精确闹钟调度（Android 15适配）
- 多重复模式支持
- 贪睡功能和自定义时长
- 音量渐强播放
- 振动模式支持

#### 权限管理
- Android 15权限完全适配
- 精确闹钟权限检查和引导
- 通知权限管理
- 电池优化白名单引导
- 权限状态实时监控

## 🧪 测试

### 测试覆盖
- **单元测试**：核心业务逻辑测试
- **集成测试**：UI交互和数据流测试
- **权限测试**：Android 15权限适配验证
- **性能测试**：内存使用和响应时间测试

### 运行测试
```bash
# 运行所有测试
./gradlew test connectedAndroidTest

# 生成测试报告
./gradlew jacocoTestReport
```

## 📦 构建和发布

### 构建类型

#### Debug构建
- 包含调试信息
- 启用日志输出
- 使用调试签名

#### Release构建
- 代码混淆和优化
- 移除调试信息
- 使用发布签名
- APK大小优化

### 发布流程

1. **版本号更新**
   ```kotlin
   // 在 app/build.gradle.kts 中更新
   versionCode = 1
   versionName = "1.0.0"
   ```

2. **构建发布版本**
   ```bash
   ./gradlew assembleRelease
   ```

3. **签名验证**
   ```bash
   # 验证APK签名
   jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
   ```

4. **上传到应用商店**
   - Google Play Store
   - 其他Android应用市场

## 🔧 配置说明

### 音频资源配置
- 支持MP3、WAV、M4A格式
- 建议音频文件命名格式：`psalm_[编号].mp3`
- 音频文件路径可在设置中配置
- 支持外部存储和应用内资源

### 权限配置
应用需要以下权限：
- `SCHEDULE_EXACT_ALARM`：精确闹钟调度
- `USE_EXACT_ALARM`：使用精确闹钟
- `POST_NOTIFICATIONS`：发送通知
- `WAKE_LOCK`：唤醒设备
- `VIBRATE`：振动功能
- `READ_EXTERNAL_STORAGE`：读取音频文件

### 数据库配置
- 使用Room数据库
- 支持数据库迁移
- 自动备份和恢复
- 数据加密（可选）

## 🐛 故障排除

### 常见问题

#### 闹钟不响
1. 检查精确闹钟权限是否授予
2. 确认应用未被电池优化限制
3. 验证音频文件路径是否正确
4. 检查系统音量设置

#### 音频播放失败
1. 确认音频文件格式支持
2. 检查文件路径和权限
3. 验证存储空间是否充足
4. 重启应用重新加载资源

#### 权限问题
1. 手动检查应用权限设置
2. 重新安装应用
3. 清除应用数据后重新配置
4. 检查Android版本兼容性

### 日志调试
```bash
# 查看应用日志
adb logcat | grep BibleAlarm

# 清除日志
adb logcat -c
```

## 🤝 贡献指南

### 开发规范
- 遵循Kotlin编码规范
- 使用Jetpack Compose最佳实践
- 编写单元测试和集成测试
- 提交前运行代码检查

### 提交流程
1. Fork项目
2. 创建功能分支
3. 提交代码变更
4. 编写测试用例
5. 提交Pull Request

### 代码审查
- 代码质量检查
- 功能测试验证
- 性能影响评估
- 兼容性测试

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

- **项目主页**：https://github.com/your-username/bible-alarm-android
- **问题反馈**：https://github.com/your-username/bible-alarm-android/issues
- **邮箱联系**：feedback@biblealarm.com

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者和用户。

---

**注意**：本应用仅供学习和个人使用，请遵守相关法律法规和版权要求。