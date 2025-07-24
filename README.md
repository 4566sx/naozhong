# 圣经诗篇闹钟应用

## 应用简介

这是一个安卓闹钟应用，专门设计用于播放圣经诗篇的华语有声戏剧版录音作为闹钟铃声。每天会随机选择一篇诗篇进行播放，让用户在美好的圣经话语中开始新的一天。

## 主要功能

### 🔔 智能闹钟
- 支持设置每日定时闹钟
- 自动在设备重启后恢复闹钟设置
- 支持贪睡功能（5分钟）
- 锁屏状态下也能正常响铃

### 📖 每日诗篇
- 基于日期的智能算法，每天随机选择一篇诗篇
- 同一天总是播放相同的诗篇，确保一致性
- 涵盖诗篇1-150篇的完整内容
- 显示诗篇文本内容供用户阅读

### 🎵 音频播放
- 播放华语有声戏剧版诗篇录音
- 高质量音频体验
- 循环播放直到用户停止
- 支持后台播放

### 🎨 用户界面
- 简洁美观的Material Design设计
- 深色主题的闹钟响铃界面
- 卡片式布局，信息层次清晰
- 响应式设计，适配不同屏幕尺寸

## 技术特性

### 权限管理
- `WAKE_LOCK`: 保持设备唤醒状态
- `VIBRATE`: 振动提醒
- `RECEIVE_BOOT_COMPLETED`: 开机自启动
- `SCHEDULE_EXACT_ALARM`: 精确定时闹钟
- `FOREGROUND_SERVICE`: 前台服务
- `POST_NOTIFICATIONS`: 通知权限

### 核心组件
- **MainActivity**: 主界面，设置和管理闹钟
- **AlarmActivity**: 闹钟响铃界面
- **AlarmReceiver**: 广播接收器，处理闹钟触发
- **AlarmService**: 前台服务，确保闹钟可靠运行
- **BootReceiver**: 开机广播接收器
- **PsalmManager**: 诗篇管理器，处理诗篇选择和内容
- **AlarmUtils**: 闹钟工具类

## 文件结构

```
├── AndroidManifest.xml          # 应用清单文件
├── MainActivity.java            # 主活动
├── AlarmActivity.java           # 闹钟响铃活动
├── AlarmReceiver.java           # 闹钟广播接收器
├── AlarmService.java            # 闹钟前台服务
├── BootReceiver.java            # 开机广播接收器
├── AlarmUtils.java              # 闹钟工具类
├── PsalmManager.java            # 诗篇管理器
├── build.gradle                 # 构建配置
├── res/
│   ├── layout/
│   │   ├── activity_main.xml    # 主界面布局
│   │   └── activity_alarm.xml   # 闹钟界面布局
│   ├── drawable/
│   │   ├── button_stop.xml      # 停止按钮样式
│   │   ├── button_snooze.xml    # 贪睡按钮样式
│   │   └── ic_alarm.xml         # 闹钟图标
│   └── values/
│       ├── strings.xml          # 字符串资源
│       ├── colors.xml           # 颜色资源
│       └── styles.xml           # 样式资源
└── README.md                    # 说明文档
```

## 📱 手机安装指南

### 系统要求
- Android 5.0 (API 21) 及以上版本
- 至少100MB存储空间（用于应用和音频文件）
- 支持MP3音频播放

### 方法一：在线编译（最简单，推荐）

#### 🌐 GitHub Actions 自动编译
1. **Fork 项目到您的 GitHub 账户**
2. **启用 GitHub Actions**：
   - 进入您 fork 的仓库
   - 点击 `Actions` 标签页
   - 点击 `I understand my workflows, go ahead and enable them`

3. **触发自动编译**：
   - 对项目进行任何修改（如编辑 README.md）
   - 提交更改，GitHub Actions 将自动开始编译
   - 编译完成后，在 `Actions` 页面下载生成的 APK

#### 🔧 在线 IDE 编译

**选项1：Gitpod（推荐）**
```bash
# 1. 访问：https://gitpod.io/#https://github.com/你的用户名/项目名
# 2. 等待环境加载完成
# 3. 在终端中运行：
./gradlew assembleDebug

# 4. APK 文件将生成在 app/build/outputs/apk/debug/ 目录
```

