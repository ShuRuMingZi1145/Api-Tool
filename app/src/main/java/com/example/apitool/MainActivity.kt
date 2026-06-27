package com.example.apitool

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apitool.ui.theme.ApiToolTheme
import rikka.shizuku.Shizuku
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class ApiProvider(
    val customName: String = "",
    val apiUrl: String,
    val apiKey: String,
    val modelName: String
)

data class ChatMessage(
    val role: String,
    val content: String,
    val reasoningContent: String? = null
)

data class Conversation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "新对话",
    val createdAt: Long = System.currentTimeMillis(),
    val messages: List<ChatMessage> = emptyList(),
    val providerIndex: Int = 0
)

data class Project(
    val name: String,
    val path: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class EnvSelection(
    val name: String,
    val version: String
)

data class LangEnvironment(
    val name: String,
    val versions: List<String>,
    val icon: String,
    val category: String
)

val allEnvironments = listOf(
    LangEnvironment("Python", listOf("3.12", "3.11", "3.10", "3.9", "3.8", "2.7"), "🐍", "后端语言"),
    LangEnvironment("Java", listOf("21", "17", "11", "8"), "☕", "后端语言"),
    LangEnvironment("Kotlin", listOf("2.0", "1.9", "1.8"), "🅺", "后端语言"),
    LangEnvironment("Go", listOf("1.22", "1.21", "1.20"), "🔵", "后端语言"),
    LangEnvironment("Rust", listOf("1.78", "1.77", "1.76"), "🦀", "后端语言"),
    LangEnvironment("C#", listOf(".NET 8", ".NET 6", "Mono"), "#️⃣", "后端语言"),
    LangEnvironment("Ruby", listOf("3.3", "3.2", "3.1"), "💎", "后端语言"),
    LangEnvironment("PHP", listOf("8.3", "8.2", "8.1"), "🐘", "后端语言"),
    LangEnvironment("Scala", listOf("3.4", "3.3", "2.13"), "🔷", "后端语言"),
    LangEnvironment("JavaScript (Node.js)", listOf("22", "20", "18", "16"), "🟨", "前端/全栈"),
    LangEnvironment("TypeScript", listOf("5.4", "5.3", "5.2"), "🟦", "前端/全栈"),
    LangEnvironment("JavaScript (Deno)", listOf("1.42", "1.40"), "🟦", "前端/全栈"),
    LangEnvironment("HTML/CSS", listOf("HTML5", "HTML4"), "🌐", "前端/全栈"),
    LangEnvironment("Vue.js", listOf("3.4", "3.3", "2.7"), "💚", "前端/全栈"),
    LangEnvironment("React", listOf("18", "17", "16"), "⚛️", "前端/全栈"),
    LangEnvironment("C++", listOf("C++23", "C++20", "C++17", "C++14", "C++11"), "⚡", "系统/底层"),
    LangEnvironment("C", listOf("C23", "C17", "C11", "C99"), "🔧", "系统/底层"),
    LangEnvironment("Android (Kotlin)", listOf("API 34", "API 33", "API 32"), "🤖", "移动开发"),
    LangEnvironment("Swift", listOf("5.10", "5.9", "5.8"), "🍎", "移动开发"),
    LangEnvironment("SQL", listOf("SQLite", "PostgreSQL", "MySQL", "SQL Server"), "🗄️", "数据库"),
    LangEnvironment("Bash/Shell", listOf("Bash 5", "Bash 4", "Zsh"), "⌨️", "脚本/运维"),
    LangEnvironment("Lua", listOf("5.4", "5.3", "5.2"), "🌙", "脚本/运维"),
    LangEnvironment("R", listOf("4.4", "4.3", "4.2"), "📊", "数据科学"),
    LangEnvironment("MATLAB", listOf("R2024a", "R2023b", "R2023a"), "📈", "数据科学"),
    LangEnvironment("Arduino", listOf("1.8", "1.6"), "🔌", "脚本/嵌入式"),
)

val categoryOrder = listOf("后端语言", "前端/全栈", "系统/底层", "移动开发", "数据库", "脚本/运维", "数据科学", "脚本/嵌入式")

object FileOps {
    private const val PROJECTS_DIR = "API Tool配置文件"

    fun getExternalBase(): java.io.File =
        java.io.File(Environment.getExternalStorageDirectory(), "$PROJECTS_DIR/projects")

    private fun getInternalBase(context: Context): java.io.File =
        java.io.File(context.filesDir, "projects")

    private fun getProjectDir(context: Context, name: String): java.io.File {
        val ext = getExternalBase()
        if (ext.exists() || ext.mkdirs()) {
            val dir = java.io.File(ext, name)
            dir.mkdirs()
            return dir
        }
        val dir = java.io.File(getInternalBase(context), name)
        dir.mkdirs()
        return dir
    }

    fun loadProjects(context: Context): List<Project> {
        val list = mutableListOf<Project>()
        val ext = getExternalBase()
        val int = getInternalBase(context)

        // Migrate projects from internal → external so they're visible via file manager
        if (int.exists() && (ext.exists() || ext.mkdirs())) {
            int.listFiles()?.filter { it.isDirectory }?.forEach { f ->
                val extDir = java.io.File(ext, f.name)
                if (!extDir.exists()) {
                    try { f.copyRecursively(extDir, true); f.deleteRecursively() } catch (_: Exception) { }
                } else {
                    try { f.deleteRecursively() } catch (_: Exception) { }
                }
            }
        }

        if (ext.exists()) {
            ext.listFiles()?.filter { it.isDirectory }?.forEach { f ->
                list.add(Project(name = f.name, path = f.absolutePath, createdAt = f.lastModified()))
            }
        } else if (int.exists()) {
            int.listFiles()?.filter { it.isDirectory }?.forEach { f ->
                list.add(Project(name = f.name, path = f.absolutePath, createdAt = f.lastModified()))
            }
        }

        return list.sortedByDescending { it.createdAt }
    }

    fun createProject(context: Context, name: String): Project {
        val dir = getProjectDir(context, name)
        return Project(name = name, path = dir.absolutePath)
    }

    fun listFiles(projectPath: String): List<java.io.File> {
        val dir = java.io.File(projectPath)
        if (!dir.exists()) return emptyList()
        return dir.listFiles()?.sortedWith(compareBy({ it.isDirectory }, { it.name })) ?: emptyList()
    }

    fun readFile(filePath: String): String {
        return java.io.File(filePath).readText(Charsets.UTF_8)
    }

    fun writeFile(filePath: String, content: String) {
        java.io.File(filePath).writeText(content, Charsets.UTF_8)
    }

    fun deleteFile(filePath: String) {
        java.io.File(filePath).delete()
    }

    fun isTextFile(file: java.io.File): Boolean {
        val name = file.name.lowercase()
        val textExtensions = setOf("txt", "kt", "java", "py", "js", "ts", "html", "css", "xml", "json", "yml", "yaml", "md", "cfg", "conf", "ini", "sh", "bat", "ps1", "gradle", "kts", "properties", "env", "gitignore")
        return !name.contains(".") || name.substringAfterLast(".") in textExtensions
    }
}

object ConfigStorage {
    private const val DIR_NAME = "API Tool配置文件"
    private const val FILE_NAME = "providers.json"

    private fun getExternalDir(): java.io.File =
        java.io.File(Environment.getExternalStorageDirectory(), DIR_NAME)

    private fun getExternalFile(): java.io.File =
        java.io.File(getExternalDir(), FILE_NAME)

    private fun getInternalFile(context: Context): java.io.File =
        java.io.File(context.filesDir, FILE_NAME)

    private fun toJson(providers: List<ApiProvider>, conversations: List<Conversation>, envSelections: List<EnvSelection> = emptyList()): String =
        JSONObject().apply {
            put("providers", JSONArray().apply {
                providers.forEach { p ->
                    put(JSONObject().apply {
                        put("customName", p.customName)
                        put("apiUrl", p.apiUrl)
                        put("apiKey", p.apiKey)
                        put("modelName", p.modelName)
                    })
                }
            })
            put("conversations", JSONArray().apply {
                conversations.forEach { c ->
                    put(JSONObject().apply {
                        put("id", c.id)
                        put("title", c.title)
                        put("createdAt", c.createdAt)
                        put("providerIndex", c.providerIndex)
                        put("messages", JSONArray().apply {
                            c.messages.forEach { m ->
                                put(JSONObject().apply {
                                    put("role", m.role)
                                    put("content", m.content)
                                    m.reasoningContent?.let { put("reasoningContent", it) }
                                })
                            }
                        })
                    })
                }
            })
            put("envSelections", JSONArray().apply {
                envSelections.forEach { e ->
                    put(JSONObject().apply {
                        put("name", e.name)
                        put("version", e.version)
                    })
                }
            })
        }.toString(2)

    private fun parseJson(text: String): Triple<List<ApiProvider>, List<Conversation>, List<EnvSelection>> {
        val root = JSONObject(text)
        val providers = root.getJSONArray("providers").let { arr ->
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                ApiProvider(
                    customName = obj.optString("customName", ""),
                    apiUrl = obj.getString("apiUrl"),
                    apiKey = obj.getString("apiKey"),
                    modelName = obj.getString("modelName")
                )
            }
        }
        val conversations = root.getJSONArray("conversations").let { arr ->
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val msgs = obj.getJSONArray("messages").let { ma ->
                    (0 until ma.length()).map { mi ->
                        val mo = ma.getJSONObject(mi)
                        ChatMessage(mo.getString("role"), mo.getString("content"), mo.optString("reasoningContent", "").takeIf { it.isNotEmpty() })
                    }
                }
                Conversation(
                    id = obj.getString("id"),
                    title = obj.optString("title", "新对话"),
                    createdAt = obj.getLong("createdAt"),
                    messages = msgs,
                    providerIndex = obj.optInt("providerIndex", 0)
                )
            }
        }
        val envSelections = if (root.has("envSelections")) {
            root.getJSONArray("envSelections").let { arr ->
                (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    EnvSelection(name = obj.getString("name"), version = obj.getString("version"))
                }
            }
        } else emptyList()
        return Triple(providers, conversations, envSelections)
    }

    fun save(context: Context, providers: List<ApiProvider>, conversations: List<Conversation>, envSelections: List<EnvSelection> = emptyList()) {
        try {
            val json = toJson(providers, conversations, envSelections)
            // Always save to internal storage (no permission needed)
            getInternalFile(context).writeText(json, Charsets.UTF_8)
            // Also try external if permission granted
            if (hasExternalPermission(context)) {
                getExternalDir().mkdirs()
                getExternalFile().writeText(json, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun load(context: Context): Triple<List<ApiProvider>, List<Conversation>, List<EnvSelection>> {
        // Try external first, fall back to internal
        val file = if (getExternalFile().exists()) getExternalFile() else getInternalFile(context)
        return try {
            if (!file.exists()) return Triple(emptyList(), emptyList(), emptyList())
            parseJson(file.readText(Charsets.UTF_8))
        } catch (e: Exception) {
            // Try internal as last resort
            try {
                if (!getInternalFile(context).exists()) return Triple(emptyList(), emptyList(), emptyList())
                parseJson(getInternalFile(context).readText(Charsets.UTF_8))
            } catch (e2: Exception) {
                Triple(emptyList(), emptyList(), emptyList())
            }
        }
    }

    fun hasExternalPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}

object ApiService {
    private val mainHandler = Handler(Looper.getMainLooper())

    private fun resolvePath(projectPath: String, path: String): java.io.File {
        val f = java.io.File(path)
        return if (f.isAbsolute) f else java.io.File(projectPath, path)
    }

    private val toolDefinitions = JSONArray().apply {
        put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", "read_file")
                put("description", "读取项目中的文件内容")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("path", JSONObject().apply {
                            put("type", "string")
                            put("description", "文件在项目中的相对路径")
                        })
                    })
                    put("required", JSONArray().put("path"))
                })
            })
        })
        put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", "write_file")
                put("description", "写入内容到项目中的文件（覆盖已有内容）")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("path", JSONObject().apply {
                            put("type", "string")
                            put("description", "文件在项目中的相对路径")
                        })
                        put("content", JSONObject().apply {
                            put("type", "string")
                            put("description", "要写入的文件内容")
                        })
                    })
                    put("required", JSONArray().put("path").put("content"))
                })
            })
        })
        put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", "create_file")
                put("description", "在项目中创建新文件并写入内容")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("path", JSONObject().apply {
                            put("type", "string")
                            put("description", "文件在项目中的相对路径")
                        })
                        put("content", JSONObject().apply {
                            put("type", "string")
                            put("description", "要写入的文件内容")
                        })
                    })
                    put("required", JSONArray().put("path").put("content"))
                })
            })
        })
        put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", "list_files")
                put("description", "列出项目目录中的所有文件")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("path", JSONObject().apply {
                            put("type", "string")
                            put("description", "子目录相对路径（留空列出根目录）")
                        })
                    })
                    put("required", JSONArray())
                })
            })
        })
        put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", "create_directory")
                put("description", "在项目中创建新目录")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("path", JSONObject().apply {
                            put("type", "string")
                            put("description", "目录在项目中的相对路径")
                        })
                    })
                    put("required", JSONArray().put("path"))
                })
            })
        })
        put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", "delete_file")
                put("description", "删除项目中的文件或空目录")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("path", JSONObject().apply {
                            put("type", "string")
                            put("description", "文件或目录在项目中的相对路径")
                        })
                    })
                    put("required", JSONArray().put("path"))
                })
            })
        })
    }

    fun executeToolCall(projectPath: String, funcName: String, argsJson: String): String {
        return try {
            val args = JSONObject(argsJson)
            fun getPath(): String = args.optString("path", args.optString("relative_path", "")).takeIf { it.isNotEmpty() } ?: "."
            when (funcName) {
                "read_file" -> {
                    val absPath = resolvePath(projectPath, getPath()).absolutePath
                    if (!java.io.File(absPath).exists()) return "文件不存在: $absPath"
                    FileOps.readFile(absPath)
                }
                "write_file", "create_file" -> {
                    val path = getPath()
                    val content = args.optString("content", "")
                    val absPath = resolvePath(projectPath, path).absolutePath
                    java.io.File(absPath).parentFile?.mkdirs()
                    FileOps.writeFile(absPath, content)
                    "已写入: $absPath"
                }
                "list_files" -> {
                    val subPath = args.optString("path", args.optString("relative_path", ""))
                    val dir = resolvePath(projectPath, subPath)
                    val files = FileOps.listFiles(dir.absolutePath)
                    if (files.isEmpty()) "(空目录) ${dir.absolutePath}"
                    else files.joinToString("\n") { f ->
                        val prefix = if (f.isDirectory) "[目录] " else "[文件] "
                        val relPath = if (subPath.isEmpty()) f.name else "$subPath/${f.name}"
                        "$prefix$relPath"
                    }
                }
                "create_directory" -> {
                    val absPath = resolvePath(projectPath, getPath())
                    absPath.mkdirs()
                    "目录已创建"
                }
                "delete_file" -> {
                    val absPath = resolvePath(projectPath, getPath())
                    absPath.delete()
                    "已删除"
                }
                else -> {
                    val path = getPath()
                    val content = args.optString("content", "")
                    if (path != "." && content.isNotEmpty()) {
                        val absPath = resolvePath(projectPath, path).absolutePath
                        java.io.File(absPath).parentFile?.mkdirs()
                        FileOps.writeFile(absPath, content)
                        "已写入: $absPath"
                    } else if (path != ".") {
                        "未知操作: $funcName"
                    } else {
                        "未知操作: $funcName"
                    }
                }
            }
        } catch (e: Exception) {
            "操作失败: ${e.message}"
        }
    }

    fun sendMessage(
        provider: ApiProvider,
        message: String,
        history: List<ChatMessage>,
        projectPath: String?,
        envSelections: List<EnvSelection> = emptyList(),
        onReasoning: (String) -> Unit = {},
        onContent: (String) -> Unit = {},
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        Thread {
            try {
                val isAgentsMode = projectPath != null
                val body = JSONObject().apply {
                    put("model", provider.modelName)
                    put("messages", JSONArray().apply {
                        if (isAgentsMode) {
                            put(JSONObject().apply {
                                put("role", "system")
                                put("content", buildString {
                                    append("你是 IDE Agent，运行在 Android 设备上。")
                                    append("你拥有对项目目录的完整读写权限。")
                                    append("项目根目录: $projectPath\n\n")
                                    if (envSelections.isNotEmpty()) {
                                        append("用户配置的开发环境:\n")
                                        envSelections.forEach { e ->
                                            append("- ${e.name} ${e.version}\n")
                                        }
                                        append("\n请根据上述环境生成适配的代码。\n\n")
                                    }
                                    append("请直接在回复中使用以下指令操作文件，不要只提供建议：\n")
                                    append("- 【READ:相对路径】- 读取文件内容\n")
                                    append("- 【WRITE:相对路径】后跟文件内容再以【END_WRITE】结束 - 写入文件\n")
                                    append("- 【LIST:相对路径】- 列出目录文件\n")
                                    append("- 【MKDIR:相对路径】- 创建目录\n")
                                    append("- 【DELETE:相对路径】- 删除文件或空目录\n\n")
                                    append("示例：要读取 src/Main.kt，回复【READ:src/Main.kt】即可。")
                                    append("要写入文件，回复【WRITE:test.txt】文件内容【END_WRITE】。")
                                })
                            })
                        }
                        history.forEach { h ->
                            put(JSONObject().apply {
                                put("role", h.role)
                                put("content", h.content)
                            })
                        }
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", message)
                        })
                    })
                    put("stream", true)
                }

                val conn = URL(provider.apiUrl).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Accept", "text/event-stream")
                conn.setRequestProperty("Authorization", "Bearer ${provider.apiKey}")
                conn.doOutput = true
                conn.connectTimeout = 30000
                conn.readTimeout = 0

                OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

                if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                    val errorBody = try {
                        BufferedReader(InputStreamReader(conn.errorStream, Charsets.UTF_8)).readText()
                    } catch (_: Exception) { "" }
                    mainHandler.post { onError("HTTP ${conn.responseCode}: ${errorBody.take(500)}") }
                    conn.disconnect()
                    return@Thread
                }

                val reader = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8))
                var reasoning = StringBuilder()
                var content = StringBuilder()
                // Collect tool calls during streaming
                data class ToolCallData(val id: String, val name: String, val arguments: String)
                val toolCallList = mutableListOf<ToolCallData>()

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val l = line ?: continue
                    if (!l.startsWith("data: ")) continue
                    val data = l.removePrefix("data: ").trim()
                    if (data == "[DONE]") break

                    try {
                        val chunk = JSONObject(data)
                        val choices = chunk.optJSONArray("choices")
                        if (choices == null || choices.length() == 0) continue
                        val choice = choices.getJSONObject(0)
                        val delta = choice.optJSONObject("delta") ?: continue

                        val rcObj = delta.opt("reasoning_content")
                        if (rcObj is String && rcObj.isNotEmpty()) {
                            reasoning.append(rcObj)
                            mainHandler.post { onReasoning(reasoning.toString()) }
                        }
                        val cObj = delta.opt("content")
                        if (cObj is String && cObj.isNotEmpty()) {
                            content.append(cObj)
                            mainHandler.post { onContent(content.toString()) }
                        }

                        // Handle tool_calls delta
                        val tcArr = delta.optJSONArray("tool_calls")
                        if (tcArr != null) {
                            for (i in 0 until tcArr.length()) {
                                val tc = tcArr.getJSONObject(i)
                                val idx = tc.getInt("index")
                                val existing = if (idx < toolCallList.size) toolCallList[idx]
                                    else ToolCallData("", "", "")
                                val id = tc.optString("id", existing.id).takeIf { it.isNotEmpty() } ?: existing.id
                                val func = tc.optJSONObject("function")
                                val name = if (func != null) func.optString("name", existing.name).takeIf { it.isNotEmpty() } ?: existing.name else existing.name
                                val argsPart = if (func != null) func.optString("arguments", "") else ""
                                val arguments = existing.arguments + argsPart
                                if (idx < toolCallList.size) {
                                    toolCallList[idx] = ToolCallData(id, name, arguments)
                                } else {
                                    toolCallList.add(ToolCallData(id, name, arguments))
                                }
                            }
                        }
                    } catch (_: Exception) { }
                }
                reader.close()
                conn.disconnect()

                // If we have valid tool calls, execute them and send follow-up
                val validToolCalls = toolCallList.filter { it.name.isNotEmpty() }
                if (isAgentsMode && validToolCalls.isNotEmpty()) {
                    val toolCallMessages = mutableListOf<ChatMessage>()
                    val toolResults = mutableListOf<ChatMessage>()
                    validToolCalls.forEachIndexed { _, tc ->
                        val result = executeToolCall(projectPath!!, tc.name, tc.arguments)
                        toolCallMessages.add(ChatMessage("assistant_tool_call", JSONObject().apply {
                            put("tool_calls", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("id", tc.id)
                                    put("type", "function")
                                    put("function", JSONObject().apply {
                                        put("name", tc.name)
                                        put("arguments", tc.arguments)
                                    })
                                })
                            })
                        }.toString()))
                        toolResults.add(ChatMessage("tool", JSONObject().apply {
                            put("tool_call_id", tc.id)
                            put("content", result)
                        }.toString()))
                    }

                    // Send follow-up with proper tool call + tool result messages
                    val followHistory = mutableListOf<ChatMessage>()
                    followHistory.addAll(history)
                    followHistory.add(ChatMessage("user", message))
                    followHistory.addAll(toolCallMessages)
                    followHistory.addAll(toolResults)
                    sendMessageFollowUp(provider, followHistory, projectPath, envSelections, onReasoning, onContent, onDone, onError)
                } else if (isAgentsMode) {
                    val responseText = content.toString()
                    val ops = parseOpMarkers(responseText)
                    if (ops.isNotEmpty()) {
                        val results = mutableListOf<String>()
                        ops.forEach { (type, path, extra) ->
                            val absPath = resolvePath(projectPath!!, path).absolutePath
                            val res = try {
                                when (type) {
                                    "READ" -> if (java.io.File(absPath).exists()) FileOps.readFile(absPath) else "文件不存在: $absPath"
                                    "WRITE" -> {
                                        java.io.File(absPath).parentFile?.mkdirs()
                                        FileOps.writeFile(absPath, extra)
                                        "已写入: $absPath"
                                    }
                                    "LIST" -> {
                                        if (!java.io.File(absPath).exists()) "目录不存在: $absPath"
                                        else FileOps.listFiles(absPath).let { files ->
                                            if (files.isEmpty()) "(空目录) $absPath"
                                            else files.joinToString("\n") { f -> "${if (f.isDirectory) "[DIR] " else ""}${f.name}" }
                                        }
                                    }
                                    "MKDIR" -> { java.io.File(absPath).mkdirs(); "目录已创建: $path" }
                                    "DELETE" -> { java.io.File(absPath).delete(); "已删除: $path" }
                                    else -> "未知指令: $type"
                                }
                            } catch (e: Exception) { "操作失败: ${e.message}" }
                            results.add(res)
                        }
                        val resultBlock = results.joinToString("\n---\n")
                        mainHandler.post { onContent(responseText + "\n\n【执行结果】\n" + resultBlock) }
                        val fh = mutableListOf<ChatMessage>()
                        fh.addAll(history)
                        fh.add(ChatMessage("user", message))
                        fh.add(ChatMessage("assistant", responseText))
                        sendMessageFollowUp(provider, fh, projectPath, envSelections, onReasoning, onContent, onDone, onError)
                    } else {
                        mainHandler.post { onDone() }
                    }
                } else {
                    mainHandler.post { onDone() }
                }
            } catch (e: Exception) {
                mainHandler.post { onError(e.message ?: "请求失败") }
            }
        }.start()
    }

    private fun parseOpMarkers(text: String): List<Triple<String, String, String>> {
        val results = mutableListOf<Triple<String, String, String>>()
        var remaining = text
        val regex = Regex("【(READ|WRITE|LIST|MKDIR|DELETE):(.+?)】")
        while (true) {
            val m = regex.find(remaining)
            if (m == null) break
            val type = m.groupValues[1]
            val path = m.groupValues[2].trim()
            val start = m.range.last + 1
            if (type == "WRITE") {
                val endTag = "【END_WRITE】"
                val endIdx = remaining.indexOf(endTag, start)
                if (endIdx >= 0) {
                    val content = remaining.substring(start, endIdx).trim()
                    results.add(Triple("WRITE", path, content))
                    remaining = remaining.substring(endIdx + endTag.length)
                } else {
                    remaining = remaining.substring(start)
                }
            } else {
                results.add(Triple(type, path, ""))
                remaining = remaining.substring(start)
            }
        }
        return results
    }

    private fun sendMessageFollowUp(
        provider: ApiProvider,
        history: List<ChatMessage>,
        projectPath: String?,
        envSelections: List<EnvSelection> = emptyList(),
        onReasoning: (String) -> Unit,
        onContent: (String) -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            try {
                val isAgentsMode = projectPath != null
                val body = JSONObject().apply {
                    put("model", provider.modelName)
                    put("messages", JSONArray().apply {
                        if (isAgentsMode) {
                            put(JSONObject().apply {
                                put("role", "system")
                                put("content", buildString {
                                    append("你是 IDE Agent，运行在 Android 设备上。")
                                    append("你拥有对项目目录的完整读写权限。")
                                    append("项目根目录: $projectPath\n\n")
                                    if (envSelections.isNotEmpty()) {
                                        append("用户配置的开发环境:\n")
                                        envSelections.forEach { e ->
                                            append("- ${e.name} ${e.version}\n")
                                        }
                                        append("\n请根据上述环境生成适配的代码。\n\n")
                                    }
                                    append("请直接在回复中使用以下指令操作文件，不要只提供建议：\n")
                                    append("- 【READ:相对路径】- 读取文件内容\n")
                                    append("- 【WRITE:相对路径】后跟文件内容再以【END_WRITE】结束 - 写入文件\n")
                                    append("- 【LIST:相对路径】- 列出目录文件\n")
                                    append("- 【MKDIR:相对路径】- 创建目录\n")
                                    append("- 【DELETE:相对路径】- 删除文件或空目录")
                                })
                            })
                        }
                        history.forEach { h ->
                            if (h.role == "tool") {
                                val toolJson = JSONObject(h.content)
                                put(JSONObject().apply {
                                    put("role", "tool")
                                    put("content", toolJson.getString("content"))
                                    put("tool_call_id", toolJson.getString("tool_call_id"))
                                })
                            } else if (h.role == "assistant_tool_call") {
                                val parsed = JSONObject(h.content)
                                put(JSONObject().apply {
                                    put("role", "assistant")
                                    put("content", JSONObject.NULL)
                                    put("tool_calls", parsed.getJSONArray("tool_calls"))
                                })
                            } else {
                                put(JSONObject().apply {
                                    put("role", h.role)
                                    put("content", h.content)
                                })
                            }
                        }
                    })
                    put("stream", true)
                }

                val conn = URL(provider.apiUrl).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Accept", "text/event-stream")
                conn.setRequestProperty("Authorization", "Bearer ${provider.apiKey}")
                conn.doOutput = true
                conn.connectTimeout = 30000
                conn.readTimeout = 0

                OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

                if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                    val errorBody = try {
                        BufferedReader(InputStreamReader(conn.errorStream, Charsets.UTF_8)).readText()
                    } catch (_: Exception) { "" }
                    mainHandler.post { onError("HTTP ${conn.responseCode}: ${errorBody.take(500)}") }
                    conn.disconnect()
                    return@Thread
                }

                val reader = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8))
                var reasoning = StringBuilder()
                var content = StringBuilder()

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val l = line ?: continue
                    if (!l.startsWith("data: ")) continue
                    val data = l.removePrefix("data: ").trim()
                    if (data == "[DONE]") break

                    try {
                        val chunk = JSONObject(data)
                        val choices = chunk.optJSONArray("choices")
                        if (choices == null || choices.length() == 0) continue
                        val delta = choices.getJSONObject(0).optJSONObject("delta") ?: continue
                        val rcObj = delta.opt("reasoning_content")
                        if (rcObj is String && rcObj.isNotEmpty()) {
                            reasoning.append(rcObj)
                            mainHandler.post { onReasoning(reasoning.toString()) }
                        }
                        val cObj = delta.opt("content")
                        if (cObj is String && cObj.isNotEmpty()) {
                            content.append(cObj)
                            mainHandler.post { onContent(content.toString()) }
                        }
                    } catch (_: Exception) { }
                }
                reader.close()
                conn.disconnect()

                val responseText = content.toString()
                if (isAgentsMode) {
                    val ops = parseOpMarkers(responseText)
                    if (ops.isNotEmpty()) {
                        val results = mutableListOf<String>()
                        ops.forEach { (type, path, extra) ->
                            val absPath = resolvePath(projectPath!!, path).absolutePath
                            val res = try {
                                 when (type) {
                                        "READ" -> if (java.io.File(absPath).exists()) FileOps.readFile(absPath) else "文件不存在: $absPath"
                                        "WRITE" -> {
                                            java.io.File(absPath).parentFile?.mkdirs()
                                            FileOps.writeFile(absPath, extra)
                                            "已写入: $absPath"
                                        }
                                        "LIST" -> {
                                            if (!java.io.File(absPath).exists()) "目录不存在: $absPath"
                                            else FileOps.listFiles(absPath).let { files ->
                                                if (files.isEmpty()) "(空目录) $absPath"
                                                else files.joinToString("\n") { f -> "${if (f.isDirectory) "[DIR] " else ""}${f.name}" }
                                            }
                                        }
                                        "MKDIR" -> { java.io.File(absPath).mkdirs(); "目录已创建: $path" }
                                        "DELETE" -> { java.io.File(absPath).delete(); "已删除: $path" }
                                        else -> "未知指令: $type"
                                    }
                                } catch (e: Exception) { "操作失败: ${e.message}" }
                            results.add(res)
                        }
                        val resultBlock = results.joinToString("\n---\n")
                        mainHandler.post { onContent(responseText + "\n\n【执行结果】\n" + resultBlock) }
                    } else {
                        mainHandler.post { onDone() }
                    }
                } else {
                    mainHandler.post { onDone() }
                }
            } catch (e: Exception) {
                mainHandler.post { onError(e.message ?: "请求失败") }
            }
        }.start()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApiToolTheme {
                MainApp(appContext = this)
            }
        }
    }
}

