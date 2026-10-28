package com.example.terminal.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.terminal.engine.AnsiParser
import com.example.terminal.model.LineType
import com.example.terminal.model.TerminalLine
import com.example.terminal.model.TerminalSettings
import com.example.terminal.model.TerminalTab
import com.example.ui.theme.RobotoMonoFontFamily

@Composable
fun TerminalOutputView(
    tab: TerminalTab,
    homeDir: String,
    settings: TerminalSettings,
    focusRequester: FocusRequester,
    onInterrupt: () -> Unit,
    onCopyOutput: () -> Unit,
    onShareOutput: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = settings.theme
    val listState = rememberLazyListState()

    // Auto-scroll to bottom whenever new lines appear or execution toggles
    LaunchedEffect(tab.lines.size, tab.isRunning) {
        if (tab.lines.isNotEmpty()) {
            listState.animateScrollToItem(tab.lines.size)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                try {
                    focusRequester.requestFocus()
                } catch (_: Exception) {}
            }
            .testTag("terminal_output_view")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            items(
                items = tab.lines,
                key = { it.id }
            ) { line ->
                TerminalLineRow(
                    line = line,
                    homeDir = homeDir,
                    settings = settings
                )
            }

            // If process is actively running, show live execution status bar
            if (tab.isRunning) {
                item(key = "running_indicator") {
                    RunningProcessIndicator(
                        command = tab.runningCommand ?: "process",
                        settings = settings,
                        onInterrupt = onInterrupt
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Top right quick action icons (Copy log & Share log)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onCopyOutput,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(theme.surface.copy(alpha = 0.75f))
                    .testTag("copy_output_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Output",
                    tint = theme.text.copy(alpha = 0.7f),
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = onShareOutput,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(theme.surface.copy(alpha = 0.75f))
                    .testTag("share_output_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Output",
                    tint = theme.text.copy(alpha = 0.7f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun TerminalLineRow(
    line: TerminalLine,
    homeDir: String,
    settings: TerminalSettings
) {
    val theme = settings.theme
    val fontSize = settings.fontSizeSp.sp
    val lineHeight = (settings.fontSizeSp * 1.35f).sp

    when (line.type) {
        LineType.COMMAND -> {
            // Styled command entry: [path] $ command
            val annotated = buildAnnotatedString {
                val promptSep = if (line.text.contains(" # ")) " # " else " $ "
                val parts = line.text.split(promptSep, limit = 2)
                if (parts.size == 2) {
                    withStyle(SpanStyle(color = theme.promptPath, fontWeight = FontWeight.Bold)) {
                        append(parts[0])
                    }
                    val isRootPrompt = promptSep == " # "
                    withStyle(SpanStyle(color = if (isRootPrompt) Color(0xFFEF4444) else theme.promptSymbol, fontWeight = FontWeight.Bold)) {
                        append(promptSep)
                    }
                    withStyle(SpanStyle(color = theme.commandText, fontWeight = FontWeight.SemiBold)) {
                        append(parts[1])
                    }
                } else {
                    withStyle(SpanStyle(color = theme.commandText)) {
                        append(line.text)
                    }
                }
            }
            Text(
                text = annotated,
                fontFamily = RobotoMonoFontFamily,
                fontSize = fontSize,
                lineHeight = lineHeight,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
        }

        LineType.BANNER -> {
            Text(
                text = line.text,
                fontFamily = RobotoMonoFontFamily,
                fontSize = (settings.fontSizeSp + 1f).sp,
                fontWeight = FontWeight.Bold,
                color = theme.promptUser,
                lineHeight = lineHeight,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        LineType.SYSTEM -> {
            Text(
                text = line.text,
                fontFamily = RobotoMonoFontFamily,
                fontSize = fontSize,
                color = theme.promptPath,
                lineHeight = lineHeight,
                modifier = Modifier.padding(vertical = 1.dp)
            )
        }

        LineType.STDERR -> {
            Text(
                text = line.text,
                fontFamily = RobotoMonoFontFamily,
                fontSize = fontSize,
                color = Color(0xFFEF4444),
                lineHeight = lineHeight,
                modifier = Modifier.padding(vertical = 0.5.dp)
            )
        }

        LineType.STDOUT -> {
            // Parse ANSI escape sequences
            val parsed = AnsiParser.parse(line.text, theme)
            Text(
                text = parsed,
                fontFamily = RobotoMonoFontFamily,
                fontSize = fontSize,
                color = theme.text,
                lineHeight = lineHeight,
                modifier = Modifier.padding(vertical = 0.5.dp)
            )
        }
    }
}

@Composable
private fun RunningProcessIndicator(
    command: String,
    settings: TerminalSettings,
    onInterrupt: () -> Unit
) {
    val theme = settings.theme
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surface.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .alpha(alpha)
                        .clip(CircleShape)
                        .background(theme.promptUser)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Running: $command...",
                    fontFamily = RobotoMonoFontFamily,
                    fontSize = 12.sp,
                    color = theme.promptUser,
                    fontWeight = FontWeight.Medium
                )
            }

            OutlinedButton(
                onClick = onInterrupt,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                modifier = Modifier.height(28.dp),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Ctrl+C",
                    fontSize = 11.sp,
                    fontFamily = RobotoMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
