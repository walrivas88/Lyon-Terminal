#!/usr/bin/env bash
set -e

echo "[*] Running release check for Lyon LuxTerminal..."
gradle :app:testDebugUnitTest
gradle assembleDebug
./scripts/verify-artifacts.sh
echo "[+] Release check completed successfully!"
