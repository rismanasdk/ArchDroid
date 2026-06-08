package id.archdroid.core.rootfs

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.security.cert.X509Certificate
import java.util.zip.GZIPInputStream
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val TAG = "RootFsInstaller"

class RootFsInstaller(private val context: Context) {
    val installDir: File = File(context.filesDir, "arch")
    val rootFsDir: File = File(installDir, "rootfs")
    val binDir: File = File(installDir, "bin")
    private val marker = File(rootFsDir, ".archdroid-ready")

    fun isInstalled(): Boolean = marker.exists()

    fun install(): Flow<InstallProgress> = flow {
        System.err.println("[ARCHDROID] Install starting...")
        Log.d(TAG, "=== Installation flow starting ===")
        
        if (isInstalled()) {
            Log.d(TAG, "Already installed, returning Done")
            System.err.println("[ARCHDROID] Already installed")
            emit(InstallProgress.Done)
            return@flow
        }
        
        Log.d(TAG, "Not installed, proceeding with installation")
        installDir.mkdirs()
        rootFsDir.mkdirs()
        binDir.mkdirs()

        emit(InstallProgress.Step("Preparing PRoot"))
        val abi = supportedLinuxAbi()
        Log.d(TAG, "Supported ABI: $abi")
        prepareExecutable(abi, "proot", File(binDir, "proot"))
        // Note: busybox is not needed - we use system tar for extraction

        Log.d(TAG, "Starting archive location detection")
        var archive = File(context.cacheDir, "arch-rootfs.tar.gz")
        
        // For offline testing, check if archive exists in /data/local/tmp first (world-readable location)
        val tmpArchive = File("/data/local/tmp/arch-rootfs.tar.gz")
        Log.d(TAG, "Checking /data/local/tmp: exists=${tmpArchive.exists()}, size=${tmpArchive.length()}")
        System.err.println("[ARCHDROID] Checking /data/local/tmp: exists=${tmpArchive.exists()}, size=${tmpArchive.length()}")
        
        if (tmpArchive.exists() && tmpArchive.length() > 900_000_000) {
            // Use temp location version if it's a valid complete file (>900MB)
            Log.d(TAG, "Found valid archive in /data/local/tmp: ${tmpArchive.absolutePath} (${tmpArchive.length()} bytes)")
            System.err.println("[ARCHDROID] Using /data/local/tmp archive (${tmpArchive.length()} bytes)")
            archive = tmpArchive
        }
        // Fallback: also check sdcard
        val sdcardArchive = File("/sdcard/ArchLinuxARM-aarch64-latest.tar.gz")
        Log.d(TAG, "Checking /sdcard: exists=${sdcardArchive.exists()}, size=${sdcardArchive.length()}")
        
        if (archive == File(context.cacheDir, "arch-rootfs.tar.gz") && 
            sdcardArchive.exists() && sdcardArchive.length() > 900_000_000) {
            Log.d(TAG, "Found valid archive on sdcard: ${sdcardArchive.absolutePath} (${sdcardArchive.length()} bytes)")
            System.err.println("[ARCHDROID] Using /sdcard archive (${sdcardArchive.length()} bytes)")
            archive = sdcardArchive
        }
        
        Log.d(TAG, "Final archive path: ${archive.absolutePath}, exists=${archive.exists()}, size=${archive.length()}")
        
        // Check if archive already exists (for offline testing or pre-pushed files)
        if (!archive.exists() || archive.length() == 0L) {
            System.err.println("[ARCHDROID] Archive not found, downloading...")
            emit(InstallProgress.Step("Downloading Arch Linux rootfs"))
            download(rootFsUrl(), archive) { read, total ->
                emit(InstallProgress.Download(read, total))
            }
            
            // Verify download
            if (!archive.exists() || archive.length() == 0L) {
                throw IllegalStateException("Download failed: archive file not found or empty (${archive.length()} bytes)")
            }
        } else {
            Log.d(TAG, "Using existing archive: ${archive.absolutePath} (${archive.length()} bytes)")
            System.err.println("[ARCHDROID] Using existing archive (${archive.length()} bytes)")
        }

        if (hasUsableRootfs()) {
            Log.d(TAG, "Existing rootfs looks usable; skipping extraction")
            System.err.println("[ARCHDROID] Existing rootfs looks usable, skipping extraction")
        } else {
            if (rootFsDir.exists() && rootFsDir.list()?.isNotEmpty() == true) {
                Log.d(TAG, "Clearing incomplete rootfs before extraction")
                rootFsDir.deleteRecursively()
                rootFsDir.mkdirs()
            }

            System.err.println("[ARCHDROID] Starting extraction...")
            emit(InstallProgress.Step("Extracting rootfs"))
            extractWithBusybox(archive)
            System.err.println("[ARCHDROID] Extraction completed!")
        }

        System.err.println("[ARCHDROID] Configuring shell...")
        emit(InstallProgress.Step("Configuring shell"))
        writeBaseConfig()
        marker.writeText("ok\n")
        System.err.println("[ARCHDROID] Installation complete!")
        emit(InstallProgress.Done)
    }.flowOn(Dispatchers.IO)

