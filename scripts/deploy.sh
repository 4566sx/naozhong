#!/bin/bash

# 圣经闹钟应用部署脚本
# 用于自动化构建、测试和发布流程

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查必要工具
check_requirements() {
    log_info "检查构建环境..."
    
    # 检查Java版本
    if ! command -v java &> /dev/null; then
        log_error "Java未安装或未在PATH中"
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$java_version" -lt 17 ]; then
        log_error "需要Java 17或更高版本，当前版本: $java_version"
        exit 1
    fi
    
    # 检查Android SDK
    if [ -z "$ANDROID_HOME" ]; then
        log_error "ANDROID_HOME环境变量未设置"
        exit 1
    fi
    
    # 检查密钥库配置
    if [ ! -f "keystore.properties" ]; then
        log_warning "keystore.properties文件不存在，将使用调试签名"
        USE_DEBUG_SIGNING=true
    else
        USE_DEBUG_SIGNING=false
    fi
    
    log_success "环境检查完成"
}

# 清理构建产物
clean_build() {
    log_info "清理构建产物..."
    ./gradlew clean
    log_success "清理完成"
}

# 运行测试
run_tests() {
    log_info "运行单元测试..."
    ./gradlew test
    
    log_info "运行代码检查..."
    ./gradlew lint
    
    # 如果有连接的设备，运行集成测试
    if adb devices | grep -q "device$"; then
        log_info "运行集成测试..."
        ./gradlew connectedAndroidTest
    else
        log_warning "未检测到连接的设备，跳过集成测试"
    fi
    
    log_success "测试完成"
}

# 构建应用
build_app() {
    local build_type=$1
    
    log_info "构建${build_type}版本..."
    
    if [ "$build_type" = "release" ]; then
        if [ "$USE_DEBUG_SIGNING" = true ]; then
            log_warning "使用调试签名构建发布版本"
            ./gradlew assembleRelease -Pandroid.injected.signing.store.file=debug.keystore
        else
            ./gradlew assembleRelease
        fi
    else
        ./gradlew assembleDebug
    fi
    
    log_success "${build_type}版本构建完成"
}

# 生成构建报告
generate_reports() {
    log_info "生成构建报告..."
    
    # 生成测试报告
    ./gradlew jacocoTestReport
    
    # 生成依赖报告
    ./gradlew dependencies > build/reports/dependencies.txt
    
    # 生成APK分析报告
    if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
        aapt dump badging app/build/outputs/apk/release/app-release.apk > build/reports/apk-info.txt
    fi
    
    log_success "报告生成完成"
}

# 验证APK
verify_apk() {
    local apk_path=$1
    
    log_info "验证APK: $apk_path"
    
    if [ ! -f "$apk_path" ]; then
        log_error "APK文件不存在: $apk_path"
        return 1
    fi
    
    # 检查APK签名
    if command -v jarsigner &> /dev/null; then
        if jarsigner -verify -verbose "$apk_path" > /dev/null 2>&1; then
            log_success "APK签名验证通过"
        else
            log_error "APK签名验证失败"
            return 1
        fi
    fi
    
    # 检查APK大小
    apk_size=$(stat -f%z "$apk_path" 2>/dev/null || stat -c%s "$apk_path" 2>/dev/null)
    apk_size_mb=$((apk_size / 1024 / 1024))
    
    log_info "APK大小: ${apk_size_mb}MB"
    
    if [ $apk_size_mb -gt 100 ]; then
        log_warning "APK大小超过100MB，可能需要优化"
    fi
    
    return 0
}

# 安装APK到设备
install_apk() {
    local apk_path=$1
    
    if ! adb devices | grep -q "device$"; then
        log_warning "未检测到连接的设备，跳过安装"
        return 0
    fi
    
    log_info "安装APK到设备..."
    
    # 卸载旧版本（如果存在）
    adb uninstall com.biblealarm.app 2>/dev/null || true
    
    # 安装新版本
    if adb install "$apk_path"; then
        log_success "APK安装成功"
    else
        log_error "APK安装失败"
        return 1
    fi
}

