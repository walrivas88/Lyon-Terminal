package com.example.terminal.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.terminal.model.TerminalSettings
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.TerminalThemePalette

@Composable
fun ExtraKeysBar(
    isCtrlActive: Boolean,
    isAltActive: Boolean,
    settings: TerminalSettings,
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = settings.theme
    val view = LocalView.current

    val keys = listOf(
        "ESC", "TAB", "CTRL", "ALT", "UP", "DOWN",
        "|", "/", "-", "~", "&", ">", "$", "CLEAR"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("extra_keys_bar"),
        color = theme.surface,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            keys.forEach { keyLabel ->
                val isModifierSelected = (keyLabel == "CTRL" && isCtrlActive) || (keyLabel == "ALT" && isAltActive)

                KeyItem(
                    label = keyLabel,
                    isSelected = isModifierSelected,
                    theme = theme,
                    onClick = {
                        if (settings.hapticFeedback) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                        onKeyClick(keyLabel)
                    }
                )
            }
        }
    }
}

@Composable
private fun KeyItem(
    label: String,
    isSelected: Boolean,
    theme: TerminalThemePalette,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> theme.promptUser
        label in listOf("ESC", "CLEAR") -> theme.background.copy(alpha = 0.9f)
        label in listOf("CTRL", "ALT") -> theme.surface.copy(alpha = 0.8f)
        else -> theme.background
    }

    val textColor = when {
        isSelected -> Color.Black
        label in listOf("CTRL", "ALT") -> theme.promptUser
        label == "CLEAR" -> Color(0xFFEF4444)
        label in listOf("|", "/", "-", "~", "&", ">", "$") -> theme.promptSymbol
        else -> theme.text
    }

    val borderColor = if (isSelected) theme.promptUser else theme.surface.copy(alpha = 0.7f)

    Box(
        modifier = Modifier
            .height(38.dp)
            .widthIn(min = 44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp)
            .testTag("extra_key_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = RobotoMonoFontFamily,
            fontSize = 12.sp,
            fontWeight = if (isSelected || label in listOf("CTRL", "ALT", "TAB")) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}