**选项2：Codespaces（GitHub）**
```bash
# 1. 在 GitHub 仓库页面点击绿色的 "Code" 按钮
# 2. 选择 "Codespaces" 标签
# 3. 点击 "Create codespace on main"
# 4. 等待环境启动完成
# 5. 在终端运行编译命令
```

**选项3：Repl.it**
```bash
# 1. 访问 https://replit.com
# 2. 导入 GitHub 仓库
# 3. 选择 Android 环境
# 4. 运行编译脚本
```

#### 📱 在线 APK 构建服务

**ApkOnline.com**
1. 访问 https://www.apkonline.net
2. 上传项目源码压缩包
3. 选择编译选项
4. 等待在线编译完成
5. 下载生成的 APK 文件

### 方法二：本地编译

#### 1. 准备开发环境
```bash
# 下载并安装 Android Studio
# 官网：https://developer.android.com/studio

# 安装 Java 8 或更高版本
# 确保 JAVA_HOME 环境变量已设置
```

#### 2. 编译项目
```bash
# 1. 打开 Android Studio
# 2. 选择 "Open an existing project"
# 3. 选择项目根目录（包含 build.gradle 的文件夹）
# 4. 等待 Gradle 同步完成
# 5. 连接 Android 设备或启动模拟器
# 6. 点击 "Run" 按钮或按 Shift+F10
```

#### 3. 生成 APK 文件
```bash
# 在 Android Studio 中：
# 1. 菜单栏选择 Build → Build Bundle(s) / APK(s) → Build APK(s)
# 2. 等待编译完成
# 3. APK 文件将生成在 app/build/outputs/apk/debug/ 目录下
```

#### 4. 使用命令行编译
```bash
# 运行提供的编译脚本
build_apk.bat

# 或者手动执行 Gradle 命令
./gradlew assembleDebug
```

### 方法二：直接安装 APK

#### 1. 获取 APK 文件
- 从开发者处获取编译好的 APK 文件
- 或者按照上述方法自行编译生成

#### 2. 在手机上启用未知来源安装
**Android 8.0 及以上版本：**
1. 打开 `设置` → `安全` → `更多安全设置`
2. 找到 `安装未知应用` 或 `未知来源`
3. 选择用于安装的应用（如文件管理器、浏览器）
4. 开启 `允许来自此来源的应用`

**Android 8.0 以下版本：**
1. 打开 `设置` → `安全`
2. 勾选 `未知来源` 选项
3. 在弹出的警告对话框中选择 `确定`

#### 3. 安装应用
1. 将 APK 文件传输到手机（通过 USB、蓝牙、网络等）
2. 使用文件管理器找到 APK 文件
3. 点击 APK 文件开始安装
4. 按照提示完成安装过程

#### 4. 授予必要权限
安装完成后，首次运行时需要授予以下权限：
- ✅ **闹钟权限**：允许应用设置闹钟
- ✅ **通知权限**：显示闹钟通知
- ✅ **唤醒设备权限**：在锁屏状态下唤醒设备
- ✅ **振动权限**：闹钟振动提醒
- ✅ **开机自启权限**：设备重启后自动恢复闹钟

### 🚀 使用方法

#### 首次设置
1. 打开 `圣经诗篇闹钟` 应用
2. 查看今日推荐的诗篇编号
3. 使用时间选择器设置您希望的闹钟时间
4. 点击 `设置闹钟` 按钮
5. 确认闹钟状态显示为 "闹钟已设置"

#### 日常使用
1. **闹钟响起时**：
   - 屏幕会自动点亮并显示闹钟界面
   - 开始播放当日诗篇的华语录音
   - 显示诗篇标题和文本内容

2. **停止闹钟**：
   - 点击红色的 `停止闹钟` 按钮
   - 闹钟将完全停止

3. **贪睡功能**：
   - 点击橙色的 `贪睡 5 分钟` 按钮
   - 闹钟将在 5 分钟后再次响起

4. **取消闹钟**：
   - 在主界面点击 `取消闹钟` 按钮
   - 闹钟将被完全取消

### ⚠️ 常见问题解决

