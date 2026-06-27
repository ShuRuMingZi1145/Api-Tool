> **出现任何更改或信息都要记录在本文件里**
> **所有回复使用中文**
# Api Tool - Project Memory

> **规则：每次对项目做出更改、修复问题或发生任何相关事件，都必须在此文件中追加记录。** 此要求由用户于 2026-06-19 明确下达，必须遵守。
>
> **构建与部署规则：每次对项目代码做出改动后，必须执行 `.\gradlew.bat assembleDebug` 构建 APK，并通过 adb 安装到手机（`adb -s "<serial>" install -r app\build\outputs\apk\debug\app-debug.apk`）。如有多台设备，先执行 `adb devices` 查看 serial 再指定。**

## Project Overview
Android Jetpack Compose chat app with AI provider integration, project/IDE mode, syntax-highlighted code editor, Agent file operations, and first-launch environment setup wizard.

## Tech Stack
- Kotlin 2.2.10, AGP 9.0.1, Compose BOM 2024.12.01
- Material 3, single-file architecture (all code in `MainActivity.kt`)
- Network: `HttpURLConnection` (no extra dependencies)
- Storage: internal `filesDir` + external `/storage/emulated/0/API Tool配置文件/`

## Version Naming
- Format: `bata0.X` (changed from `data` prefix to `bata`)
- Current: `1.0`
- APK filename: `apitool-1.0.apk`

## Architecture

### Screens
- `Screen.Home` - Main chat interface (also contains side menu, file browser in IDE mode)
- `Screen.Settings` - Provider and environment config
- `Screen.Setup` - First-launch environment wizard (shown when `providers.isEmpty()`)

### Key Objects
- `ApiProvider(customName, apiUrl, apiKey, modelName)` - AI service provider config
- `ChatMessage(role, content, reasoningContent?)` - Chat messages with optional reasoning
- `Conversation(id, title, createdAt, providerIndex, messages)` - Chat history
- `Project(name, path, createdAt)` - Project for IDE mode
- `EnvSelection(name, version)` - Programming language environment config
- `LangEnvironment(name, versions, icon, category)` - Language definition

### File Structure (`MainActivity.kt` ~2800 lines)
- Data classes + `FileOps` + `ConfigStorage` + `ApiService`
- `MainActivity` -> `MainApp` -> routing to screens
- `MainApp` manages: providers, conversations, envSelections, projects, screen state

### ConfigStorage
- File: `providers.json` in internal or external storage
- JSON schema: `{ providers[], conversations[], envSelections[] }`
- Dual-write: internal always + external if permission granted
- Triple return type: `Triple<List<ApiProvider>, List<Conversation>, List<EnvSelection>>`

### Syntax Highlighting (`SyntaxHighlighter` object)
- Regex-based, supports: Kotlin, Java, Python, JS/TS, HTML, XML, CSS
- VS Code Dark+ color scheme:
  - keyword=#569CD6 (blue), controlKeyword=#C586C0 (purple)
  - string=#CE9178 (orange-brown), comment=#6A9955 (green)
  - number=#B5CEA8 (light green), annotation=#CC6666 (red)
  - function=#DCDCAA (yellow), type=#4EC9B0 (teal)
  - attribute=#9CDCFE (light blue), tag=#569CD6 (blue)
- Editor: background `#191A1C`, cursor `#D4D4D4`, text `#D4D4D4`
- Uses `TextFieldValue(annotatedString, selection)` with explicit `TextRange` cursor tracking

### Agent System
- Two modes: Agents (with streaming) and IDE (file operations)
- `parseOpMarkers()` - parses `【READ/WRITE/LIST/MKDIR/DELETE:path】` from AI response
- `sendMessage()` - initial streaming request with SSE parsing
- `sendMessageFollowUp()` - recursive follow-up after tool execution
- System prompt includes env config info when available

### Environment Setup Wizard
- `SetupWizard` - full-screen categorized language list (shown on first launch)
- `EnvConfigDialog` - version selection dialog
- `EnvManagerDialog` - settings access to manage envs
- 24 languages across 8 categories
- AI prompt includes `用户配置的开发环境` section when envs selected

### Key Design Decisions
- Single-file app (no multi-file refactoring)
- Direct `TextFieldValue(AnnotatedString, selection)` for syntax highlighting
- `envSelections` stored in same `providers.json` as third array
- Both agent system prompts (sendMessage + sendMessageFollowUp) get env info
- Setup wizard shown when `providers.isEmpty()` (not `envSelections.isEmpty()`)

## Build & Deploy
- Build: `.\gradlew.bat assembleDebug`
- APK rename: `apitool-1.0.apk`
- Install: `adb -s "<serial>" install -r app\build\outputs\apk\debug\apitool-bata0.4.apk`
- Multiple ADB devices detected; use `-s` flag

## GitHub
- Repo: `ShuRuMingZi1145/Api-Tool`
- Releases: `data0.1`, `data0.2`, `data0.3`, `bata0.4`
- Changelog Unicode: use `[System.IO.File]::WriteAllBytes` + `curl.exe --ssl-no-revoke`
- Asset actions: `DELETE` then `POST` to upload

