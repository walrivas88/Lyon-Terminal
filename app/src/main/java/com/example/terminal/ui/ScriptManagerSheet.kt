package com.example.terminal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.terminal.model.ScriptFile
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.TerminalThemePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptManagerSheet(
    scripts: List<ScriptFile>,
    theme: TerminalThemePalette,
    onDismiss: () -> Unit,
    onRunScript: (String) -> Unit,
    onLoadScript: (String) -> String,
    onSaveScript: (String, String) -> Boolean,
    onDeleteScript: (String) -> Boolean
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Editing mode state
    var editingScriptName by remember { mutableStateOf<String?>(null) }
    var scriptContent by remember { mutableStateOf("") }
    var isNewScript by remember { mutableStateOf(false) }
    var saveStatusMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = theme.surface,
        dragHandle = null,
        modifier = Modifier.testTag("scripts_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(16.dp)
        ) {
            if (editingScriptName == null) {
                // SCRIPT LIST VIEW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Shell Scripts (~/scripts)",
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = theme.promptUser
                        )
                        Text(
                            text = "Create and run custom .sh scripts natively",
                            fontFamily = RobotoMonoFontFamily,
                            fontSize = 12.sp,
                            color = theme.text.copy(alpha = 0.6f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            onClick = {
                                editingScriptName = "myscript.sh"
                                scriptContent = "#!/system/bin/sh\n\necho \"Running custom script...\"\necho \"Current directory: \$(pwd)\"\nls -la\n"
                                isNewScript = true
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = theme.promptPath.copy(alpha = 0.2f),
                                contentColor = theme.promptPath
                            ),
                            modifier = Modifier.height(34.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "New", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New", fontFamily = RobotoMonoFontFamily, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = theme.text)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (scripts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No scripts found in ~/scripts\nTap '+ New' to create one!",
                            fontFamily = RobotoMonoFontFamily,
                            fontSize = 13.sp,
                            color = theme.text.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(scripts) { script ->
                            ScriptCard(
                                script = script,
                                theme = theme,
                                onRun = { onRunScript(script.name) },
                                onEdit = {
                                    editingScriptName = script.name
                                    scriptContent = onLoadScript(script.name)
                                    isNewScript = false
                                },
                                onDelete = { onDeleteScript(script.name) }
                            )
                        }
                    }
                }
            } else {
                // SCRIPT EDITOR VIEW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { editingScriptName = null }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = theme.text)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isNewScript) "New Script" else "Edit Script",
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = theme.promptUser
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            onClick = {
                                val name = editingScriptName?.trim() ?: "script.sh"
                                val saved = onSaveScript(name, scriptContent)
                                if (saved) {
                                    saveStatusMessage = "Saved!"
                                    isNewScript = false
                                } else {
                                    saveStatusMessage = "Error saving"
                                }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = theme.promptPath.copy(alpha = 0.25f),
                                contentColor = theme.promptPath
                            ),
                            modifier = Modifier.height(34.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontFamily = RobotoMonoFontFamily, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        FilledTonalButton(
                            onClick = {
                                val name = editingScriptName?.trim() ?: "script.sh"
                                onSaveScript(name, scriptContent)
                                onRunScript(name)
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = theme.promptUser.copy(alpha = 0.25f),
                                contentColor = theme.promptUser
                            ),
                            modifier = Modifier.height(34.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run", fontFamily = RobotoMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // File name text field
                OutlinedTextField(
                    value = editingScriptName ?: "",
                    onValueChange = { editingScriptName = it },
                    label = { Text("Script filename", fontFamily = RobotoMonoFontFamily) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.promptUser,
                        unfocusedBorderColor = theme.text.copy(alpha = 0.3f),
                        focusedTextColor = theme.text,
                        unfocusedTextColor = theme.text,
                        focusedContainerColor = theme.background,
                        unfocusedContainerColor = theme.background
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Code Editor Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.background)
                        .border(1.dp, theme.surface.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    BasicTextField(
                        value = scriptContent,
                        onValueChange = { scriptContent = it },
                        textStyle = TextStyle(
                            fontFamily = RobotoMonoFontFamily,
                            fontSize = 13.sp,
                            color = theme.commandText,
                            lineHeight = 18.sp
                        ),
                        cursorBrush = SolidColor(theme.cursorColor),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (saveStatusMessage != null) {
                    Text(
                        text = saveStatusMessage ?: "",
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = 11.sp,
                        color = theme.promptPath,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScriptCard(
    script: ScriptFile,
    theme: TerminalThemePalette,
    onRun: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.background),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.surface.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = script.name,
                    fontFamily = RobotoMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = theme.promptPath
                )
                Text(
                    text = "${script.sizeBytes} bytes • Executable: ${if (script.isExecutable) "Yes" else "No"}",
                    fontFamily = RobotoMonoFontFamily,
                    fontSize = 11.sp,
                    color = theme.text.copy(alpha = 0.5f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Text(
                        text = "Edit",
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = 12.sp,
                        color = theme.promptUser
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                FilledTonalButton(
                    onClick = onRun,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = theme.promptUser.copy(alpha = 0.2f),
                        contentColor = theme.promptUser
                    ),
                    modifier = Modifier.height(32.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run", fontFamily = RobotoMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            }
        }
    }
}