#### 问题1：闹钟不响
**解决方案：**
- 检查手机是否开启了勿扰模式
- 确认应用已获得闹钟和通知权限
- 检查手机的省电模式设置
- 将应用添加到白名单，防止被系统清理

#### 问题2：锁屏状态下不显示
**解决方案：**
- 在应用权限中开启 "显示在其他应用上层"
- 开启 "锁屏显示" 权限
- 检查手机的锁屏通知设置

#### 问题3：音频无法播放
**解决方案：**
- 确认音频文件完整且未损坏
- 检查手机音量设置
- 重新安装应用

#### 问题4：开机后闹钟失效
**解决方案：**
- 开启应用的 "开机自启动" 权限
- 将应用添加到系统的自启动管理白名单
- 检查手机的后台应用管理设置
# 圣经诗篇闹钟应用

## 应用简介

这是一个安卓闹钟应用，专门设计用于播放圣经诗篇的华语有声戏剧版录音作为闹钟铃声。每天会随机选择一篇诗篇进行播放，让用户在美好的圣经话语中开始新的一天。

## 主要功能

### 🔔 智能闹钟
- 支持设置每日定时闹钟
- 自动在设备重启后恢复闹钟设置
- 支持贪睡功能（5分钟）
- 锁屏状态下也能正常响铃

### 📖 每日诗篇
- 基于日期的智能算法，每天随机选择一篇诗篇
- 同一天总是播放相同的诗篇，确保一致性
- 涵盖诗篇1-150篇的完整内容
- 显示诗篇文本内容供用户阅读

### 🎵 音频播放
- 播放华语有声戏剧版诗篇录音
- 高质量音频体验
- 循环播放直到用户停止
- 支持后台播放

### 🎨 用户界面
- 简洁美观的Material Design设计
- 深色主题的闹钟响铃界面
- 卡片式布局，信息层次清晰
- 响应式设计，适配不同屏幕尺寸

## 技术特性

### 权限管理
- `WAKE_LOCK`: 保持设备唤醒状态
- `VIBRATE`: 振动提醒
- `RECEIVE_BOOT_COMPLETED`: 开机自启动
- `SCHEDULE_EXACT_ALARM`: 精确定时闹钟
- `FOREGROUND_SERVICE`: 前台服务
- `POST_NOTIFICATIONS`: 通知权限

### 核心组件
- **MainActivity**: 主界面，设置和管理闹钟
- **AlarmActivity**: 闹钟响铃界面
- **AlarmReceiver**: 广播接收器，处理闹钟触发
- **AlarmService**: 前台服务，确保闹钟可靠运行
- **BootReceiver**: 开机广播接收器
- **PsalmManager**: 诗篇管理器，处理诗篇选择和内容
- **AlarmUtils**: 闹钟工具类

## 文件结构

```
├── AndroidManifest.xml          # 应用清单文件
├── MainActivity.java            # 主活动
├── AlarmActivity.java           # 闹钟响铃活动
├── AlarmReceiver.java           # 闹钟广播接收器
├── AlarmService.java            # 闹钟前台服务
├── BootReceiver.java            # 开机广播接收器
├── AlarmUtils.java              # 闹钟工具类
├── PsalmManager.java            # 诗篇管理器
├── build.gradle                 # 构建配置
├── res/
│   ├── layout/
│   │   ├── activity_main.xml    # 主界面布局
│   │   └── activity_alarm.xml   # 闹钟界面布局
│   ├── drawable/
│   │   ├── button_stop.xml      # 停止按钮样式
│   │   ├── button_snooze.xml    # 贪睡按钮样式
│   │   └── ic_alarm.xml         # 闹钟图标
│   └── values/
│       ├── strings.xml          # 字符串资源
│       ├── colors.xml           # 颜色资源
│       └── styles.xml           # 样式资源
└── README.md                    # 说明文档
```

# 圣经诗篇闹钟应用

## 应用简介

这是一个安卓闹钟应用，专门设计用于播放圣经诗篇的华语有声戏剧版录音作为闹钟铃声。每天会随机选择一篇诗篇进行播放，让用户在美好的圣经话语中开始新的一天。

## 主要功能

