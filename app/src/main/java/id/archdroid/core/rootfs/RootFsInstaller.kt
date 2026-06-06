package id.archdroid.core.rootfs

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL

class RootFsInstaller(private val context: Context) {
    val installDir: File = File(context.filesDir, "arch")
    val rootFsDir: File = File(installDir, "rootfs")
    val binDir: File = File(installDir, "bin")
    private val marker = File(rootFsDir, ".archdroid-ready")

    fun isInstalled(): Boolean = marker.exists()

    fun install(): Flow<InstallProgress> = flow {
        if (isInstalled()) {
            emit(InstallProgress.Done)
            return@flow
        }
        installDir.mkdirs()
        rootFsDir.mkdirs()
        binDir.mkdirs()

        emit(InstallProgress.Step("Preparing PRoot"))
        val abi = supportedLinuxAbi()
        prepareExecutable(abi, "proot", File(binDir, "proot"))
        prepareExecutable(abi, "busybox", File(binDir, "busybox"))

        emit(InstallProgress.Step("Downloading Arch Linux rootfs"))
        val archive = File(context.cacheDir, "arch-rootfs.tar.gz")
        download(rootFsUrl(), archive) { read, total ->
            emit(InstallProgress.Download(read, total))
        }

        emit(InstallProgress.Step("Extracting rootfs"))
        extractWithBusybox(archive)

        emit(InstallProgress.Step("Configuring shell"))
        writeBaseConfig()
        marker.writeText("ok\n")
        emit(InstallProgress.Done)
    }.flowOn(Dispatchers.IO)

    private fun supportedLinuxAbi(): String = when {
        Build.SUPPORTED_ABIS.any { it == "arm64-v8a" } -> "arm64-v8a"
        Build.SUPPORTED_ABIS.any { it == "armeabi-v7a" } -> "armeabi-v7a"
        else -> error("Unsupported Android ABI: ${Build.SUPPORTED_ABIS.joinToString()}")
    }

    private fun rootFsUrl(): String = when (supportedLinuxAbi()) {
        "arm64-v8a" ->
            "https://os.archlinuxarm.org/os/ArchLinuxARM-aarch64-latest.tar.gz"
        "armeabi-v7a" ->
            "https://os.archlinuxarm.org/os/ArchLinuxARM-armv7-latest.tar.gz"
        else -> error("Unsupported Android ABI: ${Build.SUPPORTED_ABIS.joinToString()}")
    }

    private fun prepareExecutable(abi: String, assetName: String, target: File) {
        if (target.exists()) return
        val assetPath = "bin/$abi/$assetName"
        val inputStream = try {
            context.assets.open(assetPath)
        } catch (e: FileNotFoundException) {
            throw IllegalStateException(
                "Native asset '$assetPath' belum ada. Tambahkan binary $assetName untuk ABI $abi ke app/src/main/assets/$assetPath.",
                e
            )
        }
        inputStream.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        target.setExecutable(true, true)
    }

    private suspend fun download(url: String, target: File, progress: suspend (Long, Long) -> Unit) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000
        val total = connection.contentLengthLong
        connection.inputStream.use { input ->
            target.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var readTotal = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count <= 0) break
                    output.write(buffer, 0, count)
                    readTotal += count
                    progress(readTotal, total)
                }
            }
        }
    }

    private fun extractWithBusybox(archive: File) {
        val busybox = File(binDir, "busybox").absolutePath
        val process = ProcessBuilder(busybox, "tar", "-xzf", archive.absolutePath, "-C", rootFsDir.absolutePath)
            .redirectErrorStream(true)
            .start()
        check(process.waitFor() == 0) { process.inputStream.bufferedReader().readText() }
    }

    private fun writeBaseConfig() {
        val home = File(rootFsDir, "home/arch").apply { mkdirs() }
        listOf("downloads", "documents", "pictures", "music").forEach {
            File(rootFsDir, "home/arch/storage/$it").mkdirs()
        }
        File(rootFsDir, "etc/hostname").writeText("android\n")
        File(rootFsDir, "etc/resolv.conf").writeText("nameserver 1.1.1.1\nnameserver 8.8.8.8\n")
        File(rootFsDir, "usr/bin/sudo").apply {
            if (!exists()) {
                writeText("#!/bin/sh\nexec \"\$@\"\n")
                setExecutable(true, false)
            }
        }
        File(home, ".bashrc").writeText(
            """
            alias ll='ls -lah'
            alias la='ls -A'
            alias l='ls -CF'
            alias cls='clear'
            alias update='sudo pacman -Syu'
            export TERM=xterm-256color
            export LANG=C.UTF-8
            PS1='\[\e[1;32m\]\u@android\[\e[0m\] \[\e[1;34m\]\w\[\e[0m\] $ '
            cd "${'$'}HOME"
            """.trimIndent() + "\n"
        )
    }
}

sealed interface InstallProgress {
    data class Step(val message: String) : InstallProgress
    data class Download(val readBytes: Long, val totalBytes: Long) : InstallProgress
    data class Error(val message: String) : InstallProgress
    data object Done : InstallProgress
}
