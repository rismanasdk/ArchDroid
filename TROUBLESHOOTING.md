# ArchDroid Troubleshooting Guide

## Application Crashes with "Preparing"

If the app displays "Preparing" for a moment and then crashes immediately, the problem is likely:

### 1. Missing pRoot and busybox Binaries ❌

**Symptoms:**

- App crashes at splash screen
- Error: "Native asset 'bin/arm64-v8a/proot' not found"

**Cause:**

- Binary files for pRoot and busybox haven't been added to the project
- Only `.gitkeep` placeholder exists in `app/src/main/assets/bin/`

**Solution:**

You need pre-compiled ARM binaries for pRoot and busybox. There are several methods:

#### Option A: Extract from Termux (Recommended)

```bash
# Install Termux from F-Droid or Play Store
# Then in Termux:
pkg install proot busybox

# Check their location:
which proot
which busybox
# Output will be like:
# /data/data/com.termux/files/usr/bin/proot
# /data/data/com.termux/files/usr/bin/busybox

# Pull to your project:
# For arm64-v8a (64-bit):
adb pull /data/data/com.termux/files/usr/bin/proot app/src/main/assets/bin/arm64-v8a/
adb pull /data/data/com.termux/files/usr/bin/busybox app/src/main/assets/bin/arm64-v8a/

# For armeabi-v7a (32-bit), use the same files or compile separately
```

#### Option B: Download Pre-compiled from GitHub

```bash
# arm64-v8a
wget https://github.com/termux/proot-distro/releases/download/v4.2.2/proot-aarch64
chmod +x proot-aarch64
mv proot-aarch64 app/src/main/assets/bin/arm64-v8a/proot

# busybox
wget https://busybox.net/downloads/binaries/1.35.0-x86_64/busybox
chmod +x busybox
# Cross-compile or download pre-built for aarch64
```

#### Option C: Compile from Source

```bash
# Requires Android NDK
# More complex, only for advanced users
```

### 2. Check Logcat for Detailed Errors

To see the actual error:

```bash
# Terminal 1: Run app on emulator/device
adb logcat -c
adb logcat -f archdroid.log "*:V" | grep -E "SplashViewModel|RootFsInstaller|ArchDroid"

# Terminal 2: Launch the app
adb shell am start -n id.archdroid/.MainActivity

# View output in terminal 1
```

### 3. Verify Binaries Are Present

After adding binaries, ensure the folder structure looks like this:

```
app/src/main/assets/bin/
├── arm64-v8a/
│   ├── proot          ← Binary file (not .gitkeep!)
│   ├── busybox        ← Binary file
│   └── .gitkeep       ← Remove if binaries exist
├── armeabi-v7a/
│   ├── proot
│   ├── busybox
│   └── .gitkeep
```

Verify files are executable:

```bash
ls -la app/src/main/assets/bin/arm64-v8a/
# Should show:
# -rwxr-xr-x ... proot
# -rwxr-xr-x ... busybox
```

### 4. Rebuild and Test

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch and view logs
adb logcat -s SplashViewModel
```

## Other Issues

### Storage Permission Denied

- Ensure `WRITE_EXTERNAL_STORAGE` and `READ_EXTERNAL_STORAGE` are in AndroidManifest.xml
- Request permissions at runtime for Android 6.0+

### Arch Linux RootFS Download Timeout

- Internet connection may be slow
- Download size: ~200-500 MB (varies by mirror)
- Default timeout: 30 seconds (can be adjusted in RootFsInstaller.kt)

### PRoot Execution Failed

- Binary might be corrupted
- Download again and verify checksum

## Debugging Tips

1. **Add logging to RootFsInstaller.kt:**

```kotlin
Log.d("RootFsInstaller", "Checking asset: $assetPath")
Log.d("RootFsInstaller", "Downloading from: ${rootFsUrl()}")
```

2. **Test offline mode:**

```kotlin
// In SplashViewModel, add toggle to skip download:
if (BuildConfig.DEBUG) {
    // Skip installation for UI testing
}
```

3. **Monitor resource usage:**

```bash
adb shell dumpsys meminfo id.archdroid
adb shell top -p $(adb shell pidof id.archdroid) -n 1
```

## System Information

If issues persist, check:

- Android version (must be >= 8.1 / API 26)
- Device ABI: `adb shell getprop ro.product.cpu.abilist`
- Available storage: `adb shell df -h`
- Connected device: `adb devices`

## Common Solutions

| Problem           | Solution                                      |
| ----------------- | --------------------------------------------- |
| "Asset not found" | Run `./setup-binaries.sh` or pull from Termux |
| Slow download     | Use WiFi, check internet connection           |
| Permission denied | Update Android permissions in manifest        |
| App won't start   | Check logcat, ensure binaries are executable  |
| Low memory        | Reduce scrollback buffer in settings          |

## Useful Commands

```bash
# View app logs in real-time
adb logcat -s "ArchDroid"

# Check if app is running
adb shell pidof id.archdroid

# Force stop app
adb shell am force-stop id.archdroid

# View app storage
adb shell ls -la /data/data/id.archdroid/

# Monitor app process
adb shell ps -ef | grep archdroid
```

## Getting Help

If you still have issues:

1. Check [BUILD.md](docs/BUILD.md) for build-related problems
2. Check device compatibility (API 26+ required)
3. Open an issue on GitHub with:
   - Device model and Android version
   - Full logcat output
   - Steps to reproduce
