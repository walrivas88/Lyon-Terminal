package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.terminal.engine.AnsiParser
import com.example.terminal.engine.ShellEngine
import com.example.ui.theme.TerminalThemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Lyon Terminal", appName)
    }

    @Test
    fun `ansi parser decodes clean text`() {
        val raw = "Hello \u001B[32mWorld\u001B[0m!"
        val parsed = AnsiParser.parse(raw, TerminalThemes.Cyberpunk)
        assertEquals("Hello World!", parsed.text)
        assertTrue(parsed.spanStyles.isNotEmpty())
    }

    @Test
    fun `shell engine bootstrap and path resolution`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = ShellEngine(context)
        assertNotNull(engine.homeDir)

        val resolvedHome = engine.resolvePath(engine.homeDir, "~")
        assertEquals(engine.homeDir, resolvedHome.absolutePath)

        val resolvedSub = engine.resolvePath(engine.homeDir, "test_dir")
        assertTrue(resolvedSub.absolutePath.endsWith("test_dir"))

        engine.bootstrap()
        val bashFile = java.io.File(engine.homeDir, "bin/bash")
        assertTrue(bashFile.exists())
    }
}
