@echo off
echo 圣经诗篇音频文件重命名工具
echo ================================

REM 设置音频文件所在目录（请根据实际情况修改）
set "AUDIO_DIR=res\raw"

REM 创建目标目录（如果不存在）
if not exist "%AUDIO_DIR%" mkdir "%AUDIO_DIR%"

echo.
echo 请将您的诗篇音频文件放在以下目录中：
echo %CD%\%AUDIO_DIR%
echo.
echo 支持的重命名模式：
echo 1. 从 "诗篇001.mp3" 重命名为 "psalm_001.mp3"
echo 2. 从 "Psalm001.mp3" 重命名为 "psalm_001.mp3"  
echo 3. 从 "001.mp3" 重命名为 "psalm_001.mp3"
echo 4. 从 "诗篇第1篇.mp3" 重命名为 "psalm_001.mp3"
echo.

REM 检查是否有音频文件
if not exist "%AUDIO_DIR%\*.mp3" (
    echo 错误：在 %AUDIO_DIR% 目录中没有找到 .mp3 文件
    echo 请先将音频文件复制到该目录中
    pause
    exit /b 1
)

echo 找到以下音频文件：
dir /b "%AUDIO_DIR%\*.mp3"
echo.

set /p choice="请选择重命名模式 (1-4): "

if "%choice%"=="1" goto rename_mode1
if "%choice%"=="2" goto rename_mode2  
if "%choice%"=="3" goto rename_mode3
if "%choice%"=="4" goto rename_mode4

echo 无效选择，退出程序
pause
exit /b 1

:rename_mode1
echo 执行模式1：诗篇001.mp3 -> psalm_001.mp3
for %%f in ("%AUDIO_DIR%\诗篇*.mp3") do (
    set "filename=%%~nf"
    setlocal enabledelayedexpansion
    set "newname=!filename:诗篇=psalm_!"
    ren "%%f" "!newname!.mp3"
    echo 重命名: %%~nxf -> !newname!.mp3
    endlocal
)
goto end

:rename_mode2
echo 执行模式2：Psalm001.mp3 -> psalm_001.mp3
for %%f in ("%AUDIO_DIR%\Psalm*.mp3") do (
    set "filename=%%~nf"
    setlocal enabledelayedexpansion
    set "newname=!filename:Psalm=psalm_!"
    ren "%%f" "!newname!.mp3"
    echo 重命名: %%~nxf -> !newname!.mp3
    endlocal
)
goto end

:rename_mode3
echo 执行模式3：001.mp3 -> psalm_001.mp3
for %%f in ("%AUDIO_DIR%\???.mp3") do (
    set "filename=%%~nf"
    setlocal enabledelayedexpansion
    ren "%%f" "psalm_!filename!.mp3"
    echo 重命名: %%~nxf -> psalm_!filename!.mp3
    endlocal
)
goto end

:rename_mode4
echo 执行模式4：诗篇第N篇.mp3 -> psalm_00N.mp3
REM 这个模式需要更复杂的处理，建议手动处理或使用其他工具
echo 此模式较复杂，建议手动重命名或联系开发者
goto end

:end
echo.
echo 重命名完成！
echo 请检查 %AUDIO_DIR% 目录中的文件
dir /b "%AUDIO_DIR%\psalm_*.mp3"
echo.
pause