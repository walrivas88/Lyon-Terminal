package com.example.terminal.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.terminal.engine.ShellEngine
import com.example.terminal.model.CommandSnippet
import com.example.terminal.model.CursorStyle
import com.example.terminal.model.LineType
import com.example.terminal.model.ScriptFile
import com.example.terminal.model.TerminalLine
import com.example.terminal.model.TerminalSettings
import com.example.terminal.model.TerminalTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import androidx.compose.ui.graphics.Color

import com.example.terminal.model.AdbDevice
import java.net.Inet4Address
import java.net.NetworkInterface

data class TerminalUiState(
    val tabs: List<TerminalTab> = emptyList(),
    val activeTabId: String = "",
    val settings: TerminalSettings = TerminalSettings(),
    val autocompleteSuggestions: List<String> = emptyList(),
    val scriptList: List<ScriptFile> = emptyList(),
    val isSnippetsSheetOpen: Boolean = false,
    val isScriptsSheetOpen: Boolean = false,
    val isSettingsDialogOpen: Boolean = false,
    val isRenameTabDialogOpen: Boolean = false,
    val tabToRename: TerminalTab? = null,
    val isAdbDialogOpen: Boolean = false,
    val isPasteDialogOpen: Boolean = false,
    val adbDevices: List<AdbDevice> = listOf(AdbDevice("127.0.0.1:5555", "device", "Android-x86 PC", isLocal = true)),
    val localIpAddress: String = "127.0.0.1"
) {
    val activeTab: TerminalTab?
        get() = tabs.find { it.id == activeTabId } ?: tabs.firstOrNull()

    val hasConnectedAdb: Boolean
        get() = adbDevices.isNotEmpty()
}

class TerminalViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = ShellEngine(application.applicationContext)
    private val scriptsDir = File(engine.homeDir, "scripts")

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    private var tabCounter = 1

    init {
        // Initialize default primary tab
        val firstTabId = UUID.randomUUID().toString()
        val firstTab = TerminalTab(
            id = firstTabId,
            title = "sh 1",
            workingDir = engine.homeDir,
            lines = engine.getInitialLines(),
            history = listOf("lux-info", "ls -la", "lux-help")
        )
        _uiState.update {
            it.copy(
                tabs = listOf(firstTab),
                activeTabId = firstTabId
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            engine.bootstrap()
            refreshScriptList()
        }
    }

    fun getHomeDir(): String = engine.homeDir

    fun createNewTab(initialCommand: String? = null) {
        tabCounter++
        val newId = UUID.randomUUID().toString()
        val newTab = TerminalTab(
            id = newId,
            title = "sh $tabCounter",
            workingDir = engine.homeDir,
            lines = engine.getInitialLines(),
            history = emptyList()
        )

        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + newTab,
                activeTabId = newId,
                autocompleteSuggestions = emptyList()
            )
        }

        if (!initialCommand.isNullOrBlank()) {
            executeCommand(newId, initialCommand)
        }
    }

    fun selectTab(tabId: String) {
        _uiState.update {
            it.copy(
                activeTabId = tabId,
                autocompleteSuggestions = emptyList()
            )
        }
    }

    fun closeTab(tabId: String) {
        engine.interrupt(tabId)
        val currentTabs = _uiState.value.tabs
        if (currentTabs.size <= 1) {
            // If closing the only tab, reset it with a fresh session
            val freshId = UUID.randomUUID().toString()
            tabCounter = 1
            val resetTab = TerminalTab(
                id = freshId,
                title = "sh 1",
                workingDir = engine.homeDir,
                lines = engine.getInitialLines()
            )
            _uiState.update {
                it.copy(
                    tabs = listOf(resetTab),
                    activeTabId = freshId,
                    autocompleteSuggestions = emptyList()
                )
            }
            return
        }

        val remainingTabs = currentTabs.filter { it.id != tabId }
        val newActiveId = if (_uiState.value.activeTabId == tabId) {
            val closedIndex = currentTabs.indexOfFirst { it.id == tabId }
            val nextIndex = (closedIndex.coerceAtMost(remainingTabs.size - 1)).coerceAtLeast(0)
            remainingTabs[nextIndex].id
        } else {
            _uiState.value.activeTabId
        }

        _uiState.update {
            it.copy(
                tabs = remainingTabs,
                activeTabId = newActiveId,
                autocompleteSuggestions = emptyList()
            )
        }
    }

    fun openRenameTabDialog(tab: TerminalTab) {
        _uiState.update {
            it.copy(isRenameTabDialogOpen = true, tabToRename = tab)
        }
    }

    fun dismissRenameTabDialog() {
        _uiState.update {
            it.copy(isRenameTabDialogOpen = false, tabToRename = null)
        }
    }

    fun renameTab(tabId: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        _uiState.update { state ->
            val updated = state.tabs.map {
                if (it.id == tabId) it.copy(title = trimmed) else it
            }
            state.copy(tabs = updated, isRenameTabDialogOpen = false, tabToRename = null)
        }
    }

    fun updateInput(tabId: String, text: String) {
        _uiState.update { state ->
            val updated = state.tabs.map {
                if (it.id == tabId) it.copy(currentInput = text) else it
            }
            state.copy(tabs = updated)
        }
    }

    fun clearTab(tabId: String) {
        _uiState.update { state ->
            val updated = state.tabs.map {
                if (it.id == tabId) it.copy(lines = emptyList()) else it
            }
            state.copy(tabs = updated)
        }
    }

    fun executeCommand(tabId: String, commandOverride: String? = null) {
        val tab = _uiState.value.tabs.find { it.id == tabId } ?: return
        val rawCommand = (commandOverride ?: tab.currentInput).trim()
        if (rawCommand.isEmpty() || tab.isRunning) return

        // Format command entry line
        val displayDir = tab.getDisplayDir(engine.homeDir)
        val promptHeader = "$displayDir ${tab.promptSymbol} $rawCommand"
        val commandLine = TerminalLine(
            text = promptHeader,
            type = LineType.COMMAND
        )

        val updatedHistory = if (tab.history.lastOrNull() != rawCommand) {
            tab.history + rawCommand
        } else {
            tab.history
        }



        // Set running state and clear input line
        _uiState.update { state ->
            val updatedTabs = state.tabs.map {
                if (it.id == tabId) {
                    it.copy(
                        lines = (it.lines + commandLine).takeLast(state.settings.maxBufferLines),
                        currentInput = "",
                        isRunning = true,
                        runningCommand = rawCommand.split(" ").firstOrNull(),
                        history = updatedHistory,
                        historyIndex = -1
                    )
                } else it
            }
            state.copy(tabs = updatedTabs, autocompleteSuggestions = emptyList())
        }

        viewModelScope.launch(Dispatchers.IO) {
            val result = engine.execute(
                tabId = tabId,
                rawCommand = rawCommand,
                currentDir = tab.workingDir,
                onLineOutput = { line ->
                    _uiState.update { state ->
                        val updatedTabs = state.tabs.map { currentTab ->
                            if (currentTab.id == tabId) {
                                currentTab.copy(
                                    lines = (currentTab.lines + line).takeLast(state.settings.maxBufferLines)
                                )
                            } else currentTab
                        }
                        state.copy(tabs = updatedTabs)
                    }
                }
            )

            withContext(Dispatchers.Main) {
                if (result.isExit) {
                    closeTab(tabId)
                    return@withContext
                }

                _uiState.update { state ->
                    val updatedTabs = state.tabs.map { currentTab ->
                        if (currentTab.id == tabId) {
                            val newLines = if (result.isClear) emptyList() else currentTab.lines
                            currentTab.copy(
                                workingDir = result.newWorkingDir,
                                isRunning = false,
                                runningCommand = null,
                                lastExitCode = result.exitCode,
                                lines = newLines
                            )
                        } else currentTab
                    }
                    state.copy(tabs = updatedTabs)
                }
            }
        }
    }

    fun interruptCommand(tabId: String) {
        engine.interrupt(tabId)
    }

    fun handleExtraKey(key: String) {
        val activeTab = _uiState.value.activeTab ?: return
        val tabId = activeTab.id

        when (key) {
            "ESC" -> {
                // Clear input or suggestions
                _uiState.update { state ->
                    val updatedTabs = state.tabs.map {
                        if (it.id == tabId) it.copy(currentInput = "", isCtrlActive = false, isAltActive = false) else it
                    }
                    state.copy(tabs = updatedTabs, autocompleteSuggestions = emptyList())
                }
            }
            "TAB" -> {
                val result = engine.autocomplete(activeTab.currentInput, activeTab.workingDir)
                if (result.completed) {
                    updateInput(tabId, result.updatedInput)
                    _uiState.update { it.copy(autocompleteSuggestions = emptyList()) }
                } else if (result.suggestions.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            tabs = it.tabs.map { t -> if (t.id == tabId) t.copy(currentInput = result.updatedInput) else t },
                            autocompleteSuggestions = result.suggestions
                        )
                    }
                }
            }
            "CTRL" -> {
                _uiState.update { state ->
                    val updated = state.tabs.map {
                        if (it.id == tabId) it.copy(isCtrlActive = !it.isCtrlActive) else it
                    }
                    state.copy(tabs = updated)
                }
            }
            "ALT" -> {
                _uiState.update { state ->
                    val updated = state.tabs.map {
                        if (it.id == tabId) it.copy(isAltActive = !it.isAltActive) else it
                    }
                    state.copy(tabs = updated)
                }
            }
            "UP" -> historyPrev(tabId)
            "DOWN" -> historyNext(tabId)
            "CLEAR" -> clearTab(tabId)
            "CTRL+C" -> {
                interruptCommand(tabId)
                _uiState.update { state ->
                    val updated = state.tabs.map {
                        if (it.id == tabId) it.copy(isCtrlActive = false) else it
                    }
                    state.copy(tabs = updated)
                }
            }
            else -> {
                // Character insertion or modifier shortcuts
                if (activeTab.isCtrlActive) {
                    when (key.lowercase()) {
                        "c" -> interruptCommand(tabId)
                        "l" -> clearTab(tabId)
                        "d" -> executeCommand(tabId, "exit")
                        "u" -> updateInput(tabId, "")
                        else -> appendInputChar(tabId, key)
                    }
                    _uiState.update { state ->
                        val updated = state.tabs.map {
                            if (it.id == tabId) it.copy(isCtrlActive = false) else it
                        }
                        state.copy(tabs = updated)
                    }
                } else {
                    appendInputChar(tabId, key)
                }
            }
        }
    }

    private fun appendInputChar(tabId: String, char: String) {
        val tab = _uiState.value.tabs.find { it.id == tabId } ?: return
        updateInput(tabId, tab.currentInput + char)
    }

    fun applySuggestion(suggestion: String) {
        val activeTab = _uiState.value.activeTab ?: return
        val currentInput = activeTab.currentInput
        val lastSpaceIndex = currentInput.lastIndexOfAny(charArrayOf(' ', '|', '&', ';'))
        val newInput = if (lastSpaceIndex >= 0) {
            val prefix = currentInput.substring(0, lastSpaceIndex + 1)
            if (currentInput.contains("/")) {
                val lastSlash = currentInput.lastIndexOf('/')
                val dirPrefix = currentInput.substring(0, lastSlash + 1)
                dirPrefix + suggestion
            } else {
                prefix + suggestion
            }
        } else {
            suggestion
        }
        updateInput(activeTab.id, newInput)
        _uiState.update { it.copy(autocompleteSuggestions = emptyList()) }
    }

    fun historyPrev(tabId: String) {
        val tab = _uiState.value.tabs.find { it.id == tabId } ?: return
        if (tab.history.isEmpty()) return

        val newIndex = if (tab.historyIndex == -1) {
            tab.history.size - 1
        } else {
            (tab.historyIndex - 1).coerceAtLeast(0)
        }

        val recalled = tab.history[newIndex]
        _uiState.update { state ->
            val updated = state.tabs.map {
                if (it.id == tabId) it.copy(currentInput = recalled, historyIndex = newIndex) else it
            }
            state.copy(tabs = updated)
        }
    }

    fun historyNext(tabId: String) {
        val tab = _uiState.value.tabs.find { it.id == tabId } ?: return
        if (tab.history.isEmpty() || tab.historyIndex == -1) return

        val newIndex = tab.historyIndex + 1
        if (newIndex >= tab.history.size) {
            _uiState.update { state ->
                val updated = state.tabs.map {
                    if (it.id == tabId) it.copy(currentInput = "", historyIndex = -1) else it
                }
                state.copy(tabs = updated)
            }
        } else {
            val recalled = tab.history[newIndex]
            _uiState.update { state ->
                val updated = state.tabs.map {
                    if (it.id == tabId) it.copy(currentInput = recalled, historyIndex = newIndex) else it
                }
                state.copy(tabs = updated)
            }
        }
    }

    // Snippets & Scripting
    fun openSnippetsSheet(open: Boolean) {
        _uiState.update { it.copy(isSnippetsSheetOpen = open) }
    }

    fun openScriptsSheet(open: Boolean) {
        if (open) refreshScriptList()
        _uiState.update { it.copy(isScriptsSheetOpen = open) }
    }

    fun openSettingsDialog(open: Boolean) {
        _uiState.update { it.copy(isSettingsDialogOpen = open) }
    }

    fun insertSnippet(snippet: CommandSnippet, executeImmediately: Boolean = false) {
        val activeTab = _uiState.value.activeTab ?: return
        if (executeImmediately) {
            executeCommand(activeTab.id, snippet.command)
        } else {
            updateInput(activeTab.id, snippet.command)
        }
        openSnippetsSheet(false)
    }

    fun refreshScriptList() {
        if (!scriptsDir.exists()) scriptsDir.mkdirs()
        val list = scriptsDir.listFiles()?.map { file ->
            ScriptFile(
                name = file.name,
                path = file.absolutePath,
                sizeBytes = file.length(),
                lastModified = file.lastModified(),
                isExecutable = file.canExecute()
            )
        }?.sortedBy { it.name } ?: emptyList()

        _uiState.update { it.copy(scriptList = list) }
    }

    fun saveScript(name: String, content: String): Boolean {
        return try {
            val file = File(scriptsDir, name.trim())
            file.writeText(content)
            file.setExecutable(true, false)
            refreshScriptList()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun loadScriptContent(name: String): String {
        return try {
            val file = File(scriptsDir, name)
            if (file.exists()) file.readText() else ""
        } catch (_: Exception) {
            ""
        }
    }

    fun deleteScript(name: String): Boolean {
        return try {
            val file = File(scriptsDir, name)
            val deleted = file.delete()
            refreshScriptList()
            deleted
        } catch (_: Exception) {
            false
        }
    }

    fun runScript(name: String) {
        val activeTab = _uiState.value.activeTab ?: return
        val command = "sh ~/scripts/$name"
        executeCommand(activeTab.id, command)
        openScriptsSheet(false)
    }

    // Settings Updates
    fun updateTheme(themeName: String) {
        _uiState.update {
            it.copy(settings = it.settings.copy(themeName = themeName))
        }
    }

    fun updateFontSize(fontSizeSp: Float) {
        _uiState.update {
            it.copy(settings = it.settings.copy(fontSizeSp = fontSizeSp.coerceIn(10f, 24f)))
        }
    }

    fun toggleExtraKeys(show: Boolean) {
        _uiState.update {
            it.copy(settings = it.settings.copy(showExtraKeys = show))
        }
    }

    fun toggleHaptics(enable: Boolean) {
        _uiState.update {
            it.copy(settings = it.settings.copy(hapticFeedback = enable))
        }
    }

    fun updateCursorStyle(style: CursorStyle) {
        _uiState.update {
            it.copy(settings = it.settings.copy(cursorStyle = style))
        }
    }

    // ADB Connection Management
    fun openAdbDialog(open: Boolean) {
        if (open) {
            refreshAdbDevices()
        }
        _uiState.update { it.copy(isAdbDialogOpen = open) }
    }

    fun refreshAdbDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            var detectedIp = "127.0.0.1"
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val iface = interfaces.nextElement()
                    if (iface.isLoopback || !iface.isUp) continue
                    val addresses = iface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val addr = addresses.nextElement()
                        if (addr is Inet4Address && !addr.isLoopbackAddress) {
                            detectedIp = addr.hostAddress ?: detectedIp
                            break
                        }
                    }
                }
            } catch (_: Exception) {}

            val devicesList = mutableListOf<AdbDevice>()
            devicesList.add(AdbDevice("127.0.0.1:5555", "device", "Android-x86 PC", isLocal = true))

            val devicesFile = File(engine.homeDir, ".adb_devices")
            if (devicesFile.exists()) {
                devicesFile.readLines().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty()) {
                        val parts = trimmed.split(Regex("\\s+"))
                        val addr = parts[0]
                        val model = if (parts.size > 1) parts[1] else "Android Device"
                        if (addr != "127.0.0.1:5555") {
                            devicesList.add(AdbDevice(addr, "device", model, isLocal = false))
                        }
                    }
                }
            }

            _uiState.update {
                it.copy(
                    adbDevices = devicesList.distinctBy { d -> d.serial },
                    localIpAddress = detectedIp
                )
            }
        }
    }

    fun connectAdb(ip: String, port: String) {
        val target = "$ip:$port"
        val activeTab = _uiState.value.activeTab
        if (activeTab != null) {
            executeCommand(activeTab.id, "adb connect $target")
        }
        viewModelScope.launch(Dispatchers.IO) {
            val devicesFile = File(engine.homeDir, ".adb_devices")
            devicesFile.appendText("$target Android-x86\n")
            refreshAdbDevices()
        }
        openAdbDialog(false)
    }

    fun disconnectAdb(serial: String) {
        val activeTab = _uiState.value.activeTab
        if (activeTab != null) {
            executeCommand(activeTab.id, "adb disconnect $serial")
        }
        viewModelScope.launch(Dispatchers.IO) {
            val devicesFile = File(engine.homeDir, ".adb_devices")
            if (devicesFile.exists()) {
                val updated = devicesFile.readLines().filterNot { it.startsWith(serial) }
                devicesFile.writeText(updated.joinToString("\n") + "\n")
            }
            refreshAdbDevices()
        }
    }

    fun launchAdbShellTab(serial: String) {
        openAdbDialog(false)
        createNewTab(initialCommand = "adb -s $serial shell 2>/dev/null || adb shell")
    }

    fun enableLocalAdbServer() {
        val activeTab = _uiState.value.activeTab
        if (activeTab != null) {
            executeCommand(activeTab.id, "adb tcpip 5555")
        }
        openAdbDialog(false)
    }

    // Output Sharing & Copying
    fun copySessionOutput(context: Context, tabId: String) {
        val tab = _uiState.value.tabs.find { it.id == tabId } ?: return
        val outputText = tab.lines.joinToString("\n") { it.text }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Lux Terminal Output", outputText)
        clipboard.setPrimaryClip(clip)
    }

    fun shareSessionOutput(context: Context, tabId: String) {
        val tab = _uiState.value.tabs.find { it.id == tabId } ?: return
        val outputText = tab.lines.joinToString("\n") { it.text }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, outputText)
            putExtra(Intent.EXTRA_SUBJECT, "Lux Terminal - ${tab.title}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Terminal Session")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun openPasteDialog(open: Boolean) {
        _uiState.update { it.copy(isPasteDialogOpen = open) }
    }

    fun handlePastedText(text: String, runImmediately: Boolean) {
        val activeTab = _uiState.value.activeTab ?: return
        val tabId = activeTab.id
        if (runImmediately) {
            executeCommand(tabId, text)
        } else {
            updateInput(tabId, activeTab.currentInput + text)
        }
    }
}
