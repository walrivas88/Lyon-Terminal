package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Lux Terminal Brand Palette
val LuxCyan = Color(0xFF06B6D4)
val LuxCyanLight = Color(0xFF67E8F9)
val LuxEmerald = Color(0xFF10B981)
val LuxEmeraldLight = Color(0xFF34D399)
val LuxDarkBg = Color(0xFF0B0F19)
val LuxDarkSurface = Color(0xFF111827)
val LuxDarkCard = Color(0xFF1F2937)
val LuxDarkBorder = Color(0xFF374151)
val LuxTextPrimary = Color(0xFFF3F4F6)
val LuxTextSecondary = Color(0xFF9CA3AF)
val LuxTextMuted = Color(0xFF6B7280)

val LuxRed = Color(0xFFEF4444)
val LuxAmber = Color(0xFFF59E0B)
val LuxBlue = Color(0xFF3B82F6)
val LuxPurple = Color(0xFF8B5CF6)

// Theme Presets
data class TerminalThemePalette(
    val name: String,
    val background: Color,
    val surface: Color,
    val text: Color,
    val promptUser: Color,
    val promptPath: Color,
    val promptSymbol: Color,
    val commandText: Color,
    val cursorColor: Color,
    val selectionColor: Color,
    val ansiColors: List<Color>
)

object TerminalThemes {
    val TermuxClassic = TerminalThemePalette(
        name = "Termux Classic",
        background = Color(0xFF000000),
        surface = Color(0xFF141414),
        text = Color(0xFFE6E6E6),
        promptUser = Color(0xFF22C55E),
        promptPath = Color(0xFF38BDF8),
        promptSymbol = Color(0xFF22C55E),
        commandText = Color(0xFFFFFFFF),
        cursorColor = Color(0xFFE6E6E6),
        selectionColor = Color(0x5538BDF8),
        ansiColors = listOf(
            Color(0xFF000000), Color(0xFFEF4444), Color(0xFF22C55E), Color(0xFFEAB308),
            Color(0xFF3B82F6), Color(0xFFA855F7), Color(0xFF06B6D4), Color(0xFFE6E6E6),
            Color(0xFF6B7280), Color(0xFFF87171), Color(0xFF4ADE80), Color(0xFFFDE047),
            Color(0xFF60A5FA), Color(0xFFC084FC), Color(0xFF67E8F9), Color(0xFFFFFFFF)
        )
    )

    val Cyberpunk = TerminalThemePalette(
        name = "Lux Cyberpunk",
        background = Color(0xFF090D16),
        surface = Color(0xFF131B2E),
        text = Color(0xFFE2E8F0),
        promptUser = Color(0xFF06B6D4),
        promptPath = Color(0xFF10B981),
        promptSymbol = Color(0xFFA855F7),
        commandText = Color(0xFFF8FAFC),
        cursorColor = Color(0xFF06B6D4),
        selectionColor = Color(0x3306B6D4),
        ansiColors = listOf(
            Color(0xFF000000), Color(0xFFEF4444), Color(0xFF10B981), Color(0xFFF59E0B),
            Color(0xFF3B82F6), Color(0xFFA855F7), Color(0xFF06B6D4), Color(0xFFF1F5F9),
            Color(0xFF64748B), Color(0xFFF87171), Color(0xFF34D399), Color(0xFFFBBF24),
            Color(0xFF60A5FA), Color(0xFFC084FC), Color(0xFF38BDF8), Color(0xFFFFFFFF)
        )
    )

    val Matrix = TerminalThemePalette(
        name = "Matrix Green",
        background = Color(0xFF000800),
        surface = Color(0xFF021702),
        text = Color(0xFF00FF66),
        promptUser = Color(0xFF00FF66),
        promptPath = Color(0xFF22C55E),
        promptSymbol = Color(0xFF86EFAC),
        commandText = Color(0xFF4ADE80),
        cursorColor = Color(0xFF00FF66),
        selectionColor = Color(0x4400FF66),
        ansiColors = listOf(
            Color(0xFF001100), Color(0xFF00AA33), Color(0xFF00FF66), Color(0xFF33FF99),
            Color(0xFF008822), Color(0xFF00DD55), Color(0xFF55FFAA), Color(0xFFE0FFE0),
            Color(0xFF005511), Color(0xFF00CC44), Color(0xFF33FF77), Color(0xFF66FFAA),
            Color(0xFF11AA33), Color(0xFF22EE66), Color(0xFF77FFBB), Color(0xFFFFFFFF)
        )
    )

