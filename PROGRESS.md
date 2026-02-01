# Kids English Songs Player - 项目进度

**最后更新**: 2026年2月1日

---

## 📊 当前进度概览

| 模块 | 状态 | 完成度 |
|------|------|--------|
| 项目基础设施 | ✅ 完成 | 100% |
| 数据层 | ✅ 完成 | 100% |
| 播放器服务 | ✅ 完成 | 100% |
| UI 主题 | ✅ 完成 | 100% |
| UI 组件 | ✅ 完成 | 100% |
| 核心页面 | ✅ 完成 | 100% |
| 导航系统 | ✅ 完成 | 100% |
| 单元测试 | ✅ 完成 | 100% |
| UI 测试 | ✅ 完成 | 100% |
| 调试文档 | ✅ 完成 | 100% |
| **编译验证** | ✅ 完成 | 100% |
| **单元测试运行** | ✅ 完成 | 100% |
| **APK 构建** | ✅ 完成 | 100% |
| **真机测试** | ✅ 完成 | 100% |
| **音乐导入功能** | ✅ 完成 | 100% |
| **网易云风格 UI** | ✅ 完成 | 100% |

---

## 🎯 已完成里程碑

### 2026年2月1日 - 重大更新
- ✅ **网易云音乐风格播放界面**
  - 唱片旋转动画（20秒/圈）
  - 唱针动画（播放时放下，暂停时抬起）
  - 深色沉浸式背景 + 模糊封面
  - 红色强调色 (#EC4141)
- ✅ **全局 UI 风格统一**
  - 深色主题（背景 #1A1A1A）
  - 网易云红主色调
  - 歌曲库、设置、导入页面统一风格
- ✅ **音乐导入功能**
  - 本地音乐扫描（MediaStore）
  - MD5 哈希去重
  - 文件夹导入支持
  - 批量选择导入
- ✅ **数据库升级** (v1 → v2)
  - 新增字段: fileHash, artist, album, fileSize
  - 数据迁移脚本
- ✅ **快捷构建命令** (Makefile)
  - `make build` - 编译打包
  - `make install` - 发送到手机
  - `make run` - 一键部署
- ✅ 真机测试通过（OPPO 手机）

### 2026年2月1日 - 初始版本
- ✅ 修复所有编译错误
- ✅ 88 个单元测试全部通过
- ✅ 成功构建 Debug APK (20MB)
- ✅ 代码已提交到 Git

---

## 🔧 环境配置记录

### 已安装的开发工具
| 工具 | 版本 | 安装方式 |
|------|------|----------|
| Homebrew | 4.3.0+ | 官方脚本 (GitHub 克隆) |
| JDK | 17.0.13 (Eclipse Adoptium) | 手动下载安装 |
| Gradle | 8.5 | 腾讯云镜像下载 |
| Android SDK | 34 | sdkmanager 安装 |
| Build Tools | 34.0.0 | sdkmanager 安装 |
| Platform Tools | 最新版 | sdkmanager 安装 |

### 环境变量配置 (~/.zshrc)
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export ANDROID_HOME=~/Library/Android/sdk
export PATH="/opt/gradle/gradle-8.5/bin:$PATH"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
```

---

## ⚠️ 遇到的问题与解决方案

### 1. Homebrew 安装问题
**问题**: 旧版 Homebrew (2019年) 无法更新，出现 shallow clone 错误
```
homebrew-core is a shallow clone.
homebrew-cask is a shallow clone.
```

**解决方案**: 
1. 卸载旧版 Homebrew
2. 直接从 GitHub 克隆最新版到 `/usr/local/Homebrew`
3. 配置中科大镜像源加速

---

### 2. Gradle 下载超时
**问题**: 国内网络访问 GitHub/Gradle 官网超时

**解决方案**: 使用腾讯云镜像
```bash
curl -L -o /tmp/gradle.zip "https://mirrors.cloud.tencent.com/gradle/gradle-8.5-bin.zip"
```

---

### 3. JDK 版本不兼容
**问题**: 系统自带 JDK 13，Android Gradle Plugin 需要 JDK 17+
```
SDK location not found. Define a valid SDK location with an ANDROID_HOME environment variable
```

**解决方案**: 
1. 从 Adoptium 下载 JDK 17
2. 安装到 `/Library/Java/JavaVirtualMachines/`
3. 配置 `JAVA_HOME` 环境变量

---

### 4. AndroidX 未启用
**问题**: 编译报错 `android.useAndroidX` property is not enabled

**解决方案**: 创建 `gradle.properties` 文件
```properties
android.useAndroidX=true
android.nonTransitiveRClass=true
```

---

### 5. Mockito 与 Kotlin 协程不兼容
**问题**: 单元测试中 suspend 函数 mock 失败
```
Suspend function should be called only from a coroutine
InvalidUseOfMatchersException
```

**解决方案**:
1. 添加 `mockito-kotlin` 依赖
2. 使用 `whenever()` 替代 `when()`
3. 使用 `doReturn` 和 `onBlocking` 处理 suspend 函数

```kotlin
// 错误写法
`when`(repository.getSongCount()).thenReturn(1)

// 正确写法
songRepository = mock {
    onBlocking { getSongCount() } doReturn 1
}
```

---

### 6. 模拟器启动极慢/offline
**问题**: x86_64 模拟器在 Intel Mac 上启动非常慢，一直显示 offline
```
List of devices attached
emulator-5554   offline
```

**可能原因**:
- 第一次启动使用了 `-wipe-data`，需要完整初始化系统
- 模拟器冷启动时间长（文档说可能需要 2+ 分钟）
- 出现 `IMKClient Stall detected` 兼容性警告

**解决方案**: 
- 等待更长时间（5-10分钟）
- 或者使用真机调试（推荐）
- 下次启动不用 `-wipe-data`

---

### 7. Package Manager 服务不可用
**问题**: 设备虽然显示 `device`，但无法安装 APK
```
Error: Could not access the Package Manager
```

**原因**: Android 系统服务还未完全启动（boot animation 仍在运行）

**解决方案**: 等待 `sys.boot_completed=1`
```bash
adb shell getprop sys.boot_completed
# 返回 1 时表示启动完成
```

---

## 📁 构建产物

| 文件 | 路径 | 大小 |
|------|------|------|
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` | ~20MB |

---

## 📱 真机调试步骤

### 前提条件
1. Android 手机（Android 8.0+，推荐 Android 10+）
2. USB 数据线

### 步骤
1. **开启开发者选项**: 设置 > 关于手机 > 连续点击版本号 7 次
2. **开启 USB 调试**: 开发者选项 > USB 调试
3. **连接手机**: USB 连接后允许调试授权
4. **验证连接**: `adb devices` 看到设备
5. **安装 APK**: `adb install app/build/outputs/apk/debug/app-debug.apk`

---

## 🚀 常用命令

```bash
# 快捷命令 (推荐)
make build      # 编译打包
make install    # 发送到手机
make run        # 一键部署（编译+安装）
make clean      # 清理缓存
make devices    # 查看设备

# 原始命令
./gradlew assembleDebug          # 编译项目
./gradlew test                   # 运行单元测试
adb devices                      # 查看连接设备
adb install -r <apk路径>         # 安装 APK
adb shell am start -n com.kidsenglishsongs.player/.MainActivity  # 启动应用
adb logcat | grep -i "kidsenglish"  # 查看日志
```

# 查看连接设备
adb devices

# 安装 APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 启动应用
adb shell am start -n com.kidsenglishsongs.player/.MainActivity

# 查看日志
adb logcat | grep -i "kidsenglish"

# 卸载应用
adb uninstall com.kidsenglishsongs.player
```

---

## ✅ 已完成工作

### 1. 项目基础设施
- [x] Gradle 配置 (Kotlin DSL)
- [x] Version Catalog (libs.versions.toml)
- [x] AndroidManifest.xml
- [x] ProGuard 规则
- [x] 资源文件 (strings.xml, colors.xml)

### 2. 数据层 (Room Database)
- [x] **实体类**: SongEntity, GroupEntity, TagEntity, PlaylistEntity, PlayHistoryEntity
- [x] **关联表**: PlaylistSongCrossRef, SongTagCrossRef
- [x] **DAO 接口**: SongDao, GroupDao, TagDao, PlaylistDao, PlayHistoryDao
- [x] **数据库**: AppDatabase (Room, 版本 2)
  - v1 → v2 迁移：新增 fileHash, artist, album, fileSize 字段
- [x] **仓库类**: SongRepository, GroupRepository, TagRepository

### 3. 播放器服务 (Media3)
- [x] PlaybackState 状态模型
- [x] RepeatMode 枚举 (OFF, ONE, ALL)
- [x] PlaybackService (MediaSessionService)
- [x] PlayerController (播放控制、睡眠定时器)
- [x] 通知栏控制支持
- [x] 后台播放支持

### 4. UI 层 (Jetpack Compose)

#### 主题 - 网易云音乐风格深色主题
- [x] Color.kt - 网易云红 (#EC4141)、深色背景 (#1A1A1A)
- [x] Type.kt - 圆润字体排版
- [x] Theme.kt - Material3 深色主题配置

#### 组件
- [x] SongCard - 歌曲卡片（深色风格）
- [x] LargeSongCard - 大型歌曲卡片
- [x] BigPlayButton - 大播放按钮 (80dp)
- [x] TagChip - 标签芯片
- [x] SleepTimerDialog - 睡眠定时器对话框
- [x] SleepTimerIndicator - 定时器指示器
- [x] MiniPlayer - 迷你播放器
- [x] PlaybackControls - 播放控制栏
- [x] ProgressBar - 进度条
- [x] LyricsDisplay - 歌词显示

#### 页面
- [x] **PlayerScreen** - 网易云风格播放器
  - 唱片旋转动画（20秒/圈）
  - 唱针动画（播放放下/暂停抬起）
  - 深色沉浸式背景
  - 模糊封面背景
  - 精细唱针组件（底座/唱臂/唱头/针尖）
- [x] PlayerViewModel - 播放器视图模型
- [x] LibraryScreen - 音乐库页面（深色风格）
- [x] LibraryViewModel - 音乐库视图模型
- [x] ParentControlScreen - 家长控制页面
- [x] ParentControlViewModel - 家长控制视图模型
- [x] SettingsScreen - 设置页面（深色风格）
- [x] **ImportMusicScreen** - 音乐导入页面（新增）
- [x] **ImportMusicViewModel** - 导入逻辑（新增）

#### 导航
- [x] AppNavigation - 导航图
- [x] Routes 对象 - 路由定义

### 5. 依赖注入 (Hilt)
- [x] DatabaseModule - 数据库模块
- [x] AppModule - 应用模块

### 6. 工具类
- [x] LrcParser - LRC 歌词解析器
- [x] AudioMetadataReader - 音频元数据读取器
- [x] **LocalMusicScanner** - 本地音乐扫描器（MediaStore）
- [x] **FileHashUtils** - MD5 哈希计算（文件去重）

### 7. 测试代码

#### 单元测试 (src/test/)
- [x] SongRepositoryTest
- [x] GroupRepositoryTest
- [x] TagRepositoryTest
- [x] LrcParserTest
- [x] PlaybackStateTest
- [x] PlayerViewModelTest
- [x] ParentControlViewModelTest
- [x] EntityTest

#### UI/集成测试 (src/androidTest/)
- [x] PlayerComponentsTest
- [x] ComponentsTest
- [x] SleepTimerTest
- [x] AppDatabaseTest
- [x] NavigationTest

### 8. 文档与构建
- [x] DEBUG_GUIDE.md - Mac 调试指南
- [x] PROGRESS.md - 项目进度文档
- [x] **Makefile** - 快捷构建命令

---

## ⏳ 待进行工作（可选优化）

### 功能优化
- [ ] 歌词同步显示
- [ ] 均衡器/音效设置
- [ ] 播放列表管理
- [ ] 歌曲编辑（重命名、封面）
- [ ] 云端备份

### 性能优化
- [ ] 大列表虚拟滚动优化
- [ ] 图片缓存策略
- [ ] 后台播放优化

### 发布准备
- [ ] 生成签名 APK/AAB
- [ ] 应用图标优化
- [ ] 应用商店截图
- [ ] 用户使用文档

---

## 🛠 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 2.0.0 | 开发语言 |
| Jetpack Compose | BOM 2024.02.00 | UI 框架 |
| Media3/ExoPlayer | 1.2.1 | 音频播放 |
| Room | 2.6.1 | 本地数据库 |
| Hilt | 2.50 | 依赖注入 |
| Navigation Compose | 2.7.6 | 页面导航 |
| Coil | 2.5.0 | 图片加载 |
| Material3 | - | UI 设计 |

---

## 🎨 UI 设计规范

### 配色方案 - 网易云音乐风格
| 颜色 | 色值 | 用途 |
|------|------|------|
| Primary | #EC4141 | 主色调（网易云红） |
| Background | #1A1A1A | 背景色 |
| Surface | #2A2A2A | 卡片/表面色 |
| SurfaceVariant | #3A3A3A | 次级表面色 |
| OnBackground | #FFFFFF | 主要文字 |
| OnSurfaceVariant | #B3B3B3 | 辅助文字 |

### 播放界面特效
- 唱片旋转：20秒/圈，播放时旋转，暂停时保持
- 唱针动画：播放时 18°（放下），暂停时 -25°（抬起）
- 背景模糊：专辑封面 100dp 模糊 + 渐变遮罩

---

## 📁 项目结构

```
ai_app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/kidsenglishsongs/player/
│   │   │   │   ├── data/           # 数据层
│   │   │   │   │   ├── dao/        # DAO 接口
│   │   │   │   │   ├── entity/     # 实体类
│   │   │   │   │   ├── repository/ # 仓库类
│   │   │   │   │   └── AppDatabase.kt
│   │   │   │   ├── di/             # 依赖注入
│   │   │   │   ├── player/         # 播放器
│   │   │   │   │   ├── controller/ # 播放控制
│   │   │   │   │   ├── service/    # 媒体服务
│   │   │   │   │   └── PlaybackState.kt
│   │   │   │   ├── ui/             # UI 层
│   │   │   │   │   ├── components/ # 通用组件
│   │   │   │   │   ├── library/    # 音乐库
│   │   │   │   │   ├── navigation/ # 导航
│   │   │   │   │   ├── parent/     # 家长控制
│   │   │   │   │   ├── player/     # 播放器界面
│   │   │   │   │   ├── settings/   # 设置 + 导入音乐
│   │   │   │   │   └── theme/      # 主题（网易云风格）
│   │   │   │   ├── util/           # 工具类
│   │   │   │   │   ├── LrcParser.kt
│   │   │   │   │   ├── AudioMetadataReader.kt
│   │   │   │   │   ├── LocalMusicScanner.kt  # 新增
│   │   │   │   │   └── FileHashUtils.kt      # 新增
│   │   │   │   ├── KidsEnglishSongsApp.kt
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/                # 资源文件
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                   # 单元测试
│   │   └── androidTest/            # UI 测试
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml          # 版本目录
├── build.gradle.kts                # 项目级配置
├── settings.gradle.kts
├── Makefile                        # 快捷命令
├── DEBUG_GUIDE.md                  # 调试指南
└── PROGRESS.md                     # 本文档
```

---

## 📝 备注

- 目标用户: 儿童英语歌曲播放
- 设计风格: 网易云音乐深色主题
- 主要功能: 本地音频播放、唱片动画、睡眠定时器、音乐导入
- 支持 Android 版本: 8.0 (API 26) - 14 (API 34)
- 测试设备: OPPO 手机 (设备ID: 351acfe1)
