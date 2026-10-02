# TianXueVersionSpoof

Minimal legacy LSPosed module for `com.up366.mobile`.

It changes only the `PackageInfo.versionCode` value returned inside the TianXue process for TianXue itself:

- Real installed versionCode: unchanged
- Fake versionCode visible to TianXue: `2100000000`
- Target package: `com.up366.mobile`

## Build with GitHub Actions

The included `Build APK` workflow builds `app-debug.apk` and uploads it as an Actions artifact.

## Install / LSPosed

1. Install the built APK.
2. LSPosed -> Modules -> enable `天学网版本伪装`.
3. Scope: select only `天学网学生 (com.up366.mobile)`.
4. Force-stop TianXue, then start it again.
5. Check LSPosed logs for:
   - `[TianXueVersionSpoof] loaded ...`
   - `[TianXueVersionSpoof] spoofed versionCode 213 -> 2100000000`

## Undo

Disable the module in LSPosed and force-stop/restart TianXue, or uninstall this module.
