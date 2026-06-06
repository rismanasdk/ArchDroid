package id.archdroid.core.terminal

import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import id.archdroid.core.proot.PRootCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TerminalController(
    private val command: PRootCommand,
    private val onStateChanged: (cwd: String, scrollback: String) -> Unit
) : TerminalSessionClient {
    private val _title = MutableStateFlow("ArchDroid")
    val title: StateFlow<String> = _title
    private var session: TerminalSession? = null

    fun start(): TerminalSession {
        val existing = session
        if (existing != null) return existing
        return TerminalSession(
            command.executable,
            command.cwd,
            command.args,
            command.env,
            20_000,
            this
        ).also {
            session = it
        }
    }

    fun paste(text: String) {
        val bytes = text.toByteArray(Charsets.UTF_8)
        session?.write(bytes, 0, bytes.size)
    }

    fun resize(cols: Int, rows: Int) {
        session?.updateSize(cols, rows)
    }

    fun finish() {
        session?.finishIfRunning()
        session = null
    }

    override fun onTextChanged(changedSession: TerminalSession) {
        val transcript = changedSession.emulator?.screen?.transcriptText.orEmpty()
        onStateChanged("/home/arch", transcript)
    }

    override fun onTitleChanged(changedSession: TerminalSession) {
        _title.value = changedSession.title ?: "ArchDroid"
    }

    override fun onSessionFinished(finishedSession: TerminalSession) = Unit
    override fun onCopyTextToClipboard(session: TerminalSession, text: String) = Unit
    override fun onPasteTextFromClipboard(session: TerminalSession) = Unit
    override fun onBell(session: TerminalSession) = Unit
    override fun onColorsChanged(session: TerminalSession) = Unit
    override fun onTerminalCursorStateChange(state: Boolean) = Unit
    override fun getTerminalCursorStyle(): Int? = null
    override fun logError(tag: String, message: String) = Unit
    override fun logWarn(tag: String, message: String) = Unit
    override fun logInfo(tag: String, message: String) = Unit
    override fun logDebug(tag: String, message: String) = Unit
    override fun logVerbose(tag: String, message: String) = Unit
    override fun logStackTraceWithMessage(tag: String, message: String, e: Exception) = Unit
    override fun logStackTrace(tag: String, e: Exception) = Unit
}
