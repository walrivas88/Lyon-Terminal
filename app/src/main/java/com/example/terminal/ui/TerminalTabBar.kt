package com.example.terminal.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.terminal.model.TerminalTab
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.TerminalThemePalette

@Composable
fun TerminalTabBar(
    tabs: List<TerminalTab>,
    activeTabId: String,
    homeDir: String,
    theme: TerminalThemePalette,
    showExtraKeys: Boolean,
    hasConnectedAdb: Boolean = false,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: () -> Unit,
    onRenameTab: (TerminalTab) -> Unit,
    onToggleExtraKeys: () -> Unit,
    onOpenSnippets: () -> Unit,
    onOpenScripts: () -> Unit,
    onOpenAdb: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("terminal_tab_bar"),
        color = theme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scrollable tabs strip
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isActive = tab.id == activeTabId
                    TabItem(
                        tab = tab,
                        index = index + 1,
                        isActive = isActive,
                        homeDir = homeDir,
                        theme = theme,
                        onSelect = { onSelectTab(tab.id) },
                        onClose = { onCloseTab(tab.id) },
                        onRename = { onRenameTab(tab) }
                    )
                }

                // New tab '+' button
                IconButton(
                    onClick = onNewTab,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(theme.background.copy(alpha = 0.6f))
                        .testTag("add_tab_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Tab (Ctrl+T)",
                        tint = theme.promptUser,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Action buttons on the right
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Root status pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        )
                        Text(
                            text = "ROOT",
                            fontFamily = RobotoMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                // ADB status pill button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (hasConnectedAdb) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (hasConnectedAdb) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFF59E0B).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable { onOpenAdb() }
                        .padding(end = 4.dp)
                        .testTag("adb_status_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (hasConnectedAdb) Color(0xFF10B981) else Color(0xFFF59E0B))
                        )
                        Text(
                            text = "ADB",
                            fontFamily = RobotoMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasConnectedAdb) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }
                // Toggle On-Screen Extra Keys (especially useful on Bliss OS / PC to hide soft keyboard row)
                IconButton(
                    onClick = onToggleExtraKeys,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("toggle_extra_keys_button")
                ) {
                    Icon(
                        imageVector = if (showExtraKeys) Icons.Default.KeyboardHide else Icons.Default.Keyboard,
                        contentDescription = if (showExtraKeys) "Hide On-Screen Keys" else "Show On-Screen Keys",
                        tint = if (showExtraKeys) theme.promptUser else theme.text.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onOpenSnippets,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("snippets_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Quick Snippets",
                        tint = theme.promptPath,
                        modifier = Modifier.size(18.dp)
                    )
                }



                IconButton(
                    onClick = onOpenScripts,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("scripts_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Script Manager",
                        tint = theme.promptSymbol,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: TerminalTab,
    index: Int,
    isActive: Boolean,
    homeDir: String,
    theme: TerminalThemePalette,
    onSelect: () -> Unit,
    onClose: () -> Unit,
    onRename: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = if (isActive) theme.background else theme.surface.copy(alpha = 0.5f),
        label = "tab_bg"
    )
    val borderColor = if (isActive) theme.promptUser else Color.Transparent

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable { onSelect() }
            .padding(start = 8.dp, end = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Status dot: pulsing cyan if running, green if idle, red if error
            val statusColor = when {
                tab.isRunning -> theme.promptUser
                tab.lastExitCode != null && tab.lastExitCode != 0 -> Color(0xFFEF4444)
                else -> theme.promptPath
            }
            val dotAlpha = if (tab.isRunning) pulseAlpha else 1.0f

            Box(
                modifier = Modifier
                    .size(7.dp)
                    .alpha(dotAlpha)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            // Title and Directory Badge
            Column {
                val displayTitle = tab.title.ifBlank { "sh $index" }
                Text(
                    text = displayTitle,
                    fontFamily = RobotoMonoFontFamily,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (isActive) theme.text else theme.text.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // More / Menu dropdown button
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Tab Options",
                        tint = theme.text.copy(alpha = 0.4f),
                        modifier = Modifier.size(13.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename Tab") },
                        onClick = {
                            showMenu = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Close Tab (Ctrl+W)") },
                        onClick = {
                            showMenu = false
                            onClose()
                        }
                    )
                }
            }

            // Quick Close icon button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(22.dp)
                    .testTag("close_tab_${tab.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Tab",
                    tint = theme.text.copy(alpha = 0.5f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
