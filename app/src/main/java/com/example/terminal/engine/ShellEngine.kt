package com.example.terminal.engine

import android.content.Context
import android.os.Build
import com.example.terminal.model.LineType
import com.example.terminal.model.TerminalLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import androidx.compose.ui.graphics.Color
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

class ShellEngine(private val context: Context) {

    val homeDir: String = context.filesDir.absolutePath
    private val binDir: String = "$homeDir/bin"
    private val cacheDir: String = context.cacheDir.absolutePath

    // Store active processes per tab id for cancellation/Ctrl+C
    private val activeProcesses = ConcurrentHashMap<String, Process>()

    var suBinaryPath: String? = null
        private set
    var isRealRoot: Boolean = false
        private set

    fun bootstrap() {
        detectRoot()
        PreinstalledScripts.bootstrap(context, suBinaryPath)
    }

    private fun detectRoot() {
        val suPaths = listOf(
            "/system/xbin/su",
            "/system/bin/su",
            "/sbin/su",
            "/vendor/bin/su",
            "/data/local/tmp/su",
            "su"
        )
        for (candidate in suPaths) {
            try {
                val f = File(candidate)
                if (f.exists() && f.canExecute()) {
                    val p = ProcessBuilder(candidate, "-c", "id").start()
                    val reader = p.inputStream.bufferedReader()
                    val line = reader.readLine() ?: ""
                    p.waitFor()
                    if (line.contains("uid=0") || p.exitValue() == 0) {
                        suBinaryPath = candidate
                        isRealRoot = true
                        return
                    }
                }
            } catch (_: Exception) {}
        }
        for (candidate in suPaths) {
            val f = File(candidate)
            if (f.exists()) {
                suBinaryPath = candidate
                isRealRoot = true
                return
            }
        }
    }