### 🔔 智能闹钟
- 支持设置每日定时闹钟
- 自动在设备重启后恢复闹钟设置
- 支持贪睡功能（5分钟）
- 锁屏状态下也能正常响铃

### 📖 每日诗篇
- 基于日期的智能算法，每天随机选择一篇诗篇
- 同一天总是播放相同的诗篇，确保一致性
- 涵盖诗篇1-150篇的完整内容
- 显示诗篇文本内容供用户阅读

### 🎵 音频播放
- 播放华语有声戏剧版诗篇录音
- 高质量音频体验
- 循环播放直到用户停止
- 支持后台播放

### 🎨 用户界面
- 简洁美观的Material Design设计
- 深色主题的闹钟响铃界面
- 卡片式布局，信息层次清晰
- 响应式设计，适配不同屏幕尺寸

## 技术特性

### 权限管理
- `WAKE_LOCK`: 保持设备唤醒状态
- `VIBRATE`: 振动提醒
- `RECEIVE_BOOT_COMPLETED`: 开机自启动
- `SCHEDULE_EXACT_ALARM`: 精确定时闹钟
- `FOREGROUND_SERVICE`: 前台服务
- `POST_NOTIFICATIONS`: 通知权限

### 核心组件
- **MainActivity**: 主界面，设置和管理闹钟
- **AlarmActivity**: 闹钟响铃界面
- **AlarmReceiver**: 广播接收器，处理闹钟触发
- **AlarmService**: 前台服务，确保闹钟可靠运行
- **BootReceiver**: 开机广播接收器
- **PsalmManager**: 诗篇管理器，处理诗篇选择和内容
- **AlarmUtils**: 闹钟工具类

## 文件结构

```
├── AndroidManifest.xml          # 应用清单文件
├── MainActivity.java            # 主活动
├── AlarmActivity.java           # 闹钟响铃活动
├── AlarmReceiver.java           # 闹钟广播接收器
├── AlarmService.java            # 闹钟前台服务
├── BootReceiver.java            # 开机广播接收器
├── AlarmUtils.java              # 闹钟工具类
├── PsalmManager.java            # 诗篇管理器
├── build.gradle                 # 构建配置
├── res/
│   ├── layout/
│   │   ├── activity_main.xml    # 主界面布局
│   │   └── activity_alarm.xml   # 闹钟界面布局
│   ├── drawable/
│   │   ├── button_stop.xml      # 停止按钮样式
│   │   ├── button_snooze.xml    # 贪睡按钮样式
│   │   └── ic_alarm.xml         # 闹钟图标
│   └── values/
│       ├── strings.xml          # 字符串资源
│       ├── colors.xml           # 颜色资源
│       └── styles.xml           # 样式资源
└── README.md                    # 说明文档
```

## 安装和使用

### 系统要求
- Android 5.0 (API 21) 及以上版本
- 至少50MB存储空间（用于音频文件）

### 安装步骤
1. 下载APK文件到Android设备
2. 启用"未知来源"应用安装权限
3. 安装应用
4. 授予必要的权限（闹钟、通知等）

### 使用方法
1. 打开应用，查看今日推荐的诗篇
2. 使用时间选择器设置闹钟时间
3. 点击"设置闹钟"按钮
4. 闹钟会在指定时间响起，播放当日诗篇录音
5. 可选择"停止闹钟"或"贪睡5分钟"

## 音频资源说明

应用需要诗篇1-150篇的华语有声戏剧版录音文件，文件命名格式为：
- `psalm_001.mp3` - 诗篇第1篇
- `psalm_002.mp3` - 诗篇第2篇
- ...
- `psalm_150.mp3` - 诗篇第150篇

音频文件应放置在 `res/raw/` 目录下。

## 开发说明

### 编译环境
- Android Studio 4.0+
- Gradle 7.0+
- Java 8+

### 依赖库
- AndroidX AppCompat
- Material Design Components
- CardView
- ConstraintLayout

## 版本信息
- 版本号：1.0
- 最低SDK：21 (Android 5.0)
- 目标SDK：34 (Android 14)

## 许可证
本项目仅供学习和个人使用。圣经内容和录音版权归相关版权方所有。

---

愿神的话语每天都能带给您平安和喜乐！ 🙏