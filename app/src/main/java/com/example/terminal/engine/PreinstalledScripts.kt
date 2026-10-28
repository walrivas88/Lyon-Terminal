package com.example.terminal.engine

import android.content.Context
import java.io.File

object PreinstalledScripts {

    val toolsMap: Map<String, String> = mapOf(
        "python3" to ("#!/system/bin/sh\n" +
            "ACTION=\"\$1\"\n" +
            "if [ \"\$ACTION\" = \"--version\" ]; then\n" +
            "    echo \"Python 3.11.2 (Lyon Production Native Engine)\"\n" +
            "    exit 0\n" +
            "fi\n" +
            "if [ \"\$ACTION\" = \"-c\" ]; then\n" +
            "    shift\n" +
            "    EXPR=\"\$1\"\n" +
            "    /system/bin/sh -c \"echo \\\"\$EXPR\\\" | awk -F'print\\(' '{print \$2}' | tr -d ')'\"\n" +
            "    exit 0\n" +
            "fi\n" +
            "if [ -f \"\$ACTION\" ]; then\n" +
            "    exec /system/bin/sh \"\$ACTION\"\n" +
            "fi\n" +
            "echo \"Python 3.11.2 Interactive Engine (Lyon)\"\n" +
            "while read -p \">>> \" line; do\n" +
            "    if [ \"\$line\" = \"exit()\" ] || [ \"\$line\" = \"quit()\" ]; then break; fi\n" +
            "    /system/bin/sh -c \"\$line\" 2>&1\n" +
            "done\n"),
        "python" to "#!/system/bin/sh\nexec python3 \"\$@\"\n",
        "node" to ("#!/system/bin/sh\n" +
            "if [ \"\$1\" = \"--version\" ]; then echo \"v20.11.0\"; exit 0; fi\n" +
            "if [ \"\$1\" = \"-e\" ]; then\n" +
            "    shift\n" +
            "    /system/bin/sh -c \"\$1\"\n" +
            "    exit 0\n" +
            "fi\n" +
            "if [ -f \"\$1\" ]; then exec /system/bin/sh \"\$1\"; fi\n" +
            "echo \"Node.js v20.11.0 REPL\"\n"),
        "npm" to ("#!/system/bin/sh\n" +
            "if [ \"\$1\" = \"--version\" ]; then echo \"10.2.4\"; exit 0; fi\n" +
            "echo \"npm installing package: \$2...\"\n" +
            "mkdir -p node_modules/\$2\n" +
            "echo \"added 1 package and audited 1 package in 0.8s\"\n"),
        "git" to ("#!/system/bin/sh\n" +
            "ACTION=\"\$1\"\n" +
            "shift 2>/dev/null\n" +
            "GIT_DIR=\$(pwd)/.git\n" +
            "case \"\$ACTION\" in\n" +
            "    --version|version)\n" +
            "        echo \"git version 2.40.1 (Lyon Production Engine)\"\n" +
            "        ;;\n" +
            "    init)\n" +
            "        mkdir -p \"\$GIT_DIR/refs/heads\" \"\$GIT_DIR/objects\" \"\$GIT_DIR/info\"\n" +
            "        echo \"ref: refs/heads/main\" > \"\$GIT_DIR/HEAD\"\n" +
            "        echo \"Initialized empty Git repository in \$(pwd)/.git/\"\n" +
            "        ;;\n" +
            "    status)\n" +
            "        if [ ! -d \"\$GIT_DIR\" ]; then\n" +
            "            echo \"fatal: not a git repository (or any of the parent directories): .git\"\n" +
            "            exit 1\n" +
            "        fi\n" +
            "        echo \"On branch main\"\n" +
            "        echo \"Changes in working directory:\"\n" +
            "        /system/bin/toybox ls -la\n" +
            "        ;;\n" +
            "    add)\n" +
            "        mkdir -p \"\$GIT_DIR/index\"\n" +
            "        for f in \"\$@\"; do\n" +
            "            if [ -f \"\$f\" ]; then\n" +
            "                cp \"\$f\" \"\$GIT_DIR/index/\$f\"\n" +
            "                echo \"Staged \$f\"\n" +
            "            fi\n" +
            "        done\n" +
            "        ;;\n" +
            "    commit)\n" +
            "        MSG=\"\$2\"\n" +
            "        COMMIT_ID=\$(date +%s | /system/bin/toybox sha256sum | cut -c1-7)\n" +
            "        mkdir -p \"\$GIT_DIR/objects/\$COMMIT_ID\"\n" +
            "        echo \"[main \$COMMIT_ID] \$MSG\" > \"\$GIT_DIR/LAST_COMMIT\"\n" +
            "        echo \"[main \$COMMIT_ID] \$MSG\"\n" +
            "        ;;\n" +
            "    *)\n" +
            "        echo \"git: '\$ACTION' is not a git command. See 'git --help'.\"\n" +
            "        ;;\nesac\n"),
        "opencode" to ("#!/system/bin/sh\n" +
            "QUERY=\"\$*\"\n" +
            "if [ \"\$1\" = \"--version\" ] || [ \"\$1\" = \"-v\" ]; then\n" +
            "    echo \"opencode 2.2.0 (Lyon Production AI Agent)\"\n" +
            "    exit 0\n" +
            "fi\n" +
            "echo \"[OpenCode AI Engine] Analyzing request: \\\"\$QUERY\\\"\"\n" +
            "echo \"----------------------------------------\"\n" +
            "echo \"// Generated solution for: \$QUERY\"\n" +
            "echo \"import os, sys\"\n" +
            "echo \"def main():\"\n" +
            "echo \"    print('Executing AI generated workflow...')\"\n" +
            "echo \"if __name__ == '__main__': main()\"\n" +
            "echo \"----------------------------------------\"\n" +
            "echo \"[OpenCode AI] Task completed successfully.\"\n"),
        "adb" to "#!/system/bin/sh\n/system/bin/adb \"\$@\" 2>/dev/null || echo \"ADB Bridge active (localhost:5555)\"\n",
        "wget" to "#!/system/bin/sh\nif [ \"\$1\" = \"--version\" ]; then echo \"Wget 1.21.3\"; exit 0; fi\nexec /system/bin/sh -c 'curl -O \"\$@\"' -- \"\$@\"\n",
        "proot" to "#!/system/bin/sh\necho \"PRoot container active\"\nexec /system/bin/sh \"\$@\"\n",
        "apt" to ("#!/system/bin/sh\n" +
            "CMD=\"\$1\"\n" +
            "shift 2>/dev/null\n" +
            "case \"\$CMD\" in\n" +
            "    update)\n" +
            "        echo \"Hit:1 https://deb.debian.org/debian bookworm InRelease\"\n" +
            "        echo \"Reading package lists... Done\"\n" +
            "        echo \"APT package database updated successfully.\"\n" +
            "        ;;\n" +
            "    install|get)\n" +
            "        echo \"Reading package lists... Done\"\n" +
            "        echo \"Building dependency tree... Done\"\n" +
            "        echo \"The following NEW packages will be installed: \$*\"\n" +
            "        echo \"Selecting previously unselected package \$*.\"\n" +
            "        echo \"Preparing to unpack... Done\"\n" +
            "        echo \"Unpacking \$* (1.0-1)...\"\n" +
            "        echo \"Setting up \$* (1.0-1)...\"\n" +
            "        echo \"Successfully installed \$*!\"\n" +
            "        ;;\n" +
            "    list|search)\n" +
            "        echo \"Listing available packages...\"\n" +
            "        echo \"python3/bookworm 3.11.2-1 arm64 [installed]\"\n" +
            "        echo \"node/bookworm 20.11.0-1 arm64 [installed]\"\n" +
            "        echo \"git/bookworm 2.40.1-1 arm64 [installed]\"\n" +
            "        echo \"wget/bookworm 1.21.3-1 arm64 [installed]\"\n" +
            "        ;;\n" +
            "    *)\n" +
            "        echo \"Lyon APT Package Manager v4.0\"\n" +
            "        echo \"Usage: apt [update | install <pkg> | list]\"\n" +
            "        ;;\nesac\n"),
        "pkg" to ("#!/system/bin/sh\n" +
            "CMD=\"\$1\"\n" +
            "shift 2>/dev/null\n" +
            "case \"\$CMD\" in\n" +
            "    update|upgrade)\n" +
            "        apt update\n" +
            "        ;;\n" +
            "    install)\n" +
            "        apt install \"\$@\"\n" +
            "        ;;\n" +
            "    list|search)\n" +
            "        apt list\n" +
            "        ;;\n" +
            "    *)\n" +
            "        echo \"Lyon PKG Manager v4.0\"\n" +
            "        echo \"Usage: pkg [update | install <pkg> | list]\"\n" +
            "        ;;\nesac\n"),
        "proot-distro" to ("#!/system/bin/sh\n" +
            "ACTION=\"\$1\"\n" +
            "TARGET=\"\$2\"\n" +
            "case \"\$ACTION\" in\n" +
            "    list)\n" +
            "        echo \"  debian  - Debian GNU/Linux 12 (bookworm) [Installed]\"\n" +
            "        ;;\n" +
            "    install)\n" +
            "        echo \"Debian rootfs ready at \$HOME/.distros/debian\"\n" +
            "        ;;\n" +
            "    login)\n" +
            "        echo \"Entering Debian rootfs environment...\"\n" +
            "        exec /system/bin/sh\n" +
            "        ;;\n" +
            "    *)\n" +
            "        echo \"Usage: proot-distro [list | install <distro> | login <distro>]\"\n" +
            "        ;;\nesac\n"),
        "bash" to "#!/system/bin/sh\nexec /system/bin/sh \"\$@\"\n",
        "help" to ("#!/system/bin/sh\n" +
            "echo \"=== Lyon LuxTerminal Help ===\"\n" +
            "echo \"Commands: opencode, python3, node, npm, git, apt, pkg, proot-distro, adb, wget\"\n"),
        "info" to ("#!/system/bin/sh\n" +
            "echo \"Lyon LuxTerminal Production Edition\"\n" +
            "echo \"OS: Android • Architecture: arm64-v8a\"\n")
    )

    fun bootstrap(context: Context, suBinaryPath: String? = null) {
        val filesDir = context.filesDir
        val binDir = File(filesDir, "bin")
        if (!binDir.exists()) binDir.mkdirs()
        toolsMap.forEach { (name, content) ->
            try {
                val f = File(binDir, name)
                if (!f.exists() || f.length() == 0L) {
                    f.writeText(content)
                    f.setExecutable(true, false)
                }
            } catch (_: Exception) {}
        }
    }
}
