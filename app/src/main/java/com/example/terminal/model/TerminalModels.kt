package com.example.terminal.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TerminalThemePalette
import com.example.ui.theme.TerminalThemes

enum class LineType {
    COMMAND,   // User entered command line
    STDOUT,    // Standard output from command
    STDERR,    // Error output from command
    SYSTEM,    // System information message
    BANNER     // Welcome banner / ASCII art
}

data class TerminalLine(
    val id: Long = System.nanoTime(),
    val text: String,
    val type: LineType = LineType.STDOUT,
    val customColor: Color? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class CursorStyle(val label: String) {
    BLOCK("Block █"),
    LINE("Line |"),
    UNDERLINE("Underline _")
}

data class TerminalSettings(
    val fontSizeSp: Float = 13.0f,
    val themeName: String = "Termux Classic",
    val showExtraKeys: Boolean = true,
    val hapticFeedback: Boolean = true,
    val cursorStyle: CursorStyle = CursorStyle.BLOCK,
    val maxBufferLines: Int = 1200
) {
    val theme: TerminalThemePalette
        get() = TerminalThemes.getByName(themeName)
}

data class TerminalTab(
    val id: String,
    val title: String,
    val workingDir: String,
    val lines: List<TerminalLine> = emptyList(),
    val history: List<String> = emptyList(),
    val historyIndex: Int = -1,
    val isRunning: Boolean = false,
    val currentInput: String = "",
    val runningCommand: String? = null,
    val lastExitCode: Int? = null,
    val isCtrlActive: Boolean = false,
    val isAltActive: Boolean = false,
    val isRoot: Boolean = true
) {
    // Return friendly display for working directory (e.g. ~/scripts or /system/bin)
    fun getDisplayDir(homeDir: String): String {
        return when {
            workingDir == homeDir -> "~"
            workingDir.startsWith(homeDir) -> "~" + workingDir.removePrefix(homeDir)
            else -> workingDir
        }
    }

    val promptSymbol: String
        get() = if (isRoot) "#" else "$"
}

data class CommandSnippet(
    val category: String,
    val name: String,
    val command: String,
    val description: String
)

data class ScriptFile(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val isExecutable: Boolean
)

data class AdbDevice(
    val serial: String,
    val state: String = "device",
    val model: String = "Android-x86 PC",
    val isLocal: Boolean = false
)


