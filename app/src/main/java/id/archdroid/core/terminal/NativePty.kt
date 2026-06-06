package id.archdroid.core.terminal

object NativePty {
    init {
        System.loadLibrary("archdroidpty")
    }

    external fun open(): Int
    external fun resize(fd: Int, rows: Int, cols: Int): Int
}