    private fun supportedLinuxAbi(): String = when {
        Build.SUPPORTED_ABIS.any { it == "arm64-v8a" } -> "arm64-v8a"
        Build.SUPPORTED_ABIS.any { it == "armeabi-v7a" } -> "armeabi-v7a"
        else -> error("Unsupported Android ABI: ${Build.SUPPORTED_ABIS.joinToString()}")
    }

    private fun rootFsUrl(): String = when (supportedLinuxAbi()) {
        "arm64-v8a" ->
            "https://archlinuxarm.org/os/ArchLinuxARM-aarch64-latest.tar.gz"
        "armeabi-v7a" ->
            "https://archlinuxarm.org/os/ArchLinuxARM-armv7-latest.tar.gz"
        else -> error("Unsupported Android ABI: ${Build.SUPPORTED_ABIS.joinToString()}")
    }

    private fun prepareExecutable(abi: String, assetName: String, target: File) {
        if (target.exists()) return
        val assetPath = "bin/$abi/$assetName"
        val inputStream = try {
            context.assets.open(assetPath)
        } catch (e: FileNotFoundException) {
            throw IllegalStateException(
                """
                Missing native binary: $assetName for ABI $abi
                
                Required file: app/src/main/assets/$assetPath
                
                Solutions:
                1. Run: ./setup-binaries.sh (recommended)
                2. Or follow manual setup in docs/BUILD.md
                
                For more help, see TROUBLESHOOTING.md
                """.trimIndent(),
                e
            )
        }
        inputStream.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        target.setExecutable(true, true)
    }

    private suspend fun download(url: String, target: File, progress: suspend (Long, Long) -> Unit) {
        var currentUrl = url
        var redirectCount = 0
        val maxRedirects = 5
        
        while (redirectCount < maxRedirects) {
            val connection = URL(currentUrl).openConnection() as HttpURLConnection
            
            // Setup HTTPS with permissive certificate verification for public downloads
            if (connection is HttpsURLConnection) {
                val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                        override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
                        override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
                    }
                )
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                connection.sslSocketFactory = sslContext.socketFactory
                connection.hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier()
            }
            
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.setRequestProperty("User-Agent", "ArchDroid/0.1.0")
            connection.instanceFollowRedirects = false  // Handle redirects manually
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Download response: $responseCode from $currentUrl")
            
