# ArchDroid Architecture

ArchDroid is a terminal-first Android app that boots a real Arch Linux ARM root filesystem with PRoot, without root access.

## Layers

- `ui`: Jetpack Compose screens and terminal host.
- `domain`: session and package contracts.
- `data`: Room entities, DAOs, repository implementations.
- `core/rootfs`: Arch Linux rootfs download, extraction, first-run configuration.
- `core/proot`: safe PRoot command assembly and Android storage mounts.
- `core/terminal`: terminal session adapter using Termux terminal libraries and a small native PTY bridge.

## Database

`terminal_sessions`

- `id`: stable session id.
- `title`: tab title.
- `cwd`: last working directory.
- `shell`: `/usr/bin/bash` by default.
- `isActive`: session visibility state.
- `createdAt`, `updatedAt`.
- `scrollbackSnapshot`: bounded transcript snapshot.

`package_actions`

- records recent pacman actions: update, install, remove, search.

## First Run Flow

1. Splash starts `RootFsInstaller`.
2. PRoot and BusyBox executables are copied from `assets/bin/<abi>/`.
3. Arch Linux ARM rootfs is downloaded for `arm64-v8a` or `armeabi-v7a`.
4. BusyBox extracts the rootfs into app-private storage.
5. `/etc/hostname`, `/etc/resolv.conf`, `/home/arch/.bashrc`, prompt, aliases, and storage directories are created.
6. App navigates to Home.
7. New Terminal starts Bash through PRoot.

## PRoot Mounts

- `/dev`, `/proc`, `/sys`
- `/sdcard`
- `/storage/emulated/0/Download` to `~/storage/downloads`
- `/storage/emulated/0/Documents` to `~/storage/documents`
- `/storage/emulated/0/Pictures` to `~/storage/pictures`
- `/storage/emulated/0/Music` to `~/storage/music`

## Performance Targets

- Keep Android UI process idle under 150 MB by avoiding WebView and desktop Linux components.
- Download rootfs after install instead of bundling it into APK.
- Use one foreground terminal session by default; background tabs are persisted and restarted on demand.
- Cap persisted scrollback snapshots while keeping full interactive scrollback inside terminal memory.
