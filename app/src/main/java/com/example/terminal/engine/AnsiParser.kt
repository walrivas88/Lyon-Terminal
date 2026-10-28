package com.example.terminal.engine

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.TerminalThemePalette

object AnsiParser {

    private val ANSI_REGEX = Regex("\u001B\\[([0-9;]*)m")

    /**
     * Parses an ANSI-escaped string into a Jetpack Compose AnnotatedString
     * mapped to the provided terminal theme palette colors.
     */
    fun parse(rawText: String, theme: TerminalThemePalette): AnnotatedString {
        // If there are no escape codes, return raw text simply
        if (!rawText.contains('\u001B')) {
            return AnnotatedString(rawText)
        }

        return buildAnnotatedString {
            var currentIndex = 0
            var currentColor: Color? = null
            var currentBgColor: Color? = null
            var isBold = false
            var isUnderline = false

            val matches = ANSI_REGEX.findAll(rawText)

            for (match in matches) {
                // Append text preceding the ANSI code
                if (match.range.first > currentIndex) {
                    val textChunk = rawText.substring(currentIndex, match.range.first)
                    val startPos = length
                    append(textChunk)
                    if (currentColor != null || isBold || currentBgColor != null) {
                        addStyle(
                            SpanStyle(
                                color = currentColor ?: theme.text,
                                background = currentBgColor ?: Color.Transparent,
                                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
                            ),
                            startPos,
                            length
                        )
                    }
                }

                // Process ANSI codes
                val codeString = match.groupValues[1]
                if (codeString.isEmpty() || codeString == "0") {
                    // Reset all styles
                    currentColor = null
                    currentBgColor = null
                    isBold = false
                    isUnderline = false
                } else {
                    val codes = codeString.split(";").mapNotNull { it.toIntOrNull() }
                    var i = 0
                    while (i < codes.size) {
                        when (val code = codes[i]) {
                            0 -> {
                                currentColor = null
                                currentBgColor = null
                                isBold = false
                                isUnderline = false
                            }
                            1 -> isBold = true
                            4 -> isUnderline = true
                            22 -> isBold = false
                            24 -> isUnderline = false
                            39 -> currentColor = null
                            49 -> currentBgColor = null
                            in 30..37 -> {
                                // Standard foreground colors (0-7)
                                val colorIndex = code - 30
                                if (colorIndex < theme.ansiColors.size) {
                                    currentColor = theme.ansiColors[colorIndex]
                                }
                            }
                            in 90..97 -> {
                                // Bright foreground colors (8-15)
                                val colorIndex = (code - 90) + 8
                                if (colorIndex < theme.ansiColors.size) {
                                    currentColor = theme.ansiColors[colorIndex]
                                }
                            }
                            in 40..47 -> {
                                // Standard background colors
                                val colorIndex = code - 40
                                if (colorIndex < theme.ansiColors.size) {
                                    currentBgColor = theme.ansiColors[colorIndex].copy(alpha = 0.35f)
                                }
                            }
                            38 -> {
                                // 256 color mode foreground: 38;5;n
                                if (i + 2 < codes.size && codes[i + 1] == 5) {
                                    val color256 = codes[i + 2]
                                    currentColor = get256Color(color256, theme)
                                    i += 2
                                }
                            }
                            48 -> {
                                // 256 color mode background: 48;5;n
                                if (i + 2 < codes.size && codes[i + 1] == 5) {
                                    val color256 = codes[i + 2]
                                    currentBgColor = get256Color(color256, theme).copy(alpha = 0.35f)
                                    i += 2
                                }
                            }
                        }
                        i++
                    }
                }

                currentIndex = match.range.last + 1
            }

            // Append any remaining text after the last match
            if (currentIndex < rawText.length) {
                val remaining = rawText.substring(currentIndex)
                val startPos = length
                append(remaining)
                if (currentColor != null || isBold || currentBgColor != null) {
                    addStyle(
                        SpanStyle(
                            color = currentColor ?: theme.text,
                            background = currentBgColor ?: Color.Transparent,
                            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
                        ),
                        startPos,
                        length
                    )
                }
            }
        }
    }

    private fun get256Color(index: Int, theme: TerminalThemePalette): Color {
        return when {
            index in 0..15 -> theme.ansiColors.getOrElse(index) { theme.text }
            index in 16..231 -> {
                // 6x6x6 color cube
                val r = ((index - 16) / 36) * 51
                val g = (((index - 16) % 36) / 6) * 51
                val b = ((index - 16) % 6) * 51
                Color(r, g, b)
            }
            index in 232..255 -> {
                // Grayscale ramp
                val gray = (index - 232) * 10 + 8
                Color(gray, gray, gray)
            }
            else -> theme.text
        }
    }
}
