package com.example.terminal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.TerminalThemePalette

@Composable
fun PasteDialog(
    theme: TerminalThemePalette,
    onDismiss: () -> Unit,
    onInsert: (String, Boolean) -> Unit // text, runImmediately
) {
    var pastedText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.surface,
        title = {
            Text(
                text = "Paste Commands / Script",
                fontFamily = RobotoMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = theme.text
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Paste text from outside the emulator below:",
                    fontFamily = RobotoMonoFontFamily,
                    fontSize = 12.sp,
                    color = theme.text.copy(alpha = 0.7f)
                )
                OutlinedTextField(
                    value = pastedText,
                    onValueChange = { pastedText = it },
                    placeholder = {
                        Text(
                            text = "Paste your commands or script here...",
                            fontFamily = RobotoMonoFontFamily,
                            fontSize = 12.sp,
                            color = theme.text.copy(alpha = 0.3f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .testTag("paste_dialog_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = 13.sp,
                        color = theme.commandText
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.promptUser,
                        unfocusedBorderColor = theme.surface.copy(alpha = 0.5f),
                        cursorColor = theme.cursorColor
                    )
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = {
                        if (pastedText.isNotBlank()) {
                            onInsert(pastedText, false)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.testTag("paste_insert_button")
                ) {
                    Text(
                        text = "Insert",
                        fontFamily = RobotoMonoFontFamily,
                        color = theme.promptUser,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = {
                        if (pastedText.isNotBlank()) {
                            onInsert(pastedText, true)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.promptUser),
                    modifier = Modifier.testTag("paste_execute_button")
                ) {
                    Text(
                        text = "Run Now",
                        fontFamily = RobotoMonoFontFamily,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("paste_cancel_button")
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = RobotoMonoFontFamily,
                    color = theme.text.copy(alpha = 0.6f)
                )
            }
        }
    )
}
