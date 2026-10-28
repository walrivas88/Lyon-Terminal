# Lyon LuxTerminal Security Guidelines

1. **Zero-Trust App Sandbox**: Lyon LuxTerminal operates within the Android app sandbox (`com.lyon.luxterminal`). Root access (`su`) is optional and requested only if superuser binary is present on rooted devices.
2. **Permissions**: Minimum necessary permissions (`INTERNET`, `ACCESS_NETWORK_STATE`, `VIBRATE`) are declared.
3. **No Hardcoded Secrets**: API keys and credentials are handled via environment variables and BuildConfig.
4. **Package Integrity**: All installation packages and APKs are signed with standard release/debug signing configurations.