## Known Issues / TODOs
- Deprecated icons: `Icons.Filled.Send` -> `Icons.AutoMirrored.Filled.Send`, etc.
- `parseInlineMarkdown` doesn't handle unordered lists (- items)

## Change Log

### 2026-06-28 (后续)
- **修复**: Python 执行时报 `libandroid-support.so not found` — 内嵌的 Termux Python 3.13 二进制依赖此库，但 zip 中缺失。从 Termux 仓库下载 `libandroid-support_29-1_aarch64.deb` 提取出 `libandroid-support.so`（20KB）加入 `python.zip` 的 `lib/` 目录
- **修复**: 即使 zip 已更新，旧的提取目录因 `pythonBin` 存在且非 0 字节而跳过重提取，导致 `libandroid-support.so` 仍缺失。提取条件增加 `!androidSupportLib.isFile()` 检查，确保依赖库存在
- **验证**: 通过 `pyelftools` 检查 `python3.13` 和 `libpython3.13.so` 的 `DT_NEEDED`，除 `libandroid-support.so` 外其他依赖均为系统库（`libc.so`、`libm.so`、`libdl.so`、`liblog.so`），现已全部可满足
- **构建**: APK 构建成功并安装到设备，需用户测试 Python 脚本运行
- **发布**: 版本升至 `1.0`（versionCode=5），重写 README.md 为完整项目介绍和使用指南

### 2026-06-28
- **修复**: Python 提取 0 字节的根源 — `pythonBin.exists()` 不检查文件大小，前一次失败遗留的空文件导致后续跳过提取。改为 `pythonBin.isFile() && pythonBin.length() > 0L`
- **修复**: 临时 zip 复制未验证完整性，添加 `tmpZip.length() < 1024L` 校验，及时发现复制失败
- **修复**: Shizuku 权限弹窗不显示 — 无 `ShizukuProvider` 时 `pingBinder()` 抛 `IllegalStateException`，被外层 `catch (_: Exception) {}` 吞掉导致整个 Shizuku 检查跳过。改为先检查包安装再尝试 `pingBinder()`，异常不再阻止弹窗
- **增强**: `build.gradle.kts` 添加 `aaptOptions.noCompress("zip")`，防止 AAPT2 二次压缩 `python.zip` 导致读取异常
- **构建**: APK 构建成功并安装到设备

### 2026-06-27
- **新增**: IDE 模式顶部项目名改为可点击，弹出项目选择对话框（显示项目名和日期），点击即可切换项目
- **新增**: IDE 编辑器添加 ▶ 运行按钮，支持直接运行代码文件：
  - `.sh` — 通过 `Runtime.exec("sh")` 直接执行
  - `.js` / `.mjs` — 通过 WebView 的 JavaScript 引擎执行
  - 其他语言（Python/Java/Kotlin/Go/Rust/C++/C 等）— 通过 `sh -c` 设置 PATH 环境变量包含 Termux 路径来调用系统命令，多重降级尝试（Termux → Termux applets → 直接调用）
  - 输出结果在对话框中显示（深色终端风格，等宽字体，可选择文本）
- **新增**: 环境缺失自动检测与安装引导：
  - 运行时若系统命令不存在，检查 `envSelections` 中是否已配置该语言
  - 已配置 → 弹窗提示安装 Termux，点击"去下载 Termux"打开 GitHub Releases 页（国内可访问）
  - 未配置 → 提示在设置中先配置开发环境
- **修复**: 运行代码功能无法检测到已安装的 Termux 环境。Android 11+ SELinux 禁止直接访问 Termux 私有目录，改用 Termux 的 `RUN_COMMAND` Intent 调用其后台服务来执行代码，通过 `PendingIntent` + `BroadcastReceiver` 获取执行结果
- **修复**: IDE 文件树遮罩层覆盖了文件树面板导致无法点击文件，调换绘制顺序修复
- **文档**: 补充记录项目记忆、提交推送、编写 README、发布 bata0.4 版本
- **修复**: 删除 PROJECT_MEMORY.md 中的 GitHub token（触发 GitHub secret scanning 告警）
- **指令**: 用户要求将所有记忆保存到项目文件中，且此后每次更改/修复/事件都追加记录到本文件。
- **新增**: 设置页添加"配置开发环境 (N)"按钮，点击弹出 `EnvManagerDialog` 管理已选环境
- **修复**: `git checkout` 回滚丢失了之前的环境配置代码（EnvSelection、SetupWizard、EnvConfigDialog、Triple 等），已全部重写恢复
- **修复**: `SetupWizard` 中 `items()` 在 `item {}` 内部调用导致编译错误，改为直接在 `LazyListScope` 级别调用
- **重构**: IDE 布局 — 文件树从固定左侧栏改为抽屉式（点击 📁 按钮弹出，点击遮罩关闭），编辑器占满全屏
- **新增**: 聊天欢迎页 — 当对话为空且已配置服务商时，显示欢迎界面，包含模型选择、当前项目、最近项目
- **新增**: 顶部标题栏增加 Agent 名称副标题（🤖 模型名），用户随时知道当前使用的模型
- **新增**: 历史记录按模型分组，每组显示 🤖 模型名作为分组标题，组间用分割线分隔
- **修改**: 历史记录删除按钮改为 ⋮ 更多菜单（内含"删除"选项），避免误触
- **增强**: 项目卡片增加文件数量和最后修改时间显示
- **增强**: 欢迎页面的最近项目卡片也显示文件数量
- **重构**: 输入框重排 — 输入框 + Agent 切换按钮(🤖/🧑) + 发送按钮全部在一行，紧凑布局
- **新增**: IDE 工具栏增加保存按钮（快捷保存当前文件）
- **新增**: 文件树加 📁/📄 图标前缀
- **新增**: 聊天加载状态指示器 — 思考中/生成中带转圈动画
- **新增**: 侧菜单增加搜索框，可搜索过滤历史对话
- **修改**: 缩小间距 — 欢迎页、侧菜单、项目列表间距收紧减少留白
- **修改**: Toast 反馈增加 ✅ 图标
- **构建**: APK 成功构建安装到设备

