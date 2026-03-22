# Komelia Project Instructions

## Build Instructions (from BUILDING.md)

### Debug APK
Build and sign with the Android debug keystore:
```bash
./gradlew :komelia-app:assembleDebug
```
Output: `komelia-app/build/outputs/apk/debug/komelia-app-debug.apk`

### Release APK
1. **Build**:
   ```bash
   ./gradlew :komelia-app:assembleRelease
   ```
   Output: `komelia-app/build/outputs/apk/release/komelia-app-release-unsigned.apk`

2. **Align and Sign**:
   ```bash
   cd komelia-app/build/outputs/apk/release
   ~/Android/Sdk/build-tools/35.0.0/zipalign -p -f 4 \
     komelia-app-release-unsigned.apk \
     komelia-app-release-aligned.apk

   ~/Android/Sdk/build-tools/35.0.0/apksigner sign \
     --ks ~/.android/debug.keystore \
     --ks-pass pass:android \
     --ks-key-alias androiddebugkey \
     --key-pass pass:android \
     --out komelia-app-release-signed.apk \
     komelia-app-release-aligned.apk
   ```

### Force Package (if cache is stale)
```bash
rm -f komelia-app/build/outputs/apk/release/komelia-app-release-unsigned.apk
./gradlew :komelia-app:packageRelease
```

### Installing via ADB
```bash
adb install komelia-app/build/outputs/apk/release/komelia-app-release-signed.apk
```
If signature mismatch occurs:
```bash
adb uninstall io.github.snd_r.komelia
adb install komelia-app/build/outputs/apk/release/komelia-app-release-signed.apk
```
