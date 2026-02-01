# Kids English Songs Player - 快捷命令
#
# 使用方法:
#   make build    - 编译 Debug APK
#   make install  - 安装 Debug 到手机
#   make run      - 编译 + 安装 Debug
#   make release  - 编译 Release APK
#   make deploy   - 编译 + 安装 Release（会先卸载旧版）

ANDROID_HOME := $(HOME)/Library/Android/sdk
ADB := $(ANDROID_HOME)/platform-tools/adb
APK_DEBUG := app/build/outputs/apk/debug/app-debug.apk
APK_RELEASE := app/build/outputs/apk/release/app-release.apk
PACKAGE_NAME := com.kidsenglishsongs.player
DEVICE_ID := $(shell $(ADB) devices | grep -v "List" | grep "device$$" | head -1 | cut -f1)

.PHONY: build install run clean devices help release deploy

help:
	@echo "=== Debug 版本 ==="
	@echo "  make build   - 编译 Debug APK"
	@echo "  make install - 安装 Debug 到手机"
	@echo "  make run     - 编译 + 安装 Debug"
	@echo ""
	@echo "=== Release 版本 ==="
	@echo "  make release - 编译 Release APK (签名)"
	@echo "  make deploy  - 编译 + 安装 Release (先卸载旧版)"
	@echo ""
	@echo "=== 其他 ==="
	@echo "  make clean   - 清理构建缓存"
	@echo "  make devices - 查看连接的设备"

build:
	@echo "编译 Debug APK..."
	./gradlew assembleDebug
	@echo "完成: $(APK_DEBUG)"

install:
	@echo "安装 Debug 到设备 $(DEVICE_ID)..."
	$(ADB) -s $(DEVICE_ID) install -r $(APK_DEBUG)
	@echo "启动应用..."
	@$(ADB) -s $(DEVICE_ID) shell monkey -p $(PACKAGE_NAME) -c android.intent.category.LAUNCHER 1 > /dev/null 2>&1
	@echo "完成"

run: build install

release:
	@echo "编译 Release APK..."
	./gradlew assembleRelease
	@echo "完成: $(APK_RELEASE)"

# 部署 Release 版本（先卸载旧版，再安装新版）
deploy: release
	@echo "卸载旧版本..."
	-$(ADB) -s $(DEVICE_ID) uninstall $(PACKAGE_NAME) 2>/dev/null || true
	@echo "安装 Release 到设备 $(DEVICE_ID)..."
	$(ADB) -s $(DEVICE_ID) install $(APK_RELEASE)
	@echo "启动应用..."
	@$(ADB) -s $(DEVICE_ID) shell monkey -p $(PACKAGE_NAME) -c android.intent.category.LAUNCHER 1 > /dev/null 2>&1
	@echo "部署完成!"

clean:
	./gradlew clean

devices:
	$(ADB) devices
