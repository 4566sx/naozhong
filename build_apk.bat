@echo off
echo ========================================
echo 圣经诗篇闹钟 APK 编译脚本
echo ========================================
echo.

REM 检查是否安装了 Android SDK
if not defined ANDROID_HOME (
    echo 错误：未找到 Android SDK
    echo 请先安装 Android Studio 并设置 ANDROID_HOME 环境变量
    echo.
    echo 设置方法：
    echo 1. 安装 Android Studio
    echo 2. 打开 Android Studio，进入 SDK Manager
    echo 3. 记录 SDK 路径（通常在 C:\Users\%USERNAME%\AppData\Local\Android\Sdk）
    echo 4. 设置环境变量 ANDROID_HOME 为 SDK 路径
    echo.
    pause
    exit /b 1
)

echo Android SDK 路径: %ANDROID_HOME%
echo.

REM 检查是否有 gradlew 文件
if not exist "gradlew.bat" (
    echo 错误：未找到 gradlew.bat 文件
    echo 请确保在项目根目录下运行此脚本
    pause
    exit /b 1
)

echo 开始编译 APK...
echo.

REM 清理之前的构建
echo [1/4] 清理之前的构建文件...
call gradlew.bat clean
if errorlevel 1 (
    echo 清理失败！
    pause
    exit /b 1
)

echo.
echo [2/4] 检查依赖项...
call gradlew.bat dependencies
if errorlevel 1 (
    echo 依赖项检查失败！
    pause
    exit /b 1
)

echo.
echo [3/4] 编译 Debug APK...
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo 编译失败！
    echo.
    echo 常见问题解决：
    echo 1. 检查网络连接（需要下载依赖）
    echo 2. 确认 Java 版本（需要 Java 8+）
    echo 3. 检查 Android SDK 是否完整安装
    echo 4. 尝试在 Android Studio 中打开项目进行同步
    pause
    exit /b 1
)

echo.
echo [4/4] 编译完成！
echo.

REM 查找生成的 APK 文件
set "APK_PATH=app\build\outputs\apk\debug"
if exist "%APK_PATH%\app-debug.apk" (
    echo ✅ APK 文件已生成：
    echo    %CD%\%APK_PATH%\app-debug.apk
    echo.
    echo 📱 安装方法：
    echo 1. 将 APK 文件传输到 Android 设备
    echo 2. 在设备上启用"未知来源"安装权限
    echo 3. 点击 APK 文件进行安装
    echo.
    
    REM 询问是否打开文件夹
    set /p open_folder="是否打开 APK 所在文件夹？(y/n): "
    if /i "%open_folder%"=="y" (
        explorer "%APK_PATH%"
    )
) else (
    echo ❌ 未找到生成的 APK 文件
    echo 请检查编译过程中的错误信息
)

echo.
echo 编译脚本执行完成！
pause