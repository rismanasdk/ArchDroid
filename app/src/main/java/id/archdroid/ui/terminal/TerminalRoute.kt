package id.archdroid.ui.terminal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import id.archdroid.core.terminal.TerminalHostView

@Composable
fun TerminalRoute(sessionId: String, vm: TerminalViewModel = hiltViewModel()) {
    val controller = remember(sessionId) { vm.controller(sessionId) }
    DisposableEffect(Unit) { onDispose { vm.persist(sessionId) } }
    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context -> TerminalHostView(context).apply { attach(controller) } },
            update = { it.attach(controller) }
        )
    }
}