# 创建发布包
create_release_package() {
    local version_name=$1
    local package_dir="release-packages"
    local package_name="bible-alarm-${version_name}"
    
    log_info "创建发布包..."
    
    mkdir -p "$package_dir/$package_name"
    
    # 复制APK文件
    if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
        cp "app/build/outputs/apk/release/app-release.apk" "$package_dir/$package_name/bible-alarm-${version_name}.apk"
    fi
    
    # 复制AAB文件（如果存在）
    if [ -f "app/build/outputs/bundle/release/app-release.aab" ]; then
        cp "app/build/outputs/bundle/release/app-release.aab" "$package_dir/$package_name/bible-alarm-${version_name}.aab"
    fi
    
    # 复制映射文件
    if [ -f "app/build/outputs/mapping/release/mapping.txt" ]; then
        cp "app/build/outputs/mapping/release/mapping.txt" "$package_dir/$package_name/"
    fi
    
    # 复制构建报告
    if [ -d "build/reports" ]; then
        cp -r "build/reports" "$package_dir/$package_name/"
    fi
    
    # 创建版本信息文件
    cat > "$package_dir/$package_name/version-info.txt" << EOF
版本名称: $version_name
构建时间: $(date)
Git提交: $(git rev-parse HEAD 2>/dev/null || echo "未知")
Git分支: $(git branch --show-current 2>/dev/null || echo "未知")
构建环境: $(uname -a)
Java版本: $(java -version 2>&1 | head -n1)
EOF
    
    # 创建压缩包
    cd "$package_dir"
    tar -czf "${package_name}.tar.gz" "$package_name"
    cd ..
    
    log_success "发布包创建完成: $package_dir/${package_name}.tar.gz"
}

# 上传到应用商店（模拟）
upload_to_store() {
    local apk_path=$1
    
    log_info "准备上传到应用商店..."
    
    # 这里可以集成实际的上传逻辑
    # 例如使用Google Play Console API
    
    log_warning "应用商店上传功能需要手动配置"
    log_info "APK路径: $apk_path"
    log_info "请手动上传到相应的应用商店"
}

# 发送通知
send_notification() {
    local message=$1
    
    # 这里可以集成Slack、邮件等通知方式
    log_info "通知: $message"
}

# 主函数
main() {
    local build_type="release"
    local skip_tests=false
    local install_after_build=false
    local create_package=false
    
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            --debug)
                build_type="debug"
                shift
                ;;
            --skip-tests)
                skip_tests=true
                shift
                ;;
            --install)
                install_after_build=true
                shift
                ;;
            --package)
                create_package=true
                shift
                ;;
            --help)
                echo "用法: $0 [选项]"
                echo "选项:"
                echo "  --debug         构建调试版本（默认为发布版本）"
                echo "  --skip-tests    跳过测试"
                echo "  --install       构建后安装到设备"
                echo "  --package       创建发布包"
                echo "  --help          显示帮助信息"
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                exit 1
                ;;
        esac
    done
    
    log_info "开始部署流程..."
    log_info "构建类型: $build_type"
    
    # 执行部署步骤
    check_requirements
    clean_build
    
    if [ "$skip_tests" = false ]; then
        run_tests
    else
        log_warning "跳过测试"
    fi
    
    build_app "$build_type"
    generate_reports
    
    # 确定APK路径
    if [ "$build_type" = "release" ]; then
        apk_path="app/build/outputs/apk/release/app-release.apk"
    else
        apk_path="app/build/outputs/apk/debug/app-debug.apk"
    fi
    
    verify_apk "$apk_path"
    
    if [ "$install_after_build" = true ]; then
        install_apk "$apk_path"
    fi
    
    if [ "$create_package" = true ] && [ "$build_type" = "release" ]; then
        # 从build.gradle.kts中提取版本名称
        version_name=$(grep "versionName" app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/')
        create_release_package "$version_name"
    fi
    
    log_success "部署完成！"
    
    # 显示构建结果
    echo ""
    echo "构建结果:"
    echo "- APK路径: $apk_path"
    if [ -f "$apk_path" ]; then
        apk_size=$(stat -f%z "$apk_path" 2>/dev/null || stat -c%s "$apk_path" 2>/dev/null)
        apk_size_mb=$((apk_size / 1024 / 1024))
        echo "- APK大小: ${apk_size_mb}MB"
    fi
    echo "- 构建报告: build/reports/"
    
    send_notification "圣经闹钟应用构建完成 - $build_type"
}

# 执行主函数
main "$@"