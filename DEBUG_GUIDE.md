# 儿童英文歌曲播放器 - Mac 调试指南

## 目录
1. [环境准备](#1-环境准备)
2. [安装必要工具](#2-安装必要工具)
3. [项目配置](#3-项目配置)
4. [模拟器设置](#4-模拟器设置)
5. [运行调试](#5-运行调试)
6. [测试指南](#6-测试指南)
7. [常见问题](#7-常见问题)

---

## 1. 环境准备

### 系统要求
- macOS 10.14 (Mojave) 或更高版本
- 至少 8GB RAM（推荐 16GB）
- 至少 20GB 可用磁盘空间
- Intel 或 Apple Silicon (M1/M2/M3) 芯片

### 检查系统版本
```bash
sw_vers
```

---

## 2. 安装必要工具

### 2.1 安装 Homebrew（如果未安装）
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

### 2.2 安装 JDK 17
```bash
# 使用 Homebrew 安装
brew install openjdk@17

# 配置环境变量（添加到 ~/.zshrc 或 ~/.bash_profile）
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# 验证安装
java -version
```

### 2.3 安装 Android Studio

#### 方式一：官网下载（推荐）
1. 访问 [Android Studio 官网](https://developer.android.com/studio)
2. 下载适合你芯片的版本：
   - Intel Mac: `android-studio-*-mac.dmg`
   - Apple Silicon: `android-studio-*-mac_arm.dmg`
3. 打开 DMG 文件，将 Android Studio 拖到 Applications 文件夹

#### 方式二：使用 Homebrew
```bash
brew install --cask android-studio
```

### 2.4 首次启动 Android Studio 配置

1. 启动 Android Studio
2. 选择 "Do not import settings"
3. 选择 "Standard" 安装类型
4. 选择 UI 主题
5. 等待组件下载完成（包括 Android SDK、模拟器等）

### 2.5 配置 Android SDK

打开 Android Studio 后：
1. 点击 **Android Studio → Settings** (或 `Cmd + ,`)
2. 导航到 **Languages & Frameworks → Android SDK**
3. 在 **SDK Platforms** 标签页，勾选：
   - Android 14.0 (API 34)
   - Android 13.0 (API 33)
4. 在 **SDK Tools** 标签页，勾选：
   - Android SDK Build-Tools 34
   - Android SDK Command-line Tools
   - Android Emulator
   - Android SDK Platform-Tools
5. 点击 **Apply** 并等待下载完成

### 2.6 配置环境变量

```bash
# 添加到 ~/.zshrc
echo 'export ANDROID_HOME=$HOME/Library/Android/sdk' >> ~/.zshrc
echo 'export PATH=$PATH:$ANDROID_HOME/emulator' >> ~/.zshrc
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.zshrc
echo 'export PATH=$PATH:$ANDROID_HOME/tools/bin' >> ~/.zshrc
source ~/.zshrc

# 验证配置
adb version
```

---

## 3. 项目配置

### 3.1 打开项目

1. 启动 Android Studio
2. 选择 **Open**
3. 导航到项目目录：`/Users/yonghu/code/ai_app`
4. 点击 **Open**
5. 等待 Gradle 同步完成（首次可能需要几分钟）

### 3.2 Gradle 同步

如果 Gradle 同步失败，尝试：
```bash
# 在项目根目录执行
cd /Users/yonghu/code/ai_app
./gradlew clean
./gradlew build --refresh-dependencies
```

### 3.3 检查项目配置

确保以下文件配置正确：
- `gradle/libs.versions.toml` - 依赖版本
- `app/build.gradle.kts` - App 构建配置
- `settings.gradle.kts` - 项目设置

---

## 4. 模拟器设置

### 4.1 创建虚拟设备 (AVD)

1. 在 Android Studio 中，点击 **Tools → Device Manager**
2. 点击 **Create Device**
3. 选择设备类型：
   - 推荐：**Pixel 6** 或 **Pixel 7**
   - 分辨率和大小适中，适合测试
4. 点击 **Next**
5. 选择系统镜像：
   - 推荐：**API 34 (Android 14)** 或 **API 33 (Android 13)**
   - Apple Silicon Mac 选择 **arm64-v8a** 架构
   - Intel Mac 选择 **x86_64** 架构
6. 点击 **Download** 下载系统镜像（如果尚未下载）
7. 点击 **Next**
8. 配置 AVD：
   - AVD Name: `Pixel_6_API_34`
   - 启用 **Device Frame**
9. 点击 **Finish**

### 4.2 启动模拟器

```bash
# 命令行启动（可选）
emulator -avd Pixel_6_API_34

# 或在 Android Studio Device Manager 中点击播放按钮
```

### 4.3 使用真机调试（推荐）

1. 在 Android 手机上启用开发者选项：
   - 进入 **设置 → 关于手机**
   - 连续点击 **版本号** 7次
2. 启用 USB 调试：
   - 进入 **设置 → 开发者选项**
   - 开启 **USB 调试**
3. 用 USB 线连接手机到 Mac
4. 在手机上允许 USB 调试授权
5. 验证连接：
```bash
adb devices
# 应显示你的设备
```

---

## 5. 运行调试

### 5.1 运行应用

#### 方式一：Android Studio
1. 在顶部工具栏选择目标设备（模拟器或真机）
2. 点击绿色 **Run** 按钮 (或 `Ctrl + R`)

#### 方式二：命令行
```bash
cd /Users/yonghu/code/ai_app

# 构建 Debug APK
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug

# 或者一步完成
./gradlew installDebug && adb shell am start -n com.kidsenglishsongs.player/.MainActivity
```

### 5.2 调试模式

1. 在代码中设置断点（点击行号左侧）
2. 点击 **Debug** 按钮 (或 `Ctrl + D`)
3. 使用调试工具：
   - **Step Over** (F8): 执行下一行
   - **Step Into** (F7): 进入函数
   - **Step Out** (Shift + F8): 跳出函数
   - **Resume** (F9): 继续执行

### 5.3 查看日志

#### Android Studio Logcat
1. 点击底部 **Logcat** 标签
2. 选择目标设备
3. 使用过滤器筛选日志：
```
package:com.kidsenglishsongs.player
```

#### 命令行查看日志
```bash
# 查看所有日志
adb logcat

# 筛选应用日志
adb logcat | grep -i "kidsenglishsongs"

# 只看错误日志
adb logcat *:E
```

### 5.4 热重载 (Apply Changes)

1. 修改代码后
2. 点击 **Apply Changes** 按钮 (闪电图标，或 `Ctrl + Cmd + E`)
3. 无需重启应用即可看到更改

---

## 6. 测试指南

### 6.1 运行单元测试

```bash
# 运行所有单元测试
./gradlew test

# 运行特定测试类
./gradlew test --tests "com.kidsenglishsongs.player.data.repository.SongRepositoryTest"

# 生成测试报告
./gradlew test jacocoTestReport
# 报告位置: app/build/reports/tests/
```

### 6.2 运行 UI 测试 (Instrumented Tests)

```bash
# 确保设备已连接
adb devices

# 运行所有 UI 测试
./gradlew connectedAndroidTest

# 运行特定测试
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.kidsenglishsongs.player.ui.player.PlayerScreenTest
```

### 6.3 在 Android Studio 中运行测试

1. 右键点击测试文件或测试方法
2. 选择 **Run 'TestName'** 或 **Debug 'TestName'**
3. 查看测试结果面板

### 6.4 测试覆盖率

```bash
# 生成覆盖率报告
./gradlew createDebugCoverageReport

# 报告位置
open app/build/reports/coverage/androidTest/debug/index.html
```

---

## 7. 常见问题

### Q1: Gradle 同步失败
```bash
# 清理并重新构建
./gradlew clean
./gradlew build --refresh-dependencies

# 删除 .gradle 缓存
rm -rf ~/.gradle/caches/
```

### Q2: 模拟器启动慢或崩溃
- 确保已启用硬件加速
- Apple Silicon Mac: 使用 arm64 镜像
- 增加模拟器内存：AVD Manager → Edit → Show Advanced Settings → RAM

### Q3: ADB 设备未识别
```bash
# 重启 ADB
adb kill-server
adb start-server
adb devices
```

### Q4: 构建内存不足
在 `gradle.properties` 中添加：
```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g
```

### Q5: Apple Silicon 兼容性问题
确保安装了 arm64 版本的：
- Android Studio
- JDK
- Android Emulator 系统镜像

---

## 快速命令参考

```bash
# 构建
./gradlew assembleDebug          # 构建 Debug APK
./gradlew assembleRelease        # 构建 Release APK

# 安装
./gradlew installDebug           # 安装 Debug 版本
adb install app/build/outputs/apk/debug/app-debug.apk

# 测试
./gradlew test                   # 单元测试
./gradlew connectedAndroidTest   # UI 测试

# 清理
./gradlew clean                  # 清理构建
./gradlew cleanBuildCache        # 清理构建缓存

# 日志
adb logcat -c                    # 清除日志
adb logcat | grep kidsenglish    # 筛选日志
```

---

## 联系方式

如有问题，请检查：
1. Android Studio 内置的 **Build** 输出
2. **Logcat** 日志
3. Gradle 构建报告
