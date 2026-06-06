#!/bin/bash
# Setup script for ArchDroid - Download proot and busybox binaries

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"
ASSETS_BIN_DIR="$PROJECT_DIR/app/src/main/assets/bin"

echo "================================"
echo "ArchDroid Binary Setup Script"
echo "================================"
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check if ADB is available
if ! command -v adb &> /dev/null; then
    echo -e "${RED}Error: adb not found in PATH${NC}"
    echo "Please install Android SDK Platform Tools"
    exit 1
fi

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo -e "${RED}Error: No Android device connected${NC}"
    echo "Connect your device via USB and enable USB Debugging"
    exit 1
fi

# Get device ABI
DEVICE_ABI=$(adb shell getprop ro.product.cpu.abi)
echo -e "${GREEN}Device ABI: $DEVICE_ABI${NC}"

# Map ABI to directory
case "$DEVICE_ABI" in
    "arm64-v8a")
        ASSETS_ABI_DIR="$ASSETS_BIN_DIR/arm64-v8a"
        ;;
    "armeabi-v7a")
        ASSETS_ABI_DIR="$ASSETS_BIN_DIR/armeabi-v7a"
        ;;
    *)
        echo -e "${RED}Error: Unsupported ABI: $DEVICE_ABI${NC}"
        exit 1
        ;;
esac

echo ""
echo "Creating directories..."
mkdir -p "$ASSETS_ABI_DIR"

echo ""
echo "Pulling binaries from Termux..."
echo "Make sure Termux is installed with proot & busybox:"
echo "  pkg install proot busybox"
echo ""
echo ""

# Pull proot
echo -n "Pulling proot... "
if adb pull /data/data/com.termux/files/usr/bin/proot "$ASSETS_ABI_DIR/proot" 2>/dev/null; then
    chmod +x "$ASSETS_ABI_DIR/proot"
    echo -e "${GREEN}OK${NC}"
else
    echo -e "${RED}FAILED${NC}"
    echo "Termux proot not found. Install with: pkg install proot"
    exit 1
fi

# Pull busybox
echo -n "Pulling busybox... "
if adb pull /data/data/com.termux/files/usr/bin/busybox "$ASSETS_ABI_DIR/busybox" 2>/dev/null; then
    chmod +x "$ASSETS_ABI_DIR/busybox"
    echo -e "${GREEN}OK${NC}"
else
    echo -e "${RED}FAILED${NC}"
    echo "Termux busybox not found. Install with: pkg install busybox"
    exit 1
fi

# Remove .gitkeep if it exists
if [ -f "$ASSETS_ABI_DIR/.gitkeep" ]; then
    rm "$ASSETS_ABI_DIR/.gitkeep"
fi

echo ""
echo "Verifying binaries..."
if [ -x "$ASSETS_ABI_DIR/proot" ] && [ -x "$ASSETS_ABI_DIR/busybox" ]; then
    echo -e "${GREEN}✓ proot: $(ls -lh $ASSETS_ABI_DIR/proot | awk '{print $5}')${NC}"
    echo -e "${GREEN}✓ busybox: $(ls -lh $ASSETS_ABI_DIR/busybox | awk '{print $5}')${NC}"
else
    echo -e "${RED}Error: Binary files are not executable${NC}"
    exit 1
fi

echo ""
echo "Binaries location:"
echo "  $ASSETS_ABI_DIR/"
echo ""
echo -e "${GREEN}Setup completed successfully!${NC}"
echo ""
echo "Next steps:"
echo "  1. Rebuild APK: ./gradlew clean assembleDebug"
echo "  2. Install APK: adb install app/build/outputs/apk/debug/app-debug.apk"
echo "  3. Run: adb shell am start -n id.archdroid/.MainActivity"
