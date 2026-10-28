# Lyon LuxTerminal Release & Rollback Path

## Release Process
1. Run full test suite: `gradle :app:testDebugUnitTest`
2. Assemble debug/release APKs: `gradle assembleDebug`
3. Verify output binaries in root: `./scripts/verify-artifacts.sh`
4. Publish release APK (`Lyon_Terminal.apk`) to download portal / artifact directory.

## Rollback Path
- Retain previous signed APK versions in version control / `.build-outputs`.
- If regression occurs, revert commit via git and rebuild using last known good tag.
