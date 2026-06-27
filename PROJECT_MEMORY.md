# Api Tool - Project Memory

> **规则：每次对项目做出更改、修复问题或发生任何相关事件，都必须在此文件中追加记录。** 此要求由用户于 2026-06-19 明确下达，必须遵守。

## Project Overview
Android Jetpack Compose chat app with AI provider integration, project/IDE mode, syntax-highlighted code editor, Agent file operations, and first-launch environment setup wizard.

## Tech Stack
- Kotlin 2.2.10, AGP 9.0.1, Compose BOM 2024.12.01
- Material 3, single-file architecture (all code in `MainActivity.kt`)
- Network: `HttpURLConnection` (no extra dependencies)
- Storage: internal `filesDir` + external `/storage/emulated/0/API Tool配置文件/`

## Version Naming
- Format: `bata0.X` (changed from `data` prefix to `bata`)
- Current: `bata0.4`
- APK filename: `apitool-{versionName}.apk`

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
- APK rename: `apitool-bata0.4.apk`
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

### 2026-06-19
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
