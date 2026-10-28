package com.example.terminal.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.terminal.model.TerminalSettings
import com.example.terminal.model.TerminalTab
import com.example.ui.theme.RobotoMonoFontFamily

@Composable
fun TerminalInputRow(
    tab: TerminalTab,
    homeDir: String,
    settings: TerminalSettings,
    suggestions: List<String>,
    focusRequester: FocusRequester,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onInterrupt: () -> Unit,
    onApplySuggestion: (String) -> Unit,
    onHistoryPrev: () -> Unit,
    onHistoryNext: () -> Unit,
    onAutocomplete: () -> Unit,
    onClear: () -> Unit,
    onNewTab: () -> Unit,
    onCloseTab: () -> Unit,
    onOpenPasteDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = settings.theme
    val displayDir = tab.getDisplayDir(homeDir)
    val clipboardManager = LocalClipboardManager.current

    // Automatically request focus on startup or tab switch
    LaunchedEffect(tab.id) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("terminal_input_row"),
        color = theme.surface,
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Autocomplete suggestions chips row
            AnimatedVisibility(
                visible = suggestions.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.background)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TAB:",
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = 11.sp,
                        color = theme.promptUser,
                        fontWeight = FontWeight.Bold
                    )
                    suggestions.forEach { suggestion ->
                        SuggestionChip(
                            text = suggestion,
                            themeColor = theme.promptPath,
                            onClick = { onApplySuggestion(suggestion) }
                        )
                    }
                }
            }

            // Input command row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prompt prefix: [dir] $
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Text(
                        text = displayDir,
                        fontFamily = RobotoMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = theme.promptPath
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = tab.promptSymbol,
                        fontFamily = RobotoMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (tab.isRoot) Color(0xFFEF4444) else theme.promptSymbol
                    )
                }

                // Text field with physical hardware keyboard event handling (PC / Bliss OS)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(theme.background)
                        .border(1.dp, theme.surface.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = tab.currentInput,
                        onValueChange = onInputChange,
                        textStyle = TextStyle(
                            fontFamily = RobotoMonoFontFamily,
                            fontSize = 13.5.sp,
                            color = theme.commandText
                        ),
                        cursorBrush = SolidColor(theme.cursorColor),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Send,
                            autoCorrectEnabled = false
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = { onSubmit() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onPreviewKeyEvent { event ->
                                if (event.type == KeyEventType.KeyDown) {
                                    // Bliss OS / Desktop Physical Keyboard Shortcuts:
                                    if (event.isCtrlPressed) {
                                        when (event.key) {
                                            Key.C -> {
                                                onInterrupt()
                                                return@onPreviewKeyEvent true
                                            }
                                            Key.L -> {
                                                onClear()
                                                return@onPreviewKeyEvent true
                                            }
                                            Key.V -> {
                                                val clipText = clipboardManager.getText()?.text
                                                if (!clipText.isNullOrEmpty()) {
                                                    onInputChange(tab.currentInput + clipText)
                                                }
                                                return@onPreviewKeyEvent true
                                            }
                                            Key.D -> {
                                                if (tab.currentInput.isEmpty()) {
                                                    onCloseTab()
                                                }
                                                return@onPreviewKeyEvent true
                                            }
                                            Key.T -> {
                                                onNewTab()
                                                return@onPreviewKeyEvent true
                                            }
                                            Key.W -> {
                                                onCloseTab()
                                                return@onPreviewKeyEvent true
                                            }
                                        }
                                    }

                                    when (event.key) {
                                        Key.DirectionUp -> {
                                            onHistoryPrev()
                                            return@onPreviewKeyEvent true
                                        }
                                        Key.DirectionDown -> {
                                            onHistoryNext()
                                            return@onPreviewKeyEvent true
                                        }
                                        Key.Tab -> {
                                            onAutocomplete()
                                            return@onPreviewKeyEvent true
                                        }
                                        Key.Enter -> {
                                            onSubmit()
                                            return@onPreviewKeyEvent true
                                        }
                                    }
                                }
                                false
                            }
                            .testTag("command_input_field")
                    )

                    if (tab.currentInput.isEmpty()) {
                        Text(
                            text = "type command... (e.g. ls, lux-info)",
                            fontFamily = RobotoMonoFontFamily,
                            fontSize = 12.sp,
                            color = theme.text.copy(alpha = 0.35f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Paste clipboard button
                IconButton(
                    onClick = onOpenPasteDialog,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("paste_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste from Clipboard",
                        tint = theme.promptUser,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Clear input button
                if (tab.currentInput.isNotEmpty()) {
                    IconButton(
                        onClick = { onInputChange("") },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("clear_input_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Input",
                            tint = theme.text.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // If running: show Stop / Ctrl+C button, else Submit button
                if (tab.isRunning) {
                    IconButton(
                        onClick = onInterrupt,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                            .testTag("stop_process_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Process",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onSubmit,
                        enabled = tab.currentInput.isNotBlank(),
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (tab.currentInput.isNotBlank()) theme.promptUser.copy(alpha = 0.2f) else Color.Transparent)
                            .testTag("submit_command_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Execute Command",
                            tint = if (tab.currentInput.isNotBlank()) theme.promptUser else theme.text.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    themeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(themeColor.copy(alpha = 0.15f))
            .border(1.dp, themeColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontFamily = RobotoMonoFontFamily,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = themeColor
        )
    }
}