enum class Screen { Home, Settings, Setup }

@Composable
fun MainApp(appContext: Context) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    val providers = remember { mutableStateListOf<ApiProvider>() }
    val conversations = remember { mutableStateListOf<Conversation>() }
    var currentConvId by remember { mutableStateOf("") }
    var isAgentsMode by remember { mutableStateOf(true) }
    var isAgentEnabled by remember { mutableStateOf(false) }
    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var currentProject by remember { mutableStateOf<Project?>(null) }
    var refreshingProject by remember { mutableStateOf(false) }
    val envSelections = remember { mutableStateListOf<EnvSelection>() }

    LaunchedEffect(Unit) {
        val (p, c, e) = ConfigStorage.load(appContext)
        providers.addAll(p)
        conversations.addAll(c)
        envSelections.addAll(e)
        if (providers.isEmpty()) {
            currentScreen = Screen.Setup
        } else {
            if (conversations.isEmpty()) {
                val conv = Conversation()
                conversations.add(conv)
                currentConvId = conv.id
            } else {
                currentConvId = conversations.last().id
            }
        }
        projects = FileOps.loadProjects(appContext)
    }

    LaunchedEffect(providers.toList(), conversations.toList(), envSelections.toList()) {
        if (providers.isNotEmpty() || envSelections.isNotEmpty()) {
            ConfigStorage.save(appContext, providers, conversations, envSelections)
        }
    }

    // --- Permission dialogs ---
    var showStoragePermDialog by remember { mutableStateOf(false) }
    var showShizukuPermDialog by remember { mutableStateOf(false) }
    var shizukuGranted by remember { mutableStateOf(false) }
    var permCheckTrigger by remember { mutableStateOf(0) }

    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permCheckTrigger++
    }

    LaunchedEffect(permCheckTrigger) {
        if (!Environment.isExternalStorageManager()) {
            showStoragePermDialog = true
        } else {
            showStoragePermDialog = false
            // Check Shizuku - check package installation first (doesn't need binder)
            var shizukuInstalled = false
            try {
                appContext.packageManager.getPackageInfo("moe.shizuku.manager", 0)
                shizukuInstalled = true
            } catch (_: Exception) {
                try {
                    appContext.packageManager.getPackageInfo("moe.shizuku.privilege.api", 0)
                    shizukuInstalled = true
                } catch (_: Exception) {}
            }
            if (shizukuInstalled) {
                var binderAlive = false
                try {
                    binderAlive = Shizuku.pingBinder()
                } catch (_: IllegalStateException) {
                    // Shizuku not initialized (no ShizukuProvider), show dialog anyway
                } catch (_: Exception) {}
                if (binderAlive) {
                    shizukuGranted = true
                } else {
                    shizukuGranted = false
                    showShizukuPermDialog = true
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val listener = try {
            Shizuku.OnRequestPermissionResultListener { _, grantResult ->
                if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    shizukuGranted = true
                }
                showShizukuPermDialog = false
            }
        } catch (_: Exception) { null }
        if (listener != null) {
            try { Shizuku.addRequestPermissionResultListener(listener) } catch (_: Exception) {}
        }
        onDispose {
            if (listener != null) {
                try { Shizuku.removeRequestPermissionResultListener(listener) } catch (_: Exception) {}
            }
        }
    }

    if (showStoragePermDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("需要文件管理权限") },
            text = { Text("运行代码需要管理所有文件的权限，请在设置中开启。") },
            confirmButton = { TextButton(onClick = {
                showStoragePermDialog = false
                try {
                    val intent = android.content.Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = android.net.Uri.parse("package:${appContext.packageName}")
                    }
                    settingsLauncher.launch(intent)
                } catch (_: Exception) {}
            }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { showStoragePermDialog = false }) { Text("取消") } }
        )
    }

    if (showShizukuPermDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("需要 Shizuku 权限") },
            text = { Text("检测到已安装 Shizuku，授予权限后可以使用更高级的功能运行代码。") },
            confirmButton = { TextButton(onClick = {
                showShizukuPermDialog = false
                try { Shizuku.requestPermission(1001) } catch (_: Exception) {}
            }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { showShizukuPermDialog = false }) { Text("取消") } }
        )
    }

    when (currentScreen) {
        Screen.Home -> {
            val conv = conversations.find { it.id == currentConvId }
            if (conv != null) {
                HomeScreen(
                    isAgentsMode = isAgentsMode,
                    onToggleMode = { isAgentsMode = it },
                    isAgentEnabled = isAgentEnabled,
                    onToggleAgentEnabled = { isAgentEnabled = it },
                    providers = providers,
                    envSelections = envSelections,
                    conversations = conversations,
                    conversation = conv,
                    currentProject = currentProject,
                    projects = projects,
                    onCreateProject = { name ->
                        val p = FileOps.createProject(appContext, name)
                        currentProject = p
                        projects = FileOps.loadProjects(appContext)
                    },
                    onSelectProject = { p ->
                        currentProject = p
                    },
                    appContext = appContext,
                    onUpdateConversation = { updated ->
                        val idx = conversations.indexOfFirst { it.id == updated.id }
                        if (idx >= 0) conversations[idx] = updated
                    },
                    onNavigateToSettings = { currentScreen = Screen.Settings },
                    onNewChat = {
                        val c = Conversation()
                        conversations.add(c)
                        currentConvId = c.id
                    },
                    onSwitchConversation = { conv ->
                        currentConvId = conv.id
                    },
                    onDeleteConversation = { conv ->
                        conversations.remove(conv)
                        if (currentConvId == conv.id) {
                            if (conversations.isNotEmpty()) currentConvId = conversations.last().id
                        }
                    }
                )
            }
        }
        Screen.Settings -> SettingsScreen(
            providers = providers,
            envSelections = envSelections,
            onBack = { currentScreen = Screen.Home }
        )
        Screen.Setup -> SetupWizard(
            envSelections = envSelections,
            onComplete = { currentScreen = Screen.Home }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    isAgentsMode: Boolean,
    onToggleMode: (Boolean) -> Unit,
    isAgentEnabled: Boolean,
    onToggleAgentEnabled: (Boolean) -> Unit,
    providers: List<ApiProvider>,
    envSelections: List<EnvSelection>,
    conversations: List<Conversation>,
    conversation: Conversation,
    currentProject: Project?,
    projects: List<Project>,
    onCreateProject: (String) -> Unit,
    onSelectProject: (Project) -> Unit,
    appContext: Context,
    onUpdateConversation: (Conversation) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNewChat: () -> Unit,
    onSwitchConversation: (Conversation) -> Unit,
    onDeleteConversation: (Conversation) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    var selectedIndex by remember(conversation.id) { mutableIntStateOf(conversation.providerIndex) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var isLoading by remember { mutableStateOf(false) }
    var isStreaming by remember { mutableStateOf(false) }
    var streamContent by remember { mutableStateOf("") }
    var streamReasoning by remember { mutableStateOf("") }

    val messages = conversation.messages
    val selectedProvider = providers.getOrNull(selectedIndex)
    var deleteConfirmConv by remember { mutableStateOf<Conversation?>(null) }

    val displayMessages = if (isStreaming) {
        messages + ChatMessage("assistant", streamContent, streamReasoning.ifEmpty { null })
    } else {
        messages
    }

    Box(modifier = Modifier.fillMaxSize().imePadding()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = conversation.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1
                            )
                            if (isAgentsMode && selectedProvider != null) {
                                Text(
                                    text = "🤖 ${selectedProvider!!.customName.ifEmpty { selectedProvider!!.modelName }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "菜单")
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isAgentsMode) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline
                                )
                                .clickable { onToggleMode(!isAgentsMode) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isAgentsMode) "Agents" else "IDE",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isAgentsMode) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "设置")
                        }
                    }
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (isAgentsMode && selectedProvider != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("输入消息...") },
                                singleLine = true,
                                enabled = !isLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isAgentEnabled) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .border(
                                        width = if (isAgentEnabled) 0.dp else 1.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onToggleAgentEnabled(!isAgentEnabled) }
                                    .padding(horizontal = 10.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = if (isAgentEnabled) "🤖" else "🧑",
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    val text = inputText.trim()
                                    val provider = selectedProvider
                                    if (text.isNotEmpty() && provider != null && !isLoading) {
                                        inputText = ""
                                        val newMsgs = messages + ChatMessage("user", text)
                                        val title = if (conversation.messages.isEmpty()) {
                                            text.take(30) + if (text.length > 30) "..." else ""
                                        } else conversation.title
                                        onUpdateConversation(conversation.copy(messages = newMsgs, title = title, providerIndex = selectedIndex))
                                        isLoading = true
                                        isStreaming = true
                                        streamContent = ""
                                        streamReasoning = ""
                                        ApiService.sendMessage(
                                            provider = provider,
                                            message = text,
                                            history = messages,
                                            projectPath = if (isAgentEnabled) currentProject?.path else null,
                                            envSelections = envSelections,
                                            onReasoning = { acc -> streamReasoning = acc },
                                            onContent = { acc -> streamContent = acc },
                                            onDone = {
                                                val finalMsgs = newMsgs + ChatMessage("assistant", streamContent, streamReasoning.ifEmpty { null })
                                                onUpdateConversation(conversation.copy(messages = finalMsgs, title = title, providerIndex = selectedIndex))
                                                isStreaming = false
                                                isLoading = false
                                            },
                                            onError = { err ->
                                                val finalMsgs = newMsgs + ChatMessage("assistant", err)
                                                onUpdateConversation(conversation.copy(messages = finalMsgs, title = title, providerIndex = selectedIndex))
                                                isStreaming = false
                                                isLoading = false
                                            }
                                        )
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "发送", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            if (isAgentsMode) {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    if (providers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("请先在设置中添加服务商", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else if (messages.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "👋 欢迎使用",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "选择下方模型开始对话，或创建项目使用 IDE 功能",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "选择模型",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            providers.forEachIndexed { index, provider ->
                                Card(
                                    onClick = { selectedIndex = index },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (index == selectedIndex)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🤖 ${provider.customName.ifEmpty { provider.modelName }}",
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = provider.modelName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            if (currentProject != null) {
                                Text(
                                    text = "当前项目",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                ) {
                                    Text(
                                        text = "📁 ${currentProject!!.name}",
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            if (projects.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "最近项目",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                projects.take(3).forEach { p ->
                                    val fileCount = remember(p) { FileOps.listFiles(p.path).size }
                                    Card(
                                        onClick = { onSelectProject(p) },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (p == currentProject)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceContainerHigh
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📁 ${p.name}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "$fileCount 文件",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            TextButton(onClick = onNewChat) {
                                Text("新对话")
                            }
                        }
                    } else {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            var expanded by remember { mutableStateOf(false) }
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .clickable { expanded = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedProvider?.let { it.customName.ifEmpty { it.modelName } } ?: "选择模型",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                providers.forEachIndexed { index, provider ->
                                    DropdownMenuItem(
                                        text = { Text(provider.customName.ifEmpty { provider.modelName }) },
                                        onClick = {
                                            selectedIndex = index
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(displayMessages) { msg ->
                                ChatBubble(msg, isStreaming && msg.role == "assistant" && streamContent.isEmpty())
                            }
                        }
                        if (isLoading && messages.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Text(
                                        text = if (isStreaming) "正在生成..." else "思考中...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                if (currentProject == null) {
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("创建项目", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(24.dp))
                        var projectName by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = projectName,
                            onValueChange = { projectName = it },
                            label = { Text("项目名称") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (projectName.isNotBlank()) {
                                    onCreateProject(projectName.trim())
                                }
                            },
                            enabled = projectName.isNotBlank()
                        ) {
                            Text("创建项目")
                        }
                        if (projects.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("已有项目", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(projects) { p ->
                                    val fileCount = remember(p) { FileOps.listFiles(p.path).size }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { onSelectProject(p) },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (p == currentProject)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceContainerHigh
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "📁 ${p.name}",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text(
                                                    text = "$fileCount 个文件",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(p.createdAt)),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    var selectedFilePath by remember { mutableStateOf<String?>(null) }
                    var fileContent by remember { mutableStateOf("") }
                    var refreshTrigger by remember { mutableIntStateOf(0) }
                    var showFileTree by remember { mutableStateOf(false) }
                    var expandedPaths by remember { mutableStateOf(setOf<String>()) }
                    var clipboardFile by remember { mutableStateOf<java.io.File?>(null) }
                    var isCutAction by remember { mutableStateOf(false) }
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    var deleteTargetFile by remember { mutableStateOf<java.io.File?>(null) }
                    var showMoveDialog by remember { mutableStateOf(false) }
                    var moveSourceFile by remember { mutableStateOf<java.io.File?>(null) }
                    var moveTargetPath by remember { mutableStateOf("") }
                    var showPasteDialog by remember { mutableStateOf(false) }
                    var pasteTargetPath by remember { mutableStateOf("") }
                    var showProjectList by remember { mutableStateOf(false) }
                    var showRunOutput by remember { mutableStateOf(false) }
                    var runOutputText by remember { mutableStateOf("") }
                    var runOutputTitle by remember { mutableStateOf("") }
                    var runLoading by remember { mutableStateOf(false) }
                    var showInstallPrompt by remember { mutableStateOf(false) }
                    var installLang by remember { mutableStateOf("") }
                    var installPkgName by remember { mutableStateOf("") }

                    // Refresh file tree on every entry into IDE mode
                    LaunchedEffect(Unit) { refreshTrigger++ }
                    fun refreshTree() {
                        refreshTrigger++
                        expandedPaths = setOf()
                        selectedFilePath = null
                        fileContent = ""
                    }

                    fun performDelete(file: java.io.File) {
                        if (file.isDirectory) file.deleteRecursively() else file.delete()
                        refreshTree()
                    }

                    fun performMove(source: java.io.File, targetDir: String) {
                        val destDir = java.io.File(currentProject.path, targetDir)
                        destDir.mkdirs()
                        source.renameTo(java.io.File(destDir, source.name))
                        refreshTree()
                    }

                    fun performPaste(targetDir: String) {
                        val clip = clipboardFile ?: return
                        val destDir = java.io.File(currentProject.path, targetDir)
                        destDir.mkdirs()
                        val dest = java.io.File(destDir, clip.name)
                        if (isCutAction) {
                            clip.renameTo(dest)
                        } else {
                            if (clip.isDirectory) clip.copyRecursively(dest, true) else clip.copyTo(dest, true)
                        }
                        clipboardFile = null
                        refreshTree()
                    }

                    fun getLanguageForExt(ext: String): String = when (ext.lowercase()) {
                        "py" -> "Python"
                        "java" -> "Java"
                        "kt", "kts" -> "Kotlin"
                        "js", "mjs" -> "JavaScript (Node.js)"
                        "ts" -> "TypeScript"
                        "go" -> "Go"
                        "rs" -> "Rust"
                        "cpp", "cc", "cxx", "hpp" -> "C++"
                        "c", "h" -> "C"
                        "cs" -> "C#"
                        "rb" -> "Ruby"
                        "php" -> "PHP"
                        "scala", "sc" -> "Scala"
                        "sh", "bash" -> "Shell"
                        "html", "htm" -> "HTML/CSS"
                        "css" -> "CSS"
                        "vue" -> "Vue.js"
                        "jsx", "tsx" -> "React"
                        "swift" -> "Swift"
                        "r" -> "R"
                        else -> ""
                    }

                    fun getRuntimeCmd(ext: String): List<String>? = when (ext.lowercase()) {
                        "sh", "bash" -> listOf("sh")
                        "py" -> listOf("python3", "python")
                        "js", "mjs" -> listOf("node")
                        "java" -> listOf("java")
                        "kt", "kts" -> listOf("kotlin")
                        "go" -> listOf("go", "run")
                        "rs" -> listOf("rustc")
                        "cpp", "cc", "cxx" -> listOf("g++", "clang++")
                        "c" -> listOf("gcc", "clang")
                        else -> null
                    }

                    fun runCurrentFile(path: String, content: String, envs: List<EnvSelection>, ctx: Context) {
                        val ext = path.substringAfterLast('.', "")
                        if (ext.isEmpty()) return

                        FileOps.writeFile(path, content)

                        if (ext.lowercase() == "sh" || ext.lowercase() == "bash") {
                            runLoading = true
                            Thread {
                                try {
                                    val proc = ProcessBuilder("sh", path)
                                        .redirectErrorStream(true)
                                        .start()
                                    val out = proc.inputStream.bufferedReader().readText()
                                    proc.waitFor()
                                    runOutputTitle = "Shell 运行结果 (exit=${proc.exitValue()})"
                                    runOutputText = out.ifEmpty { "(无输出)" }
                                } catch (e: Exception) {
                                    runOutputTitle = "运行失败"
                                    runOutputText = e.message ?: "未知错误"
                                }
                                runLoading = false
                                showRunOutput = true
                            }.start()
                            return
                        }

                        if (ext.lowercase() == "js" || ext.lowercase() == "mjs") {
                            runLoading = true
                            val webView = android.webkit.WebView(ctx)
                            webView.settings.javaScriptEnabled = true
                            webView.addJavascriptInterface(object {
                                @android.webkit.JavascriptInterface
                                fun onResult(result: String) {
                                    runOutputTitle = "JavaScript 运行结果"
                                    runOutputText = result
                                    runLoading = false
                                    showRunOutput = true
                                }
                                @android.webkit.JavascriptInterface
                                fun onError(error: String) {
                                    runOutputTitle = "运行失败"
                                    runOutputText = error
                                    runLoading = false
                                    showRunOutput = true
                                }
                            }, "Android")
                            val wrapped = """
                                try {
                                    var result = (function() {
                                        ${content.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")}
                                    })();
                                    Android.onResult(result !== undefined ? String(result) : "(undefined)");
                                } catch(e) {
                                    Android.onError(e.message);
                                }
                            """.trimIndent()
                            webView.evaluateJavascript("(function(){ $wrapped })()", null)
                            return
                        }

                        val cmds = getRuntimeCmd(ext)
                        if (cmds == null) {
                            runOutputTitle = "不支持"
                            runOutputText = "不支持的文件类型: .$ext"
                            showRunOutput = true
                            return
                        }

                        runLoading = true
                        Thread {
                            try {
                                val firstCmd = cmds.first()
                                val lang = getLanguageForExt(ext)

                                // Save script to shared storage for accessibility
                                val sharedDir = java.io.File("/storage/emulated/0/apitool")
                                sharedDir.mkdirs()
                                val scriptFile = java.io.File(sharedDir, "run_${System.nanoTime()}.$ext")
                                scriptFile.writeText(content)
                                scriptFile.setReadable(true, false)
                                scriptFile.setWritable(true, false)

                                // For Python: use bundled interpreter
                                if (ext.lowercase() == "py") {
                                    val pythonDir = java.io.File(ctx.filesDir, "python")
                                    val pythonBin = java.io.File(pythonDir, "bin/python3.13")
                                    val androidSupportLib = java.io.File(pythonDir, "lib/libandroid-support.so")
                                    if (!pythonBin.isFile() || pythonBin.length() == 0L || !androidSupportLib.isFile()) {
                                        try {
                                            if (pythonDir.exists()) pythonDir.deleteRecursively()
                                            pythonDir.mkdirs()
                                            // Write asset to temp file, then use ZipFile (more reliable than ZipInputStream)
                                            val tmpZip = java.io.File(ctx.cacheDir, "python_extract.zip")
                                            ctx.assets.open("python/python.zip").use { src -> tmpZip.outputStream().use { dst -> src.copyTo(dst) } }
                                            if (!tmpZip.isFile() || tmpZip.length() < 1024L) {
                                                throw java.io.IOException("Temp zip is empty or too small: ${tmpZip.length()} bytes")
                                            }
                                            val zf = java.util.zip.ZipFile(tmpZip)
                                            val entries = zf.entries()
                                            while (entries.hasMoreElements()) {
                                                val ze = entries.nextElement()
                                                val file = java.io.File(pythonDir, ze.name)
                                                if (ze.isDirectory) { file.mkdirs() }
                                                else {
                                                    file.parentFile?.mkdirs()
                                                    zf.getInputStream(ze).use { src -> file.outputStream().use { dst -> src.copyTo(dst) } }
                                                }
                                            }
                                            zf.close()
                                            tmpZip.delete()
                                            try {
                                                Runtime.getRuntime().exec(arrayOf("chmod", "-R", "755", java.io.File(pythonDir, "bin").absolutePath)).waitFor()
                                            } catch (_: Exception) {}
                                        } catch (e: Exception) {
                                            runOutputTitle = "Python 环境准备失败"
                                            runOutputText = "内嵌 Python 提取错误: ${e.message}"
                                            runLoading = false
                                            showRunOutput = true
                                            return@Thread
                                        }
                                    }

                                    if (pythonBin.isFile() && pythonBin.length() > 0L) {
                                        fun runPython(cmd: List<String>): Boolean = try {
                                            val proc = ProcessBuilder(cmd).apply {
                                                environment()["LD_LIBRARY_PATH"] = "${pythonDir.absolutePath}/lib"
                                                environment()["PYTHONHOME"] = pythonDir.absolutePath
                                                environment()["HOME"] = sharedDir.absolutePath
                                                redirectErrorStream(true)
                                            }.start()
                                            val out = proc.inputStream.bufferedReader().readText()
                                            proc.waitFor()
                                            runOutputTitle = "$lang 运行结果 (exit=${proc.exitValue()})"
                                            runOutputText = out.ifEmpty { "(无输出)" }
                                            runLoading = false
                                            showRunOutput = true
                                            true
                                        } catch (_: Exception) { false }
                                        // Try direct execution first, fallback to linker64 if /data is noexec
                                        if (!runPython(listOf(pythonBin.absolutePath, scriptFile.absolutePath))) {
                                            val linker = if (java.io.File("/system/bin/linker64").exists()) "/system/bin/linker64" else "/system/bin/linker"
                                            runPython(listOf(linker, pythonBin.absolutePath, scriptFile.absolutePath))
                                        }
                                        return@Thread
                                    }
                                }

                                // For other languages, try Termux
                                val termuxInstalled = try {
                                    ctx.packageManager.getPackageInfo("com.termux", 0)
                                    true
                                } catch (_: Exception) { false }

                                var executed = false
                                if (termuxInstalled) {
                                    val termuxBinary = "/data/data/com.termux/files/usr/bin/$firstCmd"
                                    try {
                                        val proc = ProcessBuilder(termuxBinary, scriptFile.absolutePath)
                                            .redirectErrorStream(true)
                                            .start()
                                        val out = proc.inputStream.bufferedReader().readText()
                                        proc.waitFor()
                                        runOutputTitle = "$lang 运行结果 (exit=${proc.exitValue()})"
                                        runOutputText = out.ifEmpty { "(无输出)" }
                                        runLoading = false
                                        showRunOutput = true
                                        executed = true
                                    } catch (_: Exception) {
                                        try {
                                            val proc = ProcessBuilder(
                                                "/system/bin/sh", "-c",
                                                "export PATH=/data/data/com.termux/files/usr/bin:/system/bin; " +
                                                "$firstCmd '${scriptFile.absolutePath}'"
                                            ).redirectErrorStream(true).start()
                                            val out = proc.inputStream.bufferedReader().readText()
                                            proc.waitFor()
                                            runOutputTitle = "$lang 运行结果 (exit=${proc.exitValue()})"
                                            runOutputText = out.ifEmpty { "(无输出)" }
                                            runLoading = false
                                            showRunOutput = true
                                            executed = true
                                        } catch (_: Exception) {}
                                    }
                                }

                                if (!executed) {
                                    try {
                                        val uri = android.net.Uri.fromFile(scriptFile)
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "text/plain")
                                            setClassName("com.termux", "com.termux.app.TermuxActivity")
                                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        ctx.startActivity(intent)
                                        runOutputTitle = "已在 Termux 中打开"
                                        runOutputText = "文件已保存到 ${scriptFile.absolutePath}\n\n在 Termux 中执行：\n$firstCmd ${scriptFile.absolutePath}\n\n（命令已复制到剪贴板）"
                                        try {
                                            val cm = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            cm.setPrimaryClip(android.content.ClipData.newPlainText("cmd", "$firstCmd ${scriptFile.absolutePath}"))
                                        } catch (_: Exception) {}
                                    } catch (_: Exception) {
                                        runOutputTitle = "无法运行"
                                        runOutputText = "无法启动 Termux，请在 Termux 中手动执行：\n$firstCmd ${scriptFile.absolutePath}"
                                    }
                                    runLoading = false
                                    showRunOutput = true
                                }
                            } catch (e: Exception) {
                                runOutputTitle = "运行失败"
                                runOutputText = e.message ?: "未知错误"
                            }
                            runLoading = false
                            if (!showRunOutput) showRunOutput = true
                        }.start()
                    }

                    @Composable
                    fun FileTreeItem(file: java.io.File, depth: Int) {
                        val isExpanded = file.absolutePath in expandedPaths
                        var showCtxMenu by remember { mutableStateOf(false) }

                        Box {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = (depth * 20).dp)
                                        .combinedClickable(
                                            onClick = {
                                                if (file.isDirectory) {
                                                    expandedPaths = if (isExpanded) expandedPaths - file.absolutePath else expandedPaths + file.absolutePath
                                                } else {
                                                    selectedFilePath = file.absolutePath
                                                    fileContent = FileOps.readFile(file.absolutePath)
                                                }
                                            },
                                            onLongClick = { showCtxMenu = true }
                                        )
                                        .background(
                                            if (file.absolutePath == selectedFilePath) MaterialTheme.colorScheme.primaryContainer
                                            else Color.Transparent
                                        )
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (file.isDirectory) {
                                        Text(if (isExpanded) "▼" else "▶", fontSize = 10.sp)
                                        Spacer(Modifier.width(4.dp))
                                        Text("📁", fontSize = 12.sp)
                                    } else {
                                        Spacer(Modifier.width(4.dp))
                                        Text("📄", fontSize = 12.sp)
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = if (file.isDirectory) file.name else file.nameWithoutExtension,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                if (!file.isDirectory) {
                                    val ext = file.extension
                                    if (ext.isNotEmpty()) {
                                        Spacer(Modifier.width(2.dp))
                                        Text(
                                            text = ".$ext",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            DropdownMenu(
                                expanded = showCtxMenu,
                                onDismissRequest = { showCtxMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("复制") },
                                    onClick = { clipboardFile = file; isCutAction = false; showCtxMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("剪切") },
                                    onClick = { clipboardFile = file; isCutAction = true; showCtxMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("移动") },
                                    onClick = {
                                        moveSourceFile = file
                                        moveTargetPath = ""
                                        showMoveDialog = true
                                        showCtxMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("删除") },
                                    onClick = {
                                        deleteTargetFile = file
                                        showDeleteConfirm = true
                                        showCtxMenu = false
                                    }
                                )
                            }
                        }

                        if (file.isDirectory && isExpanded) {
                            val children = FileOps.listFiles(file.absolutePath)
                            children.forEach { child ->
                                FileTreeItem(file = child, depth = depth + 1)
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showFileTree = !showFileTree }) {
                                Text("📁")
                            }
                            Text(
                                text = currentProject.name,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .clickable { showProjectList = true }
                            )
                            if (clipboardFile != null) {
                                TextButton(onClick = { pasteTargetPath = ""; showPasteDialog = true }) {
                                    Text("粘贴")
                                }
                            }
                            if (selectedFilePath != null) {
                                TextButton(onClick = {
                                    FileOps.writeFile(selectedFilePath!!, fileContent)
                                    android.widget.Toast.makeText(appContext, "✅ 已保存", android.widget.Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("保存")
                                }
                                TextButton(onClick = {
                                    runCurrentFile(selectedFilePath!!, fileContent, envSelections, appContext)
                                }) {
                                    Text("▶")
                                }
                            }
                            TextButton(onClick = { refreshTree() }) {
                                Text("↻")
                            }
                        }
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxSize().padding(start = 4.dp)) {
                                if (selectedFilePath != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = java.io.File(selectedFilePath!!).name,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    val highlightExt = selectedFilePath!!.substringAfterLast('.')
                                    val highlightTrans = remember(highlightExt) { SyntaxHighlightTransformation(highlightExt) }
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(Color(0xFF191A1C))
                                    ) {
                                        OutlinedTextField(
                                            value = fileContent,
                                            onValueChange = { fileContent = it },
                                            modifier = Modifier.fillMaxSize(),
                                            textStyle = TextStyle(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                color = Color(0xFFD4D4D4)
                                            ),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                cursorColor = Color(0xFFFFFFFF),
                                                focusedBorderColor = Color(0xFF3C3C3C),
                                                unfocusedBorderColor = Color(0xFF3C3C3C),
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedTextColor = Color(0xFFD4D4D4),
                                                unfocusedTextColor = Color(0xFFD4D4D4)
                                            ),
                                            visualTransformation = highlightTrans
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("选择文件进行编辑", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            if (showFileTree) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f))
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { showFileTree = false }
                                    )
                                    Column(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(0.55f)
                                            .verticalScroll(rememberScrollState())
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(end = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("文件", style = MaterialTheme.typography.titleSmall)
                                            TextButton(onClick = { showFileTree = false }) {
                                                Text("✕")
                                            }
                                        }
                                        key(refreshTrigger) {
                                            val rootFiles = FileOps.listFiles(currentProject.path)
                                            rootFiles.forEach { file ->
                                                FileTreeItem(file = file, depth = 0)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showDeleteConfirm && deleteTargetFile != null) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false; deleteTargetFile = null },
                            title = { Text("确认删除") },
                            text = { Text("确定要删除 ${deleteTargetFile!!.name} 吗？") },
                            confirmButton = {
                                TextButton(onClick = {
                                    performDelete(deleteTargetFile!!)
                                    showDeleteConfirm = false; deleteTargetFile = null
                                }) { Text("删除") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false; deleteTargetFile = null }) { Text("取消") }
                            }
                        )
                    }

                    if (showMoveDialog && moveSourceFile != null) {
                        AlertDialog(
                            onDismissRequest = { showMoveDialog = false; moveSourceFile = null },
                            title = { Text("移动文件") },
                            text = {
                                Column {
                                    Text("将 ${moveSourceFile!!.name} 移动到：")
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = moveTargetPath,
                                        onValueChange = { moveTargetPath = it },
                                        label = { Text("目标目录（项目相对路径，留空为根目录）") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    performMove(moveSourceFile!!, moveTargetPath)
                                    showMoveDialog = false; moveSourceFile = null
                                }) { Text("移动") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showMoveDialog = false; moveSourceFile = null }) { Text("取消") }
                            }
                        )
                    }

                    if (showPasteDialog && clipboardFile != null) {
                        AlertDialog(
                            onDismissRequest = { showPasteDialog = false },
                            title = { Text("粘贴文件") },
                            text = {
                                Column {
                                    Text("将 ${clipboardFile!!.name} 粘贴到：")
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = pasteTargetPath,
                                        onValueChange = { pasteTargetPath = it },
                                        label = { Text("目标目录（项目相对路径，留空为根目录）") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    performPaste(pasteTargetPath)
                                    showPasteDialog = false
                                }) { Text("粘贴") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showPasteDialog = false }) { Text("取消") }
                            }
                        )
                    }

                    if (showProjectList) {
                        AlertDialog(
                            onDismissRequest = { showProjectList = false },
                            title = { Text("选择项目") },
                            text = {
                                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                                    items(projects) { p ->
                                        Card(
                                            onClick = {
                                                onSelectProject(p)
                                                showProjectList = false
                                                refreshTree()
                                            },
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (p == currentProject)
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceContainerHigh
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "📁 ${p.name}",
                                                    modifier = Modifier.weight(1f),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault()).format(java.util.Date(p.createdAt)),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showProjectList = false }) { Text("取消") }
                            }
                        )
                    }

                    if (showRunOutput) {
                        AlertDialog(
                            onDismissRequest = { showRunOutput = false },
                            title = { Text(runOutputTitle) },
                            text = {
                                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                                    SelectionContainer {
                                        Text(
                                            text = runOutputText,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .verticalScroll(rememberScrollState())
                                                .background(Color(0xFF191A1C))
                                                .padding(12.dp)
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp)),
                                            color = Color(0xFFD4D4D4)
                                        )
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showRunOutput = false }) { Text("关闭") }
                            }
                        )
                    }

                    if (showInstallPrompt) {
                        AlertDialog(
                            onDismissRequest = { showInstallPrompt = false },
                            title = { Text("安装 $installLang 环境") },
                            text = {
                                Column {
                                    Text("当前设备未检测到 $installLang 运行环境。")
                                    Spacer(Modifier.height(8.dp))
                                    Text("是否安装 Termux 并在其中安装 $installPkgName 包？")
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "步骤：\n1. 从 GitHub 下载 Termux (已自动打开页面)\n2. 安装后打开 Termux，运行：\n   pkg install $installPkgName\n3. 返回本应用再次点击 ▶ 运行",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    showInstallPrompt = false
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                        intent.data = android.net.Uri.parse("https://github.com/termux/termux-app/releases")
                                        appContext.startActivity(intent)
                                    } catch (_: Exception) {}
                                }) { Text("去下载 Termux") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showInstallPrompt = false }) { Text("稍后") }
                            }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = menuOpen,
            enter = slideInHorizontally(tween(300)) { -it },
            exit = slideOutHorizontally(tween(300)) { -it }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { menuOpen = false }
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.65f)
                        .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(top = 48.dp)) {
                        Text(
                            text = "菜单",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        TextButton(onClick = { onNewChat(); menuOpen = false }) {
                            Text("新对话", style = MaterialTheme.typography.bodyLarge)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        var searchQuery by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("搜索对话...") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (conversations.isEmpty()) {
                            Text(
                                text = "暂无历史对话",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            val filtered = if (searchQuery.isBlank()) conversations
                                else conversations.filter { it.title.contains(searchQuery, ignoreCase = true) }
                            val grouped = filtered.sortedByDescending { it.createdAt }
                                .groupBy { conv ->
                                    providers.getOrNull(conv.providerIndex)?.let { it.customName.ifEmpty { it.modelName } } ?: "默认"
                                }
                            grouped.forEach { (providerName, convs) ->
                                Text(
                                    text = "🤖 $providerName",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                                convs.forEach { conv ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { menuOpen = false; onSwitchConversation(conv) }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = conv.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                color = if (conv.id == conversation.id)
                                                    MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = conv.messages.size.let { "$it 条消息" },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        var showMore by remember { mutableStateOf(false) }
                                        Box {
                                            IconButton(
                                                onClick = { showMore = true },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.MoreVert, contentDescription = "更多", modifier = Modifier.size(18.dp))
                                            }
                                            DropdownMenu(
                                                expanded = showMore,
                                                onDismissRequest = { showMore = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                                                    onClick = {
                                                        showMore = false
                                                        deleteConfirmConv = conv
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                if (grouped.keys.last() != providerName) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        deleteConfirmConv?.let { conv ->
            AlertDialog(
                onDismissRequest = { deleteConfirmConv = null },
                title = { Text("删除对话") },
                text = { Text("确定要删除「${conv.title}」吗？") },
                confirmButton = {
                    Button(onClick = {
                        onDeleteConversation(conv)
                        deleteConfirmConv = null
                    }) {
                        Text("删除")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteConfirmConv = null }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

private class SyntaxHighlightTransformation(private val ext: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(syntaxHighlight(text.text, ext), OffsetMapping.Identity)
    }
}

private fun syntaxHighlight(code: String, ext: String): AnnotatedString = buildAnnotatedString {
    val keywords = when (ext.lowercase()) {
        "kt", "kts" -> setOf("val", "var", "fun", "class", "object", "if", "else", "when", "for", "while", "do", "return", "import", "package", "private", "public", "protected", "internal", "abstract", "interface", "enum", "data", "sealed", "open", "override", "init", "constructor", "super", "this", "null", "true", "false", "try", "catch", "finally", "throw", "in", "is", "as", "typealias", "companion", "inline", "suspend", "tailrec", "operator", "infix")
        "java" -> setOf("public", "private", "protected", "class", "interface", "enum", "abstract", "final", "static", "void", "int", "long", "double", "float", "boolean", "char", "byte", "short", "String", "null", "true", "false", "if", "else", "for", "while", "do", "switch", "case", "break", "continue", "return", "new", "import", "package", "extends", "implements", "throws", "try", "catch", "finally", "throw", "super", "this", "synchronized", "volatile", "transient")
        "py" -> setOf("def", "class", "if", "elif", "else", "for", "while", "break", "continue", "return", "import", "from", "as", "try", "except", "finally", "raise", "with", "yield", "lambda", "pass", "in", "not", "and", "or", "is", "None", "True", "False", "async", "await", "self", "global", "nonlocal", "del", "assert")
        "js", "mjs", "ts" -> setOf("function", "const", "let", "var", "if", "else", "for", "while", "do", "return", "import", "export", "from", "as", "class", "extends", "new", "this", "null", "undefined", "true", "false", "try", "catch", "finally", "throw", "async", "await", "yield", "of", "in", "typeof", "instanceof", "switch", "case", "break", "continue", "default", "delete")
        "html", "htm", "xml" -> emptySet()
        "css" -> setOf("@import", "@media", "@keyframes", "@font-face", "!important")
        "sh", "bash", "zsh" -> setOf("if", "then", "else", "elif", "fi", "for", "while", "do", "done", "case", "esac", "function", "return", "exit", "export", "local", "source", "in")
        else -> emptySet()
    }
    val comments = setOf("py", "sh", "bash", "zsh", "js", "mjs", "ts", "java", "kt", "kts")
    val hasLineComments = ext.lowercase() in comments
    val tokenRegex = Regex("""("[^"]*"|'[^']*'|[a-zA-Z_]\w*|\d+\.?\d*|//[^\n]*|#[^\n]*)""")
    var lastEnd = 0
    for (match in tokenRegex.findAll(code)) {
        append(code.substring(lastEnd, match.range.first))
        val token = match.value
        val isString = (token.startsWith('"') || token.startsWith('\'')) && token.length > 1
        val isComment = if (hasLineComments) token.startsWith("//") || token.startsWith("#") else token.startsWith("/*")
        val isNumber = token.first().isDigit()
        val isKeyword = token in keywords
        when {
            isComment -> withStyle(SpanStyle(color = Color(0xFF6A9955))) { append(token) }
            isString -> withStyle(SpanStyle(color = Color(0xFFCE9178))) { append(token) }
            isNumber -> withStyle(SpanStyle(color = Color(0xFFB5CEA8))) { append(token) }
            isKeyword -> withStyle(SpanStyle(color = Color(0xFF569CD6))) { append(token) }
            else -> append(token)
        }
        lastEnd = match.range.last + 1
    }
    append(code.substring(lastEnd))
}

private fun parseInlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    val regex = Regex("""\*\*(.+?)\*\*|\*(.+?)\*|`(.+?)`""")
    var lastEnd = 0
    for (match in regex.findAll(text)) {
        append(text.substring(lastEnd, match.range.first))
        when {
            match.groupValues[1].isNotEmpty() -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groupValues[1])
                }
            }
            match.groupValues[2].isNotEmpty() -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(match.groupValues[2])
                }
            }
            match.groupValues[3].isNotEmpty() -> {
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                    append(match.groupValues[3])
                }
            }
        }
        lastEnd = match.range.last + 1
    }
    append(text.substring(lastEnd))
}

private fun parseHeadingLevel(line: String): Int {
    val trimmed = line.trimStart()
    val level = trimmed.takeWhile { it == '#' }.length
    if (level in 1..6 && trimmed.length > level && trimmed[level] == ' ') return level
    return 0
}

@Composable
private fun MarkdownContent(text: String, modifier: Modifier = Modifier) {
    val lines = text.split("\n")
    Column(modifier = modifier) {
        var first = true
        for (line in lines) {
            val trimmed = line.trim()
            val headingLevel = parseHeadingLevel(line)

            if (headingLevel > 0) {
                val headingText = line.trimStart().drop(headingLevel).trimStart()
                val size = when (headingLevel) {
                    1 -> 22.sp; 2 -> 20.sp; 3 -> 18.sp; 4 -> 16.sp; 5 -> 15.sp; else -> 14.sp
                }
                Text(
                    text = parseInlineMarkdown(headingText),
                    fontSize = size,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                first = false
            } else if (trimmed.matches(Regex("^(-{3,}|\\*{3,}|_{3,})$"))) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                first = false
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                val itemText = parseInlineMarkdown(trimmed.substring(2))
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("•  ", fontWeight = FontWeight.Bold)
                    Text(itemText)
                }
                first = false
            } else if (trimmed.matches(Regex("^\\d+\\.\\s.*"))) {
                val num = trimmed.takeWhile { it.isDigit() }
                val itemText = parseInlineMarkdown(trimmed.drop(num.length).trimStart().removePrefix(".").trimStart())
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("$num.  ")
                    Text(itemText)
                }
                first = false
            } else if (trimmed.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                first = false
            } else {
                if (!first) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(parseInlineMarkdown(line))
                first = false
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isThinking: Boolean = false) {
    val isUser = message.role == "user"
    val bg = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
    val align = if (isUser) Arrangement.End else Arrangement.Start
    val shape = if (isUser) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
        else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    val context = androidx.compose.ui.platform.LocalContext.current
    var showCopyToast by remember { mutableStateOf(false) }

    LaunchedEffect(showCopyToast) {
        if (showCopyToast) {
            kotlinx.coroutines.delay(1000)
            showCopyToast = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser && !message.reasoningContent.isNullOrBlank()) {
            var reasoningExpanded by remember { mutableStateOf(false) }
            var autoCollapsed by remember { mutableStateOf(false) }

            LaunchedEffect(message.content) {
                if (message.content.isNotEmpty() && !autoCollapsed) {
                    reasoningExpanded = false
                    autoCollapsed = true
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 6.dp)
                    .clickable {
                        reasoningExpanded = !reasoningExpanded
                        autoCollapsed = true
                    }
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when {
                                isThinking -> "思考中..."
                                reasoningExpanded -> "收起思考内容"
                                else -> "点击展开思考内容"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        if (!isThinking) {
                            Icon(
                                imageVector = if (reasoningExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    AnimatedVisibility(visible = reasoningExpanded) {
                        val reasoningScroll = rememberScrollState()
                        LaunchedEffect(message.reasoningContent) {
                            kotlinx.coroutines.delay(30)
                            reasoningScroll.scrollTo(reasoningScroll.maxValue)
                        }
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .heightIn(max = 200.dp)
                                .verticalScroll(reasoningScroll)
                        ) {
                            MarkdownContent(text = message.reasoningContent)
                        }
                    }
                }
            }
        }

        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = bg),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("message", message.content))
                    showCopyToast = true
                }
        ) {
            if (message.content.isEmpty() && !isUser) {
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isThinking) "正在生成回答..." else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                MarkdownContent(
                    text = message.content,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
    if (showCopyToast) {
        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(providers: MutableList<ApiProvider>, envSelections: MutableList<EnvSelection>, onBack: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var showEnvManager by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(onClick = { showDialog = true }) {
                Text("添加服务商")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { showEnvManager = true }) {
                Text("配置开发环境 (${envSelections.size})")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (providers.isEmpty()) {
                Text(
                    text = "暂无服务商，请点击上方按钮添加",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "已添加的服务商",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(providers.toList()) { provider ->
                        ProviderItem(
                            provider = provider,
                            onDelete = { providers.remove(provider) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddProviderDialog(
            onDismiss = { showDialog = false },
            onConfirm = { provider ->
                providers.add(provider)
                showDialog = false
            }
        )
    }

    if (showEnvManager) {
        EnvManagerDialog(
            envSelections = envSelections,
            onDismiss = { showEnvManager = false }
        )
    }
}

@Composable
fun ProviderItem(provider: ApiProvider, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = provider.customName.ifEmpty { provider.modelName },
            style = MaterialTheme.typography.bodyLarge
        )
            Text(
                text = provider.apiUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "删除")
        }
    }
}

@Composable
fun AddProviderDialog(onDismiss: () -> Unit, onConfirm: (ApiProvider) -> Unit) {
    var customName by remember { mutableStateOf("") }
    var apiUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加服务商") },
        text = {
            Column {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("自定义模型名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("仅对自己可见，可留空") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = apiUrl,
                    onValueChange = { apiUrl = it },
                    label = { Text("API URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("https://api.example.com/v1/chat/completions") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("sk-...") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("模型名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("gpt-4o") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (apiUrl.isNotBlank() && apiKey.isNotBlank() && modelName.isNotBlank()) {
                        onConfirm(ApiProvider(customName = customName, apiUrl = apiUrl, apiKey = apiKey, modelName = modelName))
                    }
                }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun SetupWizard(envSelections: MutableList<EnvSelection>, onComplete: () -> Unit) {
    var editingEnv by remember { mutableStateOf<LangEnvironment?>(null) }
    var step by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "配置开发环境",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "选择你使用的编程语言和环境，AI 将根据配置生成适配代码",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categoryOrder.forEach { category ->
                val envs = allEnvironments.filter { it.category == category }
                if (envs.isNotEmpty()) {
                    item {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(envs) { env ->
                        val selected = envSelections.find { it.name == env.name }
                        Card(
                            onClick = { editingEnv = env },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected != null)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${env.icon}  ${env.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selected != null) {
                                    Text(
                                        text = selected.version,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        text = "添加",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onComplete) {
                Text("跳过")
            }
            Button(
                onClick = onComplete,
                enabled = envSelections.isNotEmpty()
            ) {
                Text("完成配置 (${envSelections.size} 个环境)")
            }
        }
    }

    if (editingEnv != null) {
        EnvConfigDialog(
            env = editingEnv!!,
            currentSelection = envSelections.find { it.name == editingEnv!!.name },
            onDismiss = { editingEnv = null },
            onConfirm = { name, version ->
                val existing = envSelections.indexOfFirst { it.name == name }
                if (existing >= 0) {
                    envSelections[existing] = EnvSelection(name, version)
                } else {
                    envSelections.add(EnvSelection(name, version))
                }
                editingEnv = null
            },
            onRemove = { name ->
                envSelections.removeAll { it.name == name }
                editingEnv = null
            }
        )
    }
}

@Composable
fun EnvConfigDialog(
    env: LangEnvironment,
    currentSelection: EnvSelection?,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    onRemove: (String) -> Unit
) {
    var selectedVersion by remember { mutableStateOf(currentSelection?.version ?: env.versions.first()) }
    var versionExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${env.icon}  ${env.name}") },
        text = {
            Column {
                Text(
                    text = "选择版本",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = selectedVersion,
                    onValueChange = { selectedVersion = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { versionExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, "选择版本")
                        }
                    }
                )
                DropdownMenu(
                    expanded = versionExpanded,
                    onDismissRequest = { versionExpanded = false }
                ) {
                    env.versions.forEach { v ->
                        DropdownMenuItem(
                            text = { Text(v) },
                            onClick = {
                                selectedVersion = v
                                versionExpanded = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "提示：AI 会根据所选语言和版本生成对应代码，确保语法和 API 兼容。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (currentSelection != null) {
                    TextButton(onClick = { onRemove(env.name) }) {
                        Text("移除", color = MaterialTheme.colorScheme.error)
                    }
                }
                Button(onClick = { onConfirm(env.name, selectedVersion) }) {
                    Text(if (currentSelection != null) "更新" else "添加")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun EnvManagerDialog(envSelections: MutableList<EnvSelection>, onDismiss: () -> Unit) {
    var editingEnv by remember { mutableStateOf<LangEnvironment?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("配置开发环境") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                if (envSelections.isNotEmpty()) {
                    item {
                        Text(
                            "已选环境 (${envSelections.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(envSelections.toList()) { sel ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${allEnvironments.find { it.name == sel.name }?.icon ?: ""}  ${sel.name} ${sel.version}",
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { envSelections.removeAll { it.name == sel.name } },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "移除", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }

                item {
                    Text(
                        "所有语言环境",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                categoryOrder.forEach { category ->
                    val envs = allEnvironments.filter { it.category == category }
                    if (envs.isNotEmpty()) {
                        item {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(envs) { env ->
                            val selected = envSelections.find { it.name == env.name }
                            Card(
                                onClick = { editingEnv = env },
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selected != null)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${env.icon}  ${env.name}", modifier = Modifier.weight(1f))
                                    Text(
                                        if (selected != null) selected.version else "添加",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selected != null) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("完成") }
        }
    )

    if (editingEnv != null) {
        EnvConfigDialog(
            env = editingEnv!!,
            currentSelection = envSelections.find { it.name == editingEnv!!.name },
            onDismiss = { editingEnv = null },
            onConfirm = { name, version ->
                val existing = envSelections.indexOfFirst { it.name == name }
                if (existing >= 0) {
                    envSelections[existing] = EnvSelection(name, version)
                } else {
                    envSelections.add(EnvSelection(name, version))
                }
                editingEnv = null
            },
            onRemove = { name ->
                envSelections.removeAll { it.name == name }
                editingEnv = null
            }
        )
    }
}