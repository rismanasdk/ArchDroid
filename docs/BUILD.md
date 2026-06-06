# Build Instructions

## Requirements

- Android Studio Ladybug or newer.
- JDK 17.
- Android SDK 35.
- Android NDK 27 or compatible CMake toolchain.

## Native Assets Setup

### Before Building

You **must** have executable binary files in place:

```text
app/src/main/assets/bin/arm64-v8a/proot
app/src/main/assets/bin/arm64-v8a/busybox
app/src/main/assets/bin/armeabi-v7a/proot
app/src/main/assets/bin/armeabi-v7a/busybox
```

These must be statically linked or compatible with Android 8.1+.

### Getting the Binaries

#### Option 1: From Termux (Recommended & Easiest)

**Step 1:** Install Termux

- Download from [F-Droid](https://f-droid.org/packages/com.termux/) or Play Store

**Step 2:** Install required packages in Termux

```bash
pkg update
pkg install proot busybox
```

**Step 3:** Pull binaries to your project

```bash
# Connect your device via USB with USB Debugging enabled
# From your development machine:

# For 64-bit ARM (most common)
adb pull /data/data/com.termux/files/usr/bin/proot app/src/main/assets/bin/arm64-v8a/
adb pull /data/data/com.termux/files/usr/bin/busybox app/src/main/assets/bin/arm64-v8a/

# For 32-bit ARM (if needed)
# Note: You may need to install 32-bit Termux or use pre-compiled binaries
adb pull /data/data/com.termux/files/usr/bin/proot app/src/main/assets/bin/armeabi-v7a/
adb pull /data/data/com.termux/files/usr/bin/busybox app/src/main/assets/bin/armeabi-v7a/
```

**Step 4:** Make executable

```bash
chmod +x app/src/main/assets/bin/arm64-v8a/proot
chmod +x app/src/main/assets/bin/arm64-v8a/busybox
chmod +x app/src/main/assets/bin/armeabi-v7a/proot
chmod +x app/src/main/assets/bin/armeabi-v7a/busybox
```

#### Option 2: Using Setup Script

We provide a helper script:

```bash
# Make executable
chmod +x setup-binaries.sh

# Connect your device with USB Debugging
# Then run:
./setup-binaries.sh
```

This script will:

- Detect your device ABI
- Pull binaries from Termux automatically
- Verify and set correct permissions
- Clean up `.gitkeep` placeholders

#### Option 3: Pre-compiled Binaries from GitHub Releases

Check releases in proot-distro or static builds (advanced).

### Verify Binaries

```bash
# Check files exist and are executable
ls -lah app/src/main/assets/bin/arm64-v8a/
ls -lah app/src/main/assets/bin/armeabi-v7a/

# Output should look like:
# -rwxr-xr-x  user  group  5.2M  proot
# -rwxr-xr-x  user  group  2.1M  busybox
```

## Build Debug APK

```bash
# Using gradle wrapper (recommended)
./gradlew clean assembleDebug

# Or using gradle command
gradle clean assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Build Release APK

### Create Keystore (first time only)

```bash
keytool -genkey -v -keystore app.keystore -keyalg RSA -keysize 4096 -validity 10000

# Follow prompts and remember the passwords!
# Save keystore.properties with:
# STORE_FILE=app.keystore
# STORE_PASSWORD=<your_store_password>
# KEY_ALIAS=archdroid
# KEY_PASSWORD=<your_key_password>
```

### Build Release APK

```bash
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

## Installation

### Install Debug APK

```bash
# Connect device via USB
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n id.archdroid/.MainActivity
```

### View Logs

```bash
# Real-time logs
adb logcat -s "ArchDroid|SplashViewModel|RootFsInstaller"

# Or save to file
adb logcat > archdroid.log &
```

## Release Workflow

The GitHub Actions workflow (`.github/workflows/release.yml`) builds release artifacts on tags named `v*`.

```bash
git tag v0.1.0
git push origin v0.1.0
```

## Troubleshooting

- App crashes on startup? → See [TROUBLESHOOTING.md](../TROUBLESHOOTING.md)
- Binary not found error? → Follow "Getting the Binaries" section above
- Build fails with CMake error? → Ensure Android NDK 27+ is installed in Android Studio