    fun getInitialLines(): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        lines.add(
            TerminalLine(
                text = "Welcome to Lyon Terminal",
                type = LineType.BANNER
            )
        )
        val primaryAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "x86_64"
        if (isRealRoot && suBinaryPath != null) {
            lines.add(
                TerminalLine(
                    text = "⚡ [ROOT ACTIVE] Superuser access detected via su ($suBinaryPath)",
                    type = LineType.SYSTEM,
                    customColor = Color(0xFF10B981)
                )
            )
        } else {
            lines.add(
                TerminalLine(
                    text = "🔒 [NATIVE SHELL] Standard Android app userspace • Native /system/bin/sh",
                    type = LineType.SYSTEM,
                    customColor = Color(0xFF94A3B8)
                )
            )
        }
        lines.add(
            TerminalLine(
                text = "🔌 [ADB SUITE] Network ADB bridge ready ('adb devices', 'adb connect 127.0.0.1:5555')",
                type = LineType.SYSTEM,
                customColor = Color(0xFFF59E0B)
            )
        )
        lines.add(
            TerminalLine(
                text = "🤖 [OPENCODE AI] Built-in AI Coding Agent ready (type 'opencode' to launch)",
                type = LineType.SYSTEM,
                customColor = Color(0xFF38BDF8)
            )
        )
        lines.add(
            TerminalLine(
                text = "OS: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) • Arch: $primaryAbi",
                type = LineType.SYSTEM
            )
        )
        lines.add(
            TerminalLine(
                text = "Commands: 'opencode', 'pkg list', 'apt list', 'python', 'git', 'adb devices', 'info', 'help'.",
                type = LineType.SYSTEM
            )
        )
        return lines
    }

    /**
     * Resolves a directory path relative to current working directory and HOME (~).
     */
    fun resolvePath(currentDir: String, target: String): File {
        val cleanTarget = target.trim()
        val path = when {
            cleanTarget == "~" || cleanTarget.isEmpty() -> homeDir
            cleanTarget.startsWith("~/") -> "$homeDir/${cleanTarget.substring(2)}"
            cleanTarget.startsWith("/") -> cleanTarget
            else -> "$currentDir/$cleanTarget"
        }
        return File(path).normalize()
    }

    /**
     * Executes a command in the specified tab.
     * Yields output lines in real time via onLineOutput.
     * Returns a CommandResult containing updated working dir and exit code.
     */
    suspend fun execute(
        tabId: String,
        rawCommand: String,
        currentDir: String,
        onLineOutput: (TerminalLine) -> Unit
    ): CommandResult = withContext(Dispatchers.IO) {
        val trimmed = rawCommand.trim()
        if (trimmed.isEmpty()) {
            return@withContext CommandResult(currentDir, 0)
        }

        // Check for built-ins first
        if (trimmed == "clear") {
            return@withContext CommandResult(currentDir, 0, isClear = true)
        }

        if (trimmed.startsWith("cd")) {
            val parts = trimmed.split(Regex("\\s+"), limit = 2)
            val target = if (parts.size > 1) parts[1].trim() else "~"
            val targetDir = resolvePath(currentDir, target)
            if (!targetDir.exists()) {
                onLineOutput(
                    TerminalLine(
                        text = "cd: no such file or directory: $target",
                        type = LineType.STDERR
                    )
                )
                return@withContext CommandResult(currentDir, 1)
            }
            if (!targetDir.isDirectory) {
                onLineOutput(
                    TerminalLine(
                        text = "cd: not a directory: $target",
                        type = LineType.STDERR
                    )
                )
                return@withContext CommandResult(currentDir, 1)
            }
            if (!targetDir.canRead() && !targetDir.canExecute()) {
                onLineOutput(
                    TerminalLine(
                        text = "cd: permission denied: $target",
                        type = LineType.STDERR
                    )
                )
                return@withContext CommandResult(currentDir, 1)
            }
            return@withContext CommandResult(targetDir.absolutePath, 0)
        }

        if (trimmed == "exit") {
            return@withContext CommandResult(currentDir, 0, isExit = true)
        }

        // Run command (as real root via su if available, or /system/bin/sh with universal function bootstrap)
        var exitCode = 0
        try {
            val escapedDir = currentDir.replace("'", "'\\''")
            val rootPrep = if (isRealRoot) "mount -o remount,rw / 2>/dev/null; mkdir -p /etc 2>/dev/null; " else ""
            val scriptFuncs = StringBuilder()
            PreinstalledScripts.toolsMap.forEach { (name, content) ->
                val lines = content.lines()
                val body = if (lines.firstOrNull()?.startsWith("#!") == true) {
                    lines.drop(1).joinToString("\n")
                } else {
                    content
                }
                val escapedContent = body.replace("'", "'\\''")
                scriptFuncs.append("$name() { /system/bin/sh -c '$escapedContent' -- \"\$@\"; }; ")
            }
            val funcDefs = scriptFuncs.toString()
            val cmdWrapper = "$rootPrep cd '$escapedDir' && export PATH=\"/system/bin:/system/xbin:/vendor/bin\" && export HOME='$homeDir' && $funcDefs $trimmed"

            val processBuilder = if (isRealRoot && suBinaryPath != null) {
                ProcessBuilder(suBinaryPath!!, "-c", cmdWrapper)
            } else {
                ProcessBuilder("/system/bin/sh", "-c", cmdWrapper)
            }
            processBuilder.directory(File(currentDir))
            processBuilder.redirectErrorStream(true)

            // Configure POSIX environment
            val env = processBuilder.environment()
            val existingPath = env["PATH"] ?: "/system/bin:/system/xbin"
            env["HOME"] = homeDir
            env["PATH"] = "$binDir:$existingPath:/vendor/bin:/apex/com.android.runtime/bin"
            env["TERM"] = "xterm-256color"
            env["TMPDIR"] = cacheDir
            env["SHELL"] = "/system/bin/sh"
            env["PWD"] = currentDir
            if (suBinaryPath != null) {
                env["REAL_SU"] = suBinaryPath!!
            }

            val process = processBuilder.start()
            activeProcesses[tabId] = process

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    onLineOutput(TerminalLine(text = it, type = LineType.STDOUT))
                }
            }
            exitCode = process.waitFor()
        } catch (e: InterruptedException) {
            onLineOutput(TerminalLine(text = "^C (Process interrupted)", type = LineType.STDERR))
            exitCode = 130
        } catch (e: Exception) {
            onLineOutput(
                TerminalLine(
                    text = "sh: execution error: ${e.message ?: "Unknown error"}",
                    type = LineType.STDERR
                )
            )
            exitCode = 127
        } finally {
            activeProcesses.remove(tabId)
        }

        CommandResult(currentDir, exitCode)
    }

    /**
     * Interrupts / kills the running process in a tab (Ctrl+C / SIGINT).
     */
    fun interrupt(tabId: String) {
        val process = activeProcesses[tabId]
        if (process != null) {
            try {
                process.destroy()
            } catch (_: Exception) {}
            activeProcesses.remove(tabId)
        }
    }

    /**
     * Autocompletion calculation for TAB key.
     */
    fun autocomplete(currentInput: String, currentDir: String): AutocompleteResult {
        val trimmed = currentInput.trimEnd()
        if (trimmed.isEmpty()) {
            return AutocompleteResult(currentInput, emptyList())
        }

        val lastSpaceIndex = trimmed.lastIndexOfAny(charArrayOf(' ', '|', '&', ';'))
        val prefix = if (lastSpaceIndex >= 0) trimmed.substring(lastSpaceIndex + 1) else trimmed
        val inputBeforeLastWord = if (lastSpaceIndex >= 0) trimmed.substring(0, lastSpaceIndex + 1) else ""

        val candidates = mutableSetOf<String>()

        // If it looks like a path or argument
        if (prefix.contains("/") || prefix.startsWith("~")) {
            val resolvedFile = resolvePath(currentDir, prefix)
            val parentDir = if (prefix.endsWith("/")) resolvedFile else (resolvedFile.parentFile ?: File(currentDir))
            val targetPrefix = if (prefix.endsWith("/")) "" else resolvedFile.name

            if (parentDir.exists() && parentDir.isDirectory) {
                parentDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith(targetPrefix)) {
                        val completion = if (file.isDirectory) file.name + "/" else file.name
                        candidates.add(completion)
                    }
                }
            }
        } else {
            // First word / command or current directory files
            // 1. Files in current dir
            val dir = File(currentDir)
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    if (file.name.startsWith(prefix)) {
                        val suffix = if (file.isDirectory) "/" else ""
                        candidates.add(file.name + suffix)
                    }
                }
            }
            // 2. Binaries in binDir and /system/bin
            listOf(File(binDir), File("/system/bin"), File("/system/xbin")).forEach { binFolder ->
                if (binFolder.exists() && binFolder.isDirectory) {
                    binFolder.listFiles()?.forEach { binary ->
                        if (binary.name.startsWith(prefix)) {
                            candidates.add(binary.name)
                        }
                    }
                }
            }
        }

        val sorted = candidates.sorted()
        return when {
            sorted.isEmpty() -> AutocompleteResult(currentInput, emptyList())
            sorted.size == 1 -> {
                val completion = sorted.first()
                val completedInput = if (prefix.contains("/")) {
                    val lastSlash = prefix.lastIndexOf('/')
                    val prefixPath = prefix.substring(0, lastSlash + 1)
                    inputBeforeLastWord + prefixPath + completion
                } else {
                    inputBeforeLastWord + completion
                }
                AutocompleteResult(completedInput, sorted, completed = true)
            }
            else -> {
                // Find longest common prefix
                val commonPrefix = longestCommonPrefix(sorted)
                val newPrefix = if (commonPrefix.length > prefix.length) commonPrefix else prefix
                val newInput = inputBeforeLastWord + newPrefix
                AutocompleteResult(newInput, sorted, completed = false)
            }
        }
    }

    private fun longestCommonPrefix(strings: List<String>): String {
        if (strings.isEmpty()) return ""
        var prefix = strings[0]
        for (i in 1 until strings.size) {
            while (!strings[i].startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.length - 1)
                if (prefix.isEmpty()) return ""
            }
        }
        return prefix
    }
}

data class CommandResult(
    val newWorkingDir: String,
    val exitCode: Int,
    val isClear: Boolean = false,
    val isExit: Boolean = false
)

data class AutocompleteResult(
    val updatedInput: String,
    val suggestions: List<String>,
    val completed: Boolean = false
)
