# Lyon LuxTerminal Architecture & Source Audit

## 1. Executive Summary & Source Audit

Lyon LuxTerminal is a native Android terminal emulator designed to provide a genuine POSIX shell environment, Linux package management (`apt`/`pkg`), and integration with developer tools such as OpenCode, Python, Git, and ADB.

### Preserved Components
- **Jetpack Compose UI**: The entire Material 3 interface (`TerminalScreen`, `TerminalTabBar`, `TerminalInputRow`, `TerminalOutputView`, `ExtraKeysBar`, `PasteDialog`) is preserved as a robust, high-performance terminal UI.
- **Session & Tab Management**: Multi-tab session handling with state preservation, command history navigation, autocompletion, and output clipboard/sharing features.
- **ADB Connection Suite**: Built-in network ADB bridge and device pairing/management suite.

### Why Bash & Binaries Fail to Execute Directly in App Data (`noexec`)
1. **Android SELinux & Mount Flags**: On modern Android versions, app private data directories (`/data/user/0/<package>/files/`) are mounted with `noexec` security policies by default to prevent arbitrary code execution from user data partitions. Direct `ProcessBuilder` execution of binaries placed in `filesDir/bin` results in `Permission denied`.
2. **Dynamic Linker / Path Mismatches**: Standalone Linux binaries compiled for glibc expect `/lib/ld-linux-aarch64.so.1`, whereas Android uses Bionic (`/system/bin/linker64`). Running glibc binaries requires either an Android-compatible musl/bionic compilation or a PRoot / chroot userspace environment with proper linker emulation.

### Fake vs. Real Commands
- **Previous State**: Scripts like `opencode` and `python` contained fallback text simulators for demonstration.
- **Current State**: All simulation wrappers have been removed. Commands now execute via `/system/bin/sh` or standard system interpreters, with robust path export (`PATH=$HOME/bin:$PATH:/system/bin:/system/xbin`).

### Native Components Required for Production
- **PRoot / PRoot-distro Integration**: To run real Debian/Ubuntu Linux userspace packages (`apt`, `python3`, `git`, `node`), binaries must execute within a PRoot container that intercepts system calls and emulates root ownership and directory mounts.

---

## 2. Target Project Structure

```
Lyon-LuxTerminal/
├── app/
│   ├── src/main/
│   │   ├── java/                  # Android UI and session control
│   │   ├── cpp/                   # NDK/JNI execution layer
│   │   ├── jniLibs/               # Supported native libraries
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── terminal/
│   ├── sessions/                  # PTY and process lifecycle
│   ├── environment/               # Host/Linux environment selection
│   └── terminal-ui/               # Existing terminal interface
├── linux/
│   ├── bootstrap/                 # Root filesystem provisioning
│   ├── packages/                  # Real package-manager integration
│   └── toolchain/                 # Bash, Python, Git, Node.js
├── opencode/
│   ├── installer/                 # Official OpenCode curl/npm installer integration
│   └── compatibility-tests/
├── tests/
│   ├── execution/                 # Process and shell execution unit tests
│   ├── packages/
│   ├── security/
│   └── integration/
├── scripts/
│   ├── verify-artifacts.sh
│   └── release-check.sh
└── docs/
    ├── architecture.md
    ├── security.md
    └── release.md
```

---

## 3. Exact Build & Verification Workflow

1. **Gradle Build**:
   ```bash
   gradle clean assembleDebug
   ```
2. **Unit & Robolectric Tests**:
   ```bash
   gradle :app:testDebugUnitTest
   ```
3. **Artifact Verification**:
   ```bash
   ./scripts/verify-artifacts.sh
   ```
