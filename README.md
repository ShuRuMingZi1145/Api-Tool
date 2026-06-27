# API Tool

一款运行在 Android 上的 AI IDE 开发助手，集成了代码编辑器、文件管理和 AI Agent，可在手机上完成项目开发。

## 功能

### 多模型 AI 对话
- 支持任意兼容 OpenAI API 的服务商（自定义 URL、Key、模型名）
- 流式输出展示思考过程和最终回答
- 可收起/展开思考内容
- Markdown 渲染（加粗、斜体、代码块、列表等）
- 历史对话管理（搜索、分组、删除）

### IDE 文件编辑器
- 项目化管理文件，树形文件浏览（抽屉式侧栏）
- 代码语法高亮（VS Code Dark+ 配色），支持 Kotlin/Java/Python/JS/TS/Shell 等
- 文件复制、剪切、移动、粘贴、删除
- 保存自动同步到外部存储，文件管理器可见

### AI Agent 模式
- Agent 可自动读写项目文件、创建目录、列出文件
- 支持工具调用（Tool Call）和指令标记（`【READ/WRITE/LIST/MKDIR/DELETE】`）两种模式
- Agent 了解你配置的开发环境，生成适配代码

### 运行代码
- 点击编辑器 ▶ 按钮直接运行代码
- **Python** 使用内嵌的 Python 3.13 解释器（ARM64），不依赖 Termux
- **Shell** 通过 `sh` 执行
- **JavaScript** 通过 WebView 引擎执行
- 其他语言（Node.js、Java、Go、Rust、C/C++ 等）自动检测 Termux 环境执行
- 运行结果在对话框内显示（深色终端风格、等宽字体、可选择文本）

### 开发环境配置
- 首次启动向导选择使用的编程语言
- AI 根据所选语言生成适配代码
- 24 种语言，覆盖后端、前端、系统、移动、数据库、脚本、数据科学

## 截图

| 聊天页面 | IDE 文件编辑 | 环境配置 |
|---------|-------------|---------|
| ![chat](screenshots/chat.png) | ![ide](screenshots/ide.png) | ![setup](screenshots/setup.png) |

## 使用方法

### 1. 添加 AI 服务商
打开设置 → 添加服务商，填入：
- **API URL**：兼容 OpenAI 的聊天补全接口地址
- **API Key**：你的 API 密钥
- **模型名称**：如 `gpt-4o`、`deepseek-chat` 等
- **自定义名称**：（可选）给自己看的别名

### 2. 配置开发环境（可选）
首次启动会自动进入环境配置向导，或随时在设置中点击"配置开发环境"：
- 勾选你使用的编程语言和版本
- AI Agent 会根据此配置生成适配代码

### 3. 开始对话 / IDE 编辑
- **Agents 模式**：顶部切换为 Agents，输入消息即可与 AI 对话
- **IDE 模式**：顶部切换为 IDE，创建项目后进入文件编辑器
  - 点击 📁 打开文件树
  - 选择文件编辑
  - 点击 ▶ 运行当前文件

### 4. 权限说明
- **MANAGE_EXTERNAL_STORAGE**：用于读写外置存储上的项目文件
- **Shizuku（可选）**：检测到 Shizuku 时弹窗请求权限，提升运行代码的能力

## 构建

```bash
git clone https://github.com/ShuRuMingZi1145/Api-Tool.git
cd Api-Tool
./gradlew.bat assembleDebug
```

APK 路径：`app/build/outputs/apk/debug/apitool-1.0.apk`

## 技术栈

- **语言**: Kotlin 2.2.10
- **UI**: Jetpack Compose + Material 3（Compose BOM 2024.12.01）
- **构建**: AGP 9.0.1，targetSdk 36
- **网络**: HttpURLConnection（无额外依赖）
- **存储**: 内部存储 + 外置存储双写
- **内嵌**: Python 3.13（ARM64，Termux 构建，含 libandroid-support）
- **权限**: Shizuku API 13.1.5（可选）

## 下载

[GitHub Releases](https://github.com/ShuRuMingZi1145/Api-Tool/releases) 页面提供 APK 下载。

## 许可

MIT
