package com.example.terminal.model

object SnippetsRepository {

    val snippets: List<CommandSnippet> = listOf(
        // OpenCode AI
        CommandSnippet(
            category = "OpenCode AI",
            name = "Launch OpenCode (opencode)",
            command = "opencode",
            description = "Start interactive AI coding agent session"
        ),
        CommandSnippet(
            category = "OpenCode AI",
            name = "OpenCode Help (opencode --help)",
            command = "opencode --help",
            description = "Display OpenCode command reference and models"
        ),
        CommandSnippet(
            category = "OpenCode AI",
            name = "OpenCode Version (opencode -v)",
            command = "opencode -v",
            description = "Check OpenCode installation version"
        ),

        // Package Manager (pkg & apt)
        CommandSnippet(
            category = "Package Manager",
            name = "List Packages (pkg list)",
            command = "pkg list",
            description = "Show all installed and available packages"
        ),
        CommandSnippet(
            category = "Package Manager",
            name = "Update Indexes (pkg update)",
            command = "pkg update",
            description = "Update package repository lists"
        ),
        CommandSnippet(
            category = "Package Manager",
            name = "PRoot Distros (proot-distro list)",
            command = "proot-distro list",
            description = "List available Linux distributions (Debian, Ubuntu, Kali)"
        ),

        // ADB & Android-x86
        CommandSnippet(
            category = "ADB & Android-x86",
            name = "List ADB Devices (adb devices)",
            command = "adb devices",
            description = "List all attached Android devices and Android-x86 instances"
        ),
        CommandSnippet(
            category = "ADB & Android-x86",
            name = "Connect Local Android-x86 (127.0.0.1:5555)",
            command = "adb connect 127.0.0.1:5555",
            description = "Connect to Android-x86 or Bliss OS ADB daemon on localhost"
        ),
        CommandSnippet(
            category = "ADB & Android-x86",
            name = "Launch ADB Shell (adb shell)",
            command = "adb shell",
            description = "Enter remote or local ADB shell session"
        ),
        CommandSnippet(
            category = "ADB & Android-x86",
            name = "Enable Network ADB (adb tcpip 5555)",
            command = "adb tcpip 5555",
            description = "Restart adbd in TCP mode listening on port 5555"
        ),
        CommandSnippet(
            category = "ADB & Android-x86",
            name = "Stream System Logcat",
            command = "adb logcat -v time",
            description = "Stream live system logs with timestamps"
        ),
        CommandSnippet(
            category = "ADB & Android-x86",
            name = "Disconnect All ADB Devices",
            command = "adb disconnect",
            description = "Disconnect all TCP/IP ADB connections"
        ),

        // Superuser & Elevation
        CommandSnippet(
            category = "Superuser & Root",
            name = "Superuser Shell (su)",
            command = "su",
            description = "Switch to elevated root shell on rooted devices or Bliss OS"
        ),
        CommandSnippet(
            category = "Superuser & Root",
            name = "Execute as Root (sudo whoami)",
            command = "sudo whoami",
            description = "Execute command with elevated superuser privileges via su"
        ),
        CommandSnippet(
            category = "Superuser & Root",
            name = "Root User ID (sudo id)",
            command = "sudo id",
            description = "Check UID and GID under superuser elevation"
        ),
        CommandSnippet(
            category = "Superuser & Root",
            name = "Kernel Dmesg Log (sudo dmesg)",
            command = "sudo dmesg | tail -n 25",
            description = "View kernel debug messages and hardware logs"
        ),

        // Built-ins
        CommandSnippet(
            category = "Built-in Utilities",
            name = "System Info (lux-info)",
            command = "lux-info",
            description = "Device banner, kernel version, storage, and user status"
        ),
        CommandSnippet(
            category = "Built-in Utilities",
            name = "Help Guide (lux-help)",
            command = "lux-help",
            description = "Command reference guide and terminal tips"
        ),
        CommandSnippet(
            category = "Built-in Utilities",
            name = "Matrix Rain (lux-matrix)",
            command = "lux-matrix",
            description = "Animated terminal digital matrix falling characters"
        ),

        // Filesystem
        CommandSnippet(
            category = "File System",
            name = "List Files (Detailed)",
            command = "ls -lah",
            description = "List all files including hidden ones with sizes and permissions"
        ),
        CommandSnippet(
            category = "File System",
            name = "Disk Free (Storage)",
            command = "df -h",
            description = "Show available and used filesystem disk space"
        ),
        CommandSnippet(
            category = "File System",
            name = "Directory Sizes",
            command = "du -sh *",
            description = "Display summary size of each item in current directory"
        ),
        CommandSnippet(
            category = "File System",
            name = "Current Path",
            command = "pwd",
            description = "Print current working directory"
        ),
        CommandSnippet(
            category = "File System",
            name = "Find Files",
            command = "find . -maxdepth 2",
            description = "Locate files up to 2 directories deep"
        ),

        // System & Hardware
        CommandSnippet(
            category = "System & Hardware",
            name = "Kernel & Arch",
            command = "uname -a",
            description = "Print Linux kernel release, hostname, and CPU architecture"
        ),
        CommandSnippet(
            category = "System & Hardware",
            name = "System Uptime",
            command = "uptime",
            description = "Show how long the system has been running"
        ),
        CommandSnippet(
            category = "System & Hardware",
            name = "CPU Architecture ABI",
            command = "getprop ro.product.cpu.abi",
            description = "Show exact CPU ABI (e.g. x86_64 or arm64-v8a)"
        ),
        CommandSnippet(
            category = "System & Hardware",
            name = "CPU Hardware Info",
            command = "cat /proc/cpuinfo | head -n 25",
            description = "Inspect CPU model, cores, and processor flags"
        ),
        CommandSnippet(
            category = "System & Hardware",
            name = "Memory / RAM Status",
            command = "cat /proc/meminfo | head -n 12",
            description = "Show total, free, and available system RAM"
        ),
        CommandSnippet(
            category = "System & Hardware",
            name = "Android Product Props",
            command = "getprop | grep -i product",
            description = "Query Android model, manufacturer, and board properties"
        ),

        // Processes & Identity
        CommandSnippet(
            category = "Processes",
            name = "List Processes",
            command = "ps -A",
            description = "Snapshot of all active processes and PIDs"
        ),
        CommandSnippet(
            category = "Processes",
            name = "Top CPU Processes",
            command = "top -n 1 -b -m 10",
            description = "Show top 10 processes consuming CPU and memory"
        ),
        CommandSnippet(
            category = "Processes",
            name = "User Identity & Groups",
            command = "id",
            description = "Print current user ID, group ID, and security contexts"
        ),
        CommandSnippet(
            category = "Processes",
            name = "Environment Variables",
            command = "env",
            description = "List all active POSIX shell environment variables"
        ),

        // Network
        CommandSnippet(
            category = "Network",
            name = "Network Interfaces",
            command = "ip a",
            description = "Display IP addresses and network adapter statuses"
        ),
        CommandSnippet(
            category = "Network",
            name = "Active Sockets",
            command = "netstat -tlpn 2>/dev/null || netstat",
            description = "List active TCP/UDP network connections and ports"
        ),
        CommandSnippet(
            category = "Network",
            name = "Ping Gateway",
            command = "ping -c 3 8.8.8.8",
            description = "Send 3 ICMP echo requests to Google DNS to test connectivity"
        )
    )

    val categories: List<String> = listOf("All") + snippets.map { it.category }.distinct()
}
