package id.archdroid.ui.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.archdroid.core.proot.PRoot
import id.archdroid.core.terminal.TerminalController
import id.archdroid.domain.repository.SessionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val proot: PRoot,
    private val sessions: SessionRepository
) : ViewModel() {
    private var cwd: String = "/home/arch"
    private var scrollback: String = ""
    private val bootstrapped = mutableSetOf<String>()

    fun controller(sessionId: String): TerminalController {
        val controller = TerminalController(proot.command(cwd = cwd)) { currentDir, buffer ->
            cwd = currentDir
            scrollback = buffer
            viewModelScope.launch { sessions.updateState(sessionId, cwd, scrollback) }
        }
        viewModelScope.launch {
            val session = sessions.get(sessionId)
            val command = session?.bootstrapCommand.orEmpty()
            if (command.isNotBlank() && bootstrapped.add(sessionId)) {
                delay(500)
                controller.paste("$command\n")
            }
        }
        return controller
    }

    fun persist(sessionId: String) {
        viewModelScope.launch { sessions.updateState(sessionId, cwd, scrollback) }
    }
}
