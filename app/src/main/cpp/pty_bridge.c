#include <jni.h>
#include <pty.h>
#include <unistd.h>
#include <sys/ioctl.h>

JNIEXPORT jint JNICALL
Java_id_archdroid_core_terminal_NativePty_open(JNIEnv *env, jobject thiz) {
    int master = -1;
    int slave = -1;
    if (openpty(&master, &slave, NULL, NULL, NULL) == -1) return -1;
    close(slave);
    return master;
}

JNIEXPORT jint JNICALL
Java_id_archdroid_core_terminal_NativePty_resize(JNIEnv *env, jobject thiz, jint fd, jint rows, jint cols) {
    struct winsize size;
    size.ws_row = rows;
    size.ws_col = cols;
    size.ws_xpixel = 0;
    size.ws_ypixel = 0;
    return ioctl(fd, TIOCSWINSZ, &size);
}
