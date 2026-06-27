# Api Tool

一款运行在 Android 上的 AI 开发助手，支持多服务商对话、IDE 文件编辑、代码语法高亮和 Agent 自动化操作。

## 功能

- **多模型对话** — 流式输出、Markdown 渲染、思考过程展开、历史记录管理
- **项目系统** — 创建/管理项目，按项目组织文件
- **IDE 模式** — 树形文件浏览器（抽屉式）、代码编辑器、语法高亮（VS Code Dark+ 配色）
- **Agent 模式** — AI 自动读写文件、执行工具调用
- **环境配置** — 首次启动向导选择编程语言，AI 据此生成适配代码
- **多服务商** — 支持任意兼容 OpenAI API 的服务商配置

## 截图

| 聊天页面 | IDE 文件编辑 | 环境配置 |
|---------|-------------|---------|
| ![chat](screenshots/chat.png) | ![ide](screenshots/ide.png) | ![setup](screenshots/setup.png) |

## 构建

```bash
git clone https://github.com/ShuRuMingZi1145/Api-Tool.git
cd Api-Tool
./gradlew.bat assembleDebug
```

APK 路径：`app/build/outputs/apk/debug/apitool-{version}.apk`

## 技术栈

- **语言**: Kotlin 2.2.10
- **UI**: Jetpack Compose + Material 3（Compose BOM 2024.12.01）
- **构建**: AGP 9.0.1
- **网络**: HttpURLConnection（无额外依赖）
- **存储**: 内部存储 + 外置存储双写

## 发布

当前版本：`bata0.4`

下载地址：[GitHub Releases](https://github.com/ShuRuMingZi1145/Api-Tool/releases)
