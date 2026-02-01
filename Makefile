# Kids English Songs Player - 快捷命令
#
# 使用方法:
#   make build   - 编译打包
#   make install - 发送到手机
#   make run     - 编译打包 + 发送到手机

ANDROID_HOME := $(HOME)/Library/Android/sdk
ADB := $(ANDROID_HOME)/platform-tools/adb
APK_PATH := app/build/outputs/apk/debug/app-debug.apk
PACKAGE_NAME := com.kidsenglishsongs.player
DEVICE_ID := $(shell $(ADB) devices | grep -v "List" | grep "device$$" | head -1 | cut -f1)

.PHONY: build install run clean devices help

help:
	@echo "make build   - 编译打包 APK"
	@echo "make install - 发送到手机并启动"
	@echo "make run     - 编译 + 发送"
	@echo "make clean   - 清理构建缓存"
	@echo "make devices - 查看连接的设备"

build:
	@echo "开始编译..."
	./gradlew assembleDebug
	@echo "编译完成"

install:
	@echo "安装到设备 $(DEVICE_ID)..."
	$(ADB) -s $(DEVICE_ID) install -r $(APK_PATH)
	@echo "启动应用..."
	$(ADB) -s $(DEVICE_ID) shell monkey -p $(PACKAGE_NAME) -c android.intent.category.LAUNCHER 1
	@echo "完成"

run: build install

clean:
	./gradlew clean

devices:
	$(ADB) devices