### 2026-06-27 (后续)
- **修复**: 内嵌 Python 3.13 解释器提取失败，回退到 Termux/sh 路径导致 `python3: inaccessible or not found` 错误。根因：tar 头部 size 字段以八进制存储，但 Kotlin 提取代码用 `toLongOrNull()`（十进制）解析，导致数据读取错位、后续条目全部损坏。修复：改为 `toLongOrNull(8)` 以八进制解析
- **构建**: 修复后 `.\gradlew.bat assembleDebug` 构建成功，并通过 adb 安装到手机（`adb-269bad10-WpOaz6._adb-tls-connect._tcp`）
- **规则新增**: 记忆文件中添加构建与部署规则：每次代码改动后必须构建 APK 并安装到手机

### 2026-06-27 (后续 #2)
- **UI**: 移除文件编辑标签页右侧的重复"保存"按钮，只保留顶部工具栏项目名右侧的保存按钮
- **UI**: 编辑器背景改为纯黑（`#1E1E1E`），文字白色（`#D4D4D4`），光标白色（`#FFFFFF`），不再跟随系统主题
- **新增**: 编辑器代码语法高亮 — 基于正则的 `VisualTransformation`，支持 Kotlin/Java/Python/JS/TS/HTML/CSS/Shell 等语言的关键字、字符串、数字、注释着色（VS Code Dark+ 配色）
- **修复**: Python 提取改为先删除旧的有损提取目录再重新提取，提取失败时不再静默吞异常，而是显示具体错误信息

### 2026-06-27 (后续 #3)
- **修复**: Python assets 路径问题 — Android 构建系统在合并 assets 阶段会自动解压 `.tar.gz` 文件，导致 APK 内路径变成 `python/python.tar`（不含 `.gz`），`GZIPInputStream` 因数据已是原始 tar 而读取失败。修复：将资源文件重命名为 `python.tar.pkg`（避开 `.gz` 后缀自动解压），Kotlin 代码对应改为打开 `python/python.tar.pkg`
- **UI**: 编辑器背景改为 `#191A1C`（按用户要求）
- **UI**: 软件全局主题改为固定深色模式，背景色 `#26282C`，不再跟随系统亮暗切换
- **规则新增**: 所有回复使用中文

### 2026-06-27 (后续 #4)
- **修复**: `python.tar.pkg` 仍导致 "Invalid file path" 错误，可能是 Android AssetManager 对含点扩展名的路径有限制。改为无扩展名文件名 `python`，`ctx.assets.open("python/python")` + `GZIPInputStream` 正常工作，APK 大小保持 6.3MB
- **修复**: 提取成功后执行时报 `Permission denied (error=13)` — `/data/data` 分区可能挂载了 `noexec`，`setExecutable()` 和 `chmod` 均无效。改为双重尝试：先直接执行二进制，若失败则通过 `/system/bin/linker64` 加载 ELF（linker64 本身有执行权限，读取 ELF 文件只需读权限）
- **修复**: 改用 zip 替代 tar — 手动 tar 解析在 Android 上仍有问题（文件被写 0 字节），改用 Java 标准库 `ZipInputStream` 提取 `python.zip`，更稳定可靠。APK 大小增至 17.4 MB（python.zip 压缩后 7.4 MB）

### 2026-06-27 (后续 #5)
- **修复**: `ZipInputStream` 在 Android 上仍输出 0 字节，改为将 zip 写入 `cacheDir` 临时文件后用 `ZipFile`（基于文件）提取，`ZipFile` 比 `ZipInputStream` 更稳定可靠
- **新增**: 启动时检测 `MANAGE_EXTERNAL_STORAGE` 权限，未授权则弹窗跳转设置页
- **新增**: 检测 Shizuku 安装，如有则弹窗申请 Shizuku 权限（集成 `dev.rikka.shizuku:api:13.1.5`）
- **构建**: 集成 Shizuku API，更新 AndroidManifest 添加 ShizukuProvider