            when (responseCode) {
                in 200..299 -> {
                    // Success - download the file
                    val total = connection.contentLengthLong
                    Log.d(TAG, "Downloading $total bytes from $currentUrl")
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
                    return
                }
                in 300..399 -> {
                    // Redirect
                    val location = connection.getHeaderField("Location")
                    if (location == null) {
                        throw Exception("Redirect response without Location header: HTTP $responseCode")
                    }
                    currentUrl = if (location.startsWith("http")) location else {
                        URL(URL(currentUrl), location).toString()
                    }
                    Log.d(TAG, "Redirect to: $currentUrl")
                    redirectCount++
                    connection.disconnect()
                }
                else -> {
                    val error = connection.inputStream.bufferedReader().readText()
                    connection.disconnect()
                    throw Exception("Download failed: HTTP $responseCode\n$error")
                }
            }
        }
        throw Exception("Too many redirects (>$maxRedirects) downloading $url")
    }

    private fun extractWithBusybox(archive: File) {
        // Verify file exists and has size
        Log.d(TAG, "Extract archive: ${archive.absolutePath} (${archive.length()} bytes)")
        
        if (!archive.exists()) {
            throw Exception("Archive file not found: ${archive.absolutePath}")
        }
        if (archive.length() == 0L) {
            throw Exception("Archive file is empty")
        }
        
        // Check disk space - extraction needs significant space
        // Rough estimate: archive size * 2.5 (gzipped + uncompressed both needed)
        val requiredSpace = (archive.length() * 2.5).toLong()
        val freeSpace = rootFsDir.freeSpace
        Log.d(TAG, "Disk space check - Free: ${freeSpace / (1024*1024)}MB, Required: ${requiredSpace / (1024*1024)}MB")
        
        if (freeSpace < requiredSpace) {
            throw Exception(
                "Insufficient disk space for extraction.\n" +
                "Required: ${requiredSpace / (1024*1024*1024)}GB\n" +
                "Available: ${freeSpace / (1024*1024*1024)}GB\n" +
                "Please free up at least 4GB of storage."
            )
        }
        
        // Use system tar to extract directly from gzip file
        // This is more robust than decompressing with GZIPInputStream first
        // Use --no-same-owner to skip failed chown operations (permission denied in app sandbox)
        // This significantly speeds up extraction since we don't need to wait for permission errors
        Log.d(TAG, "Starting tar extraction: tar --no-same-owner --no-same-permissions -xzf ${archive.name}")
        System.err.println("[ARCHDROID] Starting tar extraction...")
        
        val process = ProcessBuilder("tar", "--no-same-owner", "--no-same-permissions", "-xzf", archive.absolutePath, "-C", rootFsDir.absolutePath)
            .redirectErrorStream(true)
            .start()
        val output = StringBuilder()
        val outputReader = Thread {
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (output.length < 16_000) {
                        output.appendLine(line)
                    }
                }
            }
        }.apply { start() }

        val exitCode = process.waitFor()
        outputReader.join()
        val errorOutput = output.toString()
        
        Log.d(TAG, "tar exit code: $exitCode")
        if (errorOutput.isNotEmpty()) {
            Log.d(TAG, "tar stderr: $errorOutput")
        }
        
        // Check if tar reported an error
        if (exitCode != 0) {
            Log.e(TAG, "tar extraction failed with exit code $exitCode")
            Log.e(TAG, "Error output: $errorOutput")
        }
        
        // Validate extraction completeness BEFORE continuing
        // Check for critical directories that must exist in a valid Arch rootfs
        val etcDir = File(rootFsDir, "etc")
        val usrDir = File(rootFsDir, "usr")
        val bootDir = File(rootFsDir, "boot")
        
        Log.d(TAG, "Validating extraction - checking critical directories")
        Log.d(TAG, "/etc exists: ${etcDir.exists()}, is directory: ${etcDir.isDirectory}")
        Log.d(TAG, "/usr exists: ${usrDir.exists()}, is directory: ${usrDir.isDirectory}")
        Log.d(TAG, "/boot exists: ${bootDir.exists()}, is directory: ${bootDir.isDirectory}")
        
        if (!etcDir.exists() || !etcDir.isDirectory) {
            val extracted = rootFsDir.walk().filter { it.isFile }.count()
            Log.e(TAG, "Incomplete extraction: /etc directory not found (only $extracted files extracted)")
            throw Exception(
                "Extraction validation failed - missing critical /etc directory.\n" +
                "This usually means the archive was truncated or corrupted during transfer.\n" +
                "Files extracted: $extracted (expected 2000+)\n" +
                "Please verify the archive file and retry."
            )
        }
        
        if (!usrDir.exists() || !usrDir.isDirectory) {
            val extracted = rootFsDir.walk().filter { it.isFile }.count()
            Log.e(TAG, "Incomplete extraction: /usr directory not found (only $extracted files extracted)")
            throw Exception(
                "Extraction validation failed - missing critical /usr directory.\n" +
                "This usually means the archive was truncated or corrupted during transfer.\n" +
                "Files extracted: $extracted (expected 2000+)\n" +
                "Please verify the archive file and retry."
            )
        }
        
        // Count actual files (not just directories)
        val extractedFiles = rootFsDir.walk().filter { it.isFile }.count()
        Log.d(TAG, "Extraction complete: $extractedFiles files extracted")
        
        if (extractedFiles < 500) {
            Log.e(TAG, "Extraction validation failed: only $extractedFiles files extracted (expected 2000+)")
            throw Exception(
                "Extraction appears incomplete - only $extractedFiles files extracted (expected 2000+).\n" +
                "This suggests the archive file is corrupted or truncated.\n" +
                "Please verify the archive file is valid with: tar -tzf <archive>"
            )
        }
        
        Log.d(TAG, "✓ Archive extracted successfully ($extractedFiles files)")
    }

    private fun hasUsableRootfs(): Boolean {
        val etcDir = File(rootFsDir, "etc")
        val usrDir = File(rootFsDir, "usr")
        if (!etcDir.isDirectory || !usrDir.isDirectory) return false
        return rootFsDir.walk().filter { it.isFile }.take(501).count() > 500
    }

    private fun writeBaseConfig() {
        val home = File(rootFsDir, "home/arch").apply { mkdirs() }
        listOf("downloads", "documents", "pictures", "music").forEach {
            File(rootFsDir, "home/arch/storage/$it").mkdirs()
        }
        writeTextReplacingSymlink(File(rootFsDir, "etc/hostname"), "android\n")
        writeTextReplacingSymlink(File(rootFsDir, "etc/resolv.conf"), "nameserver 1.1.1.1\nnameserver 8.8.8.8\n")
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

    private fun writeTextReplacingSymlink(file: File, text: String) {
        file.parentFile?.mkdirs()
        val path = file.toPath()
        if (Files.isSymbolicLink(path)) {
            Files.delete(path)
        }
        file.writeText(text)
    }
}

sealed interface InstallProgress {
    data class Step(val message: String) : InstallProgress
    data class Download(val readBytes: Long, val totalBytes: Long) : InstallProgress
    data class Error(val message: String) : InstallProgress
    data object Done : InstallProgress
}
