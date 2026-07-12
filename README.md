# 滴答滴答 DidaDida

[![Platform](https://img.shields.io/badge/Android-15%2B-3DDC84?style=for-the-badge&logo=android)](https://android.com)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-22C55E?style=for-the-badge)](./LICENSE)

> 从 [futuredo/didadida-ios](https://github.com/futuredo/didadida-ios) 移植到 Android

一个偏产品化的工时记录与考勤分析小工具。用 Jetpack Compose + Material3 做了更有节奏感的界面，把上班/下班打卡、自动扣午休晚饭、月度加班统计、桌面快捷方式这些高频需求放在一个足够轻的本地应用里。

## 功能

- **今日页**：有效工时进度环、上班/下班打卡按钮、自动填充
- **记录页**：按月份浏览每日工时卡片，快速回看加班情况
- **分析页**：月度汇总统计、每日工时柱状图、加班费估算
- **设置页**：配置标准工时、默认上班时间、午休/晚饭扣除规则、时薪与倍率
- **桌面快捷方式**：长按图标直接打卡上班/下班/自动填今天
- **数据备份**：导出到剪贴板 / 从剪贴板导入，JSON 格式
- **记录删除**：记录页卡片右上角可删除单日记录
- **本地存储**：基于 DataStore，不依赖任何云端账号，数据全在本地

## 截图

| 今日页 | 分析页 | 快捷方式 |
|--------|--------|----------|
| 进度环 + 打卡按钮 | 月度统计 + 柱状图 | 长按图标直接打卡 |

## 技术栈

- **Kotlin** + **Jetpack Compose** + **Material3**
- **Navigation Compose**（底部导航）
- **DataStore Preferences**（本地持久化）
- **Kotlinx Serialization** + **Kotlinx Datetime**
- **ViewModel** + **StateFlow**（响应式状态管理）
- **Swift Testing** 风格的单元测试（12 个用例覆盖工时计算核心逻辑）
- **GitHub Actions**：每次提交自动构建 APK

## 项目结构

```
DidaDidaAndroid/
├── app/
│   ├── src/main/java/com/jaye/didadida/
│   │   ├── App.kt                    # Application 入口
│   │   ├── MainActivity.kt           # Compose 宿主 + 快捷方式处理
│   │   ├── domain/
│   │   │   ├── WorkLog.kt            # 打卡记录模型
│   │   │   ├── SettingsConfig.kt     # 配置 + 休息规则
│   │   │   └── WorkTimeCalculator.kt # 工时计算核心
│   │   ├── data/
│   │   │   ├── WorkLogStorage.kt     # DataStore 持久化
│   │   │   └── WorkLogRepository.kt  # 统一数据入口
│   │   └── ui/
│   │       ├── theme/                # Material3 配色
│   │       ├── navigation/           # 底部导航
│   │       ├── today/                # 今日页
│   │       ├── records/              # 记录页
│   │       ├── analysis/             # 分析页
│   │       └── settings/             # 设置页
│   └── src/test/                     # 单元测试
├── build.gradle.kts                  # 根构建配置
├── settings.gradle.kts               # 模块配置
└── .github/workflows/build.yml       # CI 自动构建
```

## 运行方式

### 方式一：下载 APK
前往 [Actions 页面](https://github.com/wujie272/DidaDidaAndroid/actions) 找到最新的成功构建，下载 `DidaDida-debug` 工件，解压后安装 APK。

### 方式二：本地构建
```bash
# 克隆仓库
git clone https://github.com/wujie272/DidaDidaAndroid.git
cd DidaDidaAndroid

# 构建
./gradlew assembleDebug

# APK 在 app/build/outputs/apk/debug/
```

### 方式三：Android Studio
用 Android Studio 打开项目根目录，Sync Gradle 后直接 Run。

## 数据与隐私

- 所有记录默认保存在本机 DataStore 中
- 项目中不包含服务器、账号系统、远程埋点
- 无网络权限，数据不会离开你的设备

## 与 iOS 版的差异

| 功能 | iOS 版 (SwiftUI) | Android 版 (Compose) |
|------|-----------------|---------------------|
| UI 框架 | SwiftUI + Observation | Jetpack Compose + Material3 |
| 持久化 | UserDefaults + Codable | DataStore Preferences |
| 快捷操作 | App Intents / Shortcuts | 桌面快捷方式 (Shortcuts) |
| 图表 | (未开源实现) | Compose Canvas 自绘柱状图 |
| 最低版本 | iOS 17.0+ | Android 8.1+ (API 27) |
| 构建 | Xcode | Gradle + GitHub Actions |

## License

MIT
