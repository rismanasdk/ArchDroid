package id.archdroid.ui.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun SessionsRoute(nav: NavHostController, vm: SessionsViewModel = hiltViewModel()) {
    val sessions = vm.sessions.collectAsState(initial = emptyList())
    Column(Modifier.fillMaxSize().padding(8.dp)) {
        Text("Sessions", modifier = Modifier.padding(8.dp))
        sessions.value.forEach {
            ListItem(
                headlineContent = { Text(it.title) },
                supportingContent = { Text(it.cwd) },
                modifier = Modifier.clickable { nav.navigate("terminal/${it.id}") }
            )
        }
    }
}