    val Monokai = TerminalThemePalette(
        name = "Monokai",
        background = Color(0xFF272822),
        surface = Color(0xFF383A32),
        text = Color(0xFFF8F8F2),
        promptUser = Color(0xFFF92672),
        promptPath = Color(0xFFA6E22E),
        promptSymbol = Color(0xFF66D9EF),
        commandText = Color(0xFFFD971F),
        cursorColor = Color(0xFFF8F8F0),
        selectionColor = Color(0x44F8F8F2),
        ansiColors = listOf(
            Color(0xFF272822), Color(0xFFF92672), Color(0xFFA6E22E), Color(0xFFF4BF75),
            Color(0xFF66D9EF), Color(0xFFAE81FF), Color(0xFFA1EFE4), Color(0xFFF8F8F2),
            Color(0xFF75715E), Color(0xFFFF5C57), Color(0xFF5AF78E), Color(0xFFF3F99D),
            Color(0xFF57C7FF), Color(0xFFFF6AC1), Color(0xFF9AEDFE), Color(0xFFF1F1F0)
        )
    )

    val SolarizedDark = TerminalThemePalette(
        name = "Solarized Dark",
        background = Color(0xFF002B36),
        surface = Color(0xFF073642),
        text = Color(0xFF839496),
        promptUser = Color(0xFF2AA198),
        promptPath = Color(0xFF859900),
        promptSymbol = Color(0xFFB58900),
        commandText = Color(0xFF93A1A1),
        cursorColor = Color(0xFF2AA198),
        selectionColor = Color(0x442AA198),
        ansiColors = listOf(
            Color(0xFF073642), Color(0xFFDC322F), Color(0xFF859900), Color(0xFFB58900),
            Color(0xFF268BD2), Color(0xFFD33682), Color(0xFF2AA198), Color(0xFFEEE8D5),
            Color(0xFF002B36), Color(0xFFCB4B16), Color(0xFF586E75), Color(0xFF657B83),
            Color(0xFF839496), Color(0xFF6C71C4), Color(0xFF93A1A1), Color(0xFFFDF6E3)
        )
    )

    val Nord = TerminalThemePalette(
        name = "Nord Frost",
        background = Color(0xFF2E3440),
        surface = Color(0xFF3B4252),
        text = Color(0xFFD8DEE9),
        promptUser = Color(0xFF88C0D0),
        promptPath = Color(0xFFA3BE8C),
        promptSymbol = Color(0xFFEBCB8B),
        commandText = Color(0xFFECEFF4),
        cursorColor = Color(0xFF88C0D0),
        selectionColor = Color(0x4488C0D0),
        ansiColors = listOf(
            Color(0xFF3B4252), Color(0xFFBF616A), Color(0xFFA3BE8C), Color(0xFFEBCB8B),
            Color(0xFF81A1C1), Color(0xFFB48EAD), Color(0xFF88C0D0), Color(0xFFE5E9F0),
            Color(0xFF4C566A), Color(0xFFD08770), Color(0xFFA3BE8C), Color(0xFFEBCB8B),
            Color(0xFF81A1C1), Color(0xFFB48EAD), Color(0xFF8FBCBB), Color(0xFFECEFF4)
        )
    )

    val AmberCRT = TerminalThemePalette(
        name = "Vintage Amber CRT",
        background = Color(0xFF140D02),
        surface = Color(0xFF241705),
        text = Color(0xFFFFB000),
        promptUser = Color(0xFFFFC040),
        promptPath = Color(0xFFFF9900),
        promptSymbol = Color(0xFFFFD480),
        commandText = Color(0xFFFFE0A0),
        cursorColor = Color(0xFFFFB000),
        selectionColor = Color(0x44FFB000),
        ansiColors = listOf(
            Color(0xFF140D02), Color(0xFFFF5500), Color(0xFFFFAA00), Color(0xFFFFCC00),
            Color(0xFFFF8800), Color(0xFFFF7700), Color(0xFFFFBB33), Color(0xFFFFE599),
            Color(0xFF553300), Color(0xFFFF6622), Color(0xFFFFBB00), Color(0xFFFFDD44),
            Color(0xFFFF9922), Color(0xFFFF8822), Color(0xFFFFCC55), Color(0xFFFFF2CC)
        )
    )

    val all = listOf(TermuxClassic, Cyberpunk, Matrix, Monokai, SolarizedDark, Nord, AmberCRT)

    fun getByName(name: String): TerminalThemePalette {
        return all.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: TermuxClassic
    }
}
