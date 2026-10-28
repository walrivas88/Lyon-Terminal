package com.example.terminal.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.terminal.viewmodel.TerminalViewModel
import com.example.ui.theme.RobotoMonoFontFamily

@Composable
fun TerminalScreen(
    viewModel: TerminalViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeTab = uiState.activeTab
    val settings = uiState.settings
    val theme = settings.theme
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    // Back button handling: dismiss sheets/dialogs first
    BackHandler(
        enabled = uiState.isSnippetsSheetOpen || uiState.isScriptsSheetOpen ||
            uiState.isSettingsDialogOpen || uiState.isRenameTabDialogOpen ||
            uiState.isAdbDialogOpen
    ) {
        if (uiState.isAdbDialogOpen) viewModel.openAdbDialog(false)
        if (uiState.isSnippetsSheetOpen) viewModel.openSnippetsSheet(false)
        if (uiState.isScriptsSheetOpen) viewModel.openScriptsSheet(false)
        if (uiState.isSettingsDialogOpen) viewModel.openSettingsDialog(false)
        if (uiState.isRenameTabDialogOpen) viewModel.dismissRenameTabDialog()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("terminal_main_scaffold"),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = theme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
             // Sleek Termux-style Tab Strip
            TerminalTabBar(
                tabs = uiState.tabs,
                activeTabId = uiState.activeTabId,
                homeDir = viewModel.getHomeDir(),
                theme = theme,
                showExtraKeys = settings.showExtraKeys,
                hasConnectedAdb = uiState.hasConnectedAdb,
                onSelectTab = { viewModel.selectTab(it) },
                onCloseTab = { viewModel.closeTab(it) },
                onNewTab = { viewModel.createNewTab() },
                onRenameTab = { viewModel.openRenameTabDialog(it) },
                onToggleExtraKeys = { viewModel.toggleExtraKeys(!settings.showExtraKeys) },
                onOpenSnippets = { viewModel.openSnippetsSheet(true) },
                onOpenScripts = { viewModel.openScriptsSheet(true) },
                onOpenAdb = { viewModel.openAdbDialog(true) }
            )

            // Terminal Console View
            if (activeTab != null) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    TerminalOutputView(
                        tab = activeTab,
                        homeDir = viewModel.getHomeDir(),
                        settings = settings,
                        focusRequester = focusRequester,
                        onInterrupt = { viewModel.interruptCommand(activeTab.id) },
                        onCopyOutput = {
                            viewModel.copySessionOutput(context, activeTab.id)
                            Toast.makeText(context, "Session output copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        onShareOutput = {
                            viewModel.shareSessionOutput(context, activeTab.id)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Optional Termux-style Extra Keys Bar
                    if (settings.showExtraKeys) {
                        ExtraKeysBar(
                            isCtrlActive = activeTab.isCtrlActive,
                            isAltActive = activeTab.isAltActive,
                            settings = settings,
                            onKeyClick = { key -> viewModel.handleExtraKey(key) }
                        )
                    }

                    // Command Input Line
                    TerminalInputRow(
                        tab = activeTab,
                        homeDir = viewModel.getHomeDir(),
                        settings = settings,
                        suggestions = uiState.autocompleteSuggestions,
                        focusRequester = focusRequester,
                        onInputChange = { viewModel.updateInput(activeTab.id, it) },
                        onSubmit = { viewModel.executeCommand(activeTab.id) },
                        onInterrupt = { viewModel.interruptCommand(activeTab.id) },
                        onApplySuggestion = { viewModel.applySuggestion(it) },
                        onHistoryPrev = { viewModel.historyPrev(activeTab.id) },
                        onHistoryNext = { viewModel.historyNext(activeTab.id) },
                        onAutocomplete = { viewModel.handleExtraKey("TAB") },
                        onClear = { viewModel.clearTab(activeTab.id) },
                        onNewTab = { viewModel.createNewTab() },
                        onCloseTab = { viewModel.closeTab(activeTab.id) },
                        onOpenPasteDialog = { viewModel.openPasteDialog(true) }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(theme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active tabs\nTap + or press Ctrl+T to create a new session",
                        fontFamily = RobotoMonoFontFamily,
                        color = theme.text.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    // Modal Sheets & Dialogs
    if (uiState.isSnippetsSheetOpen) {
        QuickSnippetsSheet(
            theme = theme,
            onDismiss = { viewModel.openSnippetsSheet(false) },
            onInsertSnippet = { snippet, runImmediately ->
                viewModel.insertSnippet(snippet, runImmediately)
            }
        )
    }

    if (uiState.isScriptsSheetOpen) {
        ScriptManagerSheet(
            scripts = uiState.scriptList,
            theme = theme,
            onDismiss = { viewModel.openScriptsSheet(false) },
            onRunScript = { scriptName -> viewModel.runScript(scriptName) },
            onLoadScript = { scriptName -> viewModel.loadScriptContent(scriptName) },
            onSaveScript = { name, content -> viewModel.saveScript(name, content) },
            onDeleteScript = { name -> viewModel.deleteScript(name) }
        )
    }

    if (uiState.isRenameTabDialogOpen && uiState.tabToRename != null) {
        RenameTabDialog(
            tab = uiState.tabToRename!!,
            theme = theme,
            onDismiss = { viewModel.dismissRenameTabDialog() },
            onConfirm = { newTitle ->
                viewModel.renameTab(uiState.tabToRename!!.id, newTitle)
            }
        )
    }

    if (uiState.isAdbDialogOpen) {
        AdbDialog(
            devices = uiState.adbDevices,
            localIpAddress = uiState.localIpAddress,
            theme = theme,
            onDismiss = { viewModel.openAdbDialog(false) },
            onConnect = { ip, port -> viewModel.connectAdb(ip, port) },
            onDisconnect = { serial -> viewModel.disconnectAdb(serial) },
            onLaunchShell = { serial -> viewModel.launchAdbShellTab(serial) },
            onEnableLocalServer = { viewModel.enableLocalAdbServer() },
            onRefreshDevices = { viewModel.refreshAdbDevices() }
        )
    }

    if (uiState.isPasteDialogOpen) {
        PasteDialog(
            theme = theme,
            onDismiss = { viewModel.openPasteDialog(false) },
            onInsert = { text, runImmediately ->
                viewModel.handlePastedText(text, runImmediately)
            }
        )
    }
}
