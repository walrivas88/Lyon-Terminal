#!/usr/bin/env bash
set -e

echo "[*] Verifying Lyon LuxTerminal artifacts..."
if [ -f "Lyon_Terminal.apk" ]; then
    echo "[+] Lyon_Terminal.apk exists ($(du -h Lyon_Terminal.apk | cut -f1))"
else
    echo "[-] ERROR: Lyon_Terminal.apk not found!"
    exit 1
fi

if [ -f "Android_Terminal.apk" ]; then
    echo "[+] Android_Terminal.apk exists ($(du -h Android_Terminal.apk | cut -f1))"
else
    echo "[-] ERROR: Android_Terminal.apk not found!"
    exit 1
fi

echo "[+] Artifact verification successful!"
