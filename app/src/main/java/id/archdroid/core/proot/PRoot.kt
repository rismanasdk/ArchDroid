package id.archdroid.core.proot

import android.content.Context
import id.archdroid.core.rootfs.RootFsInstaller
import java.io.File

class PRoot(
    private val context: Context,
    private val installer: RootFsInstaller
) {
    fun command(cwd: String = "/home/arch", shell: String = "/usr/bin/bash"): PRootCommand {
        val rootfs = installer.rootFsDir.absolutePath
        val proot = File(installer.binDir, "proot").absolutePath
        val storage = context.getExternalFilesDir(null)?.absolutePath ?: context.filesDir.absolutePath
        val shared = "/storage/emulated/0"

        val args = mutableListOf(
            proot,
            "--link2symlink",
            "-0",
            "-r", rootfs,
            "-b", "/dev",
            "-b", "/proc",
            "-b", "/sys",
            "-b", "/sdcard:/sdcard",
            "-b", "$shared/Download:/home/arch/storage/downloads",
            "-b", "$shared/Documents:/home/arch/storage/documents",
            "-b", "$shared/Pictures:/home/arch/storage/pictures",
            "-b", "$shared/Music:/home/arch/storage/music",
            "-b", "$storage:/home/arch/android-app",
            "-w", cwd,
            "/usr/bin/env",
            "-i",
            "HOME=/home/arch",
            "USER=arch",
            "LOGNAME=arch",
            "SHELL=$shell",
            "TERM=xterm-256color",
            "PATH=/usr/local/sbin:/usr/local/bin:/usr/bin:/usr/sbin:/sbin:/bin",
            shell,
            "-l"
        )
        return PRootCommand(executable = args.first(), args = args.drop(1).toTypedArray(), env = emptyArray(), cwd = context.filesDir.absolutePath)
    }
}

data class PRootCommand(
    val executable: String,
    val args: Array<String>,
    val env: Array<String>,
    val cwd: String
)
