# ArchDroid

ArchDroid is an open-source, terminal-only Android app for running an Arch Linux ARM userspace through PRoot without root.

It is intentionally not a GUI Linux launcher. There is no VNC, X11, Wayland, XFCE, KDE, GNOME, LXQt, LXDE, or desktop environment. The app boots into a Linux terminal experience that feels like logging into an Arch machine:

```text
arch@android ~ $
```

## Features

- **Kotlin & Jetpack Compose** - Modern Android development with Material Design 3
- **Arch Linux ARM rootfs** - Downloadable and auto-installed on first run
- **PRoot isolation** - Run commands safely without root, with Android storage integration
- **Multi-shell support** - Bash by default, Zsh available via pacman
- **Full terminal capabilities** - ANSI colors, interactive input, copy/paste, scrollback buffer, multi-tab sessions
- **Pacman package manager** - Install packages inside Arch, not Android
- **Session persistence** - Save and restore terminal sessions with working directory
- **Minimal footprint** - Optimized for 2GB RAM devices
- **Terminal-first** - Pure CLI experience, no GUI emulation

## Quick Start

### Prerequisites

- Android 8.1+ (API 26+)
- 2GB+ RAM recommended
- ~500MB storage for Arch Linux rootfs

### Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/yourusername/ArchDroid.git
   cd ArchDroid
   ```

2. **Setup native binaries** (proot & busybox)

   ```bash
   chmod +x setup-binaries.sh
   ./setup-binaries.sh
   ```

   Or follow [manual setup](docs/BUILD.md#getting-the-binaries)

3. **Build APK**

   ```bash
   ./gradlew clean assembleDebug
   ```

4. **Install on device**

   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

5. **Launch**
   ```bash
   adb shell am start -n id.archdroid/.MainActivity
   ```

## Usage

### First Run

- App downloads Arch Linux rootfs (~300-500MB depending on mirror)
- Sets up PRoot environment
- Configures shell with aliases and colors
- Boots into terminal

### Basic Commands

```bash
# Update system
pacman -Syu

# Install packages
sudo pacman -S git curl wget

# Security tools (example)
sudo pacman -S nmap aircrack-ng john

# Access Android storage
cd ~/storage/downloads
cd ~/storage/documents
```

### Terminal Features

- **Multi-tab**: New Terminal button to create sessions
- **Copy/Paste**: Long press to select, use system clipboard
- **Scrollback**: Swipe up to see history
- **Font size**: Adjustable in Settings
- **Themes**: Dark mode and color schemes

## Documentation

- **[Architecture](docs/ARCHITECTURE.md)** - Project structure and design patterns
- **[Build Guide](docs/BUILD.md)** - Detailed build instructions and options
- **[Troubleshooting](TROUBLESHOOTING.md)** - Common issues and solutions

## Project Details

### Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material Design 3
- **Dependency Injection**: Hilt
- **Database**: Room + DataStore
- **Concurrency**: Coroutines + Flow
- **Architecture**: MVVM with Clean Architecture layers
- **Terminal**: Termux terminal view library

### Target Users

- Linux learners
- Programmers & developers
- System administrators
- Network engineers
- Cybersecurity enthusiasts

### What's NOT Included

- GUI Linux desktop (XFCE, KDE, GNOME, etc.)
- VNC, X11, Wayland
- Remote access UI
- This is a **terminal app**, not an emulator

## System Requirements

| Component | Requirement                            |
| --------- | -------------------------------------- |
| Android   | 8.1 (API 26) or newer                  |
| RAM       | 2GB minimum, 4GB+ recommended          |
| Storage   | 600MB free (RootFS ~300-500MB)         |
| CPU       | ARM64 (aarch64) or ARM32 (armv7)       |
| Network   | Required for first-run RootFS download |

## Building

### Debug Build

```bash
./gradlew clean assembleDebug
```

### Release Build

```bash
# Setup keystore first (see BUILD.md)
./gradlew clean assembleRelease
```

See [BUILD.md](docs/BUILD.md) for detailed instructions.

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Issues & Troubleshooting

**App crashes on startup?**
→ Check [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for solutions

**Binary not found error?**
→ Run `./setup-binaries.sh` or see [binary setup guide](docs/BUILD.md#getting-the-binaries)

**Performance issues?**
→ See performance optimization section in [BUILD.md](docs/BUILD.md)

## Security Considerations

- **No root access required** - PRoot provides isolation
- **Scoped storage** - Respects Android storage permissions
- **Encrypted preferences** - Sensitive data stored securely
- **Safe PRoot execution** - Filesystem namespace isolation

Limitations without root:

- Some kernel-level features unavailable
- Monitor mode for WiFi not supported on most devices
- Packet injection requires special hardware
- Raw socket access limited

## Performance

Optimized for low-end devices:

| Metric         | Target      |
| -------------- | ----------- |
| APK Size       | < 50MB      |
| Startup Time   | < 3 seconds |
| Idle Memory    | < 150MB     |
| Battery Impact | Minimal     |

## License

This project is licensed under the [MIT License](LICENSE).

## Acknowledgments

- [Termux](https://termux.dev/) - Terminal emulation library
- [PRoot](https://proot-me.github.io/) - Userspace implementation of chroot
- [Arch Linux ARM](https://archlinuxarm.org/) - ARM builds of Arch Linux

## Support

For issues, questions, or feature requests:

- Open an [issue on GitHub](../../issues)
- Check existing [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
- Review [BUILD.md](docs/BUILD.md) for common problems

---

**Made with ❤️ for Linux terminal enthusiasts on Android**
