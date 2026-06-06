package id.archdroid.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun HomeRoute(nav: NavHostController, vm: HomeViewModel = hiltViewModel()) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("ArchDroid", style = MaterialTheme.typography.headlineMedium)
        HomeButton("New Terminal", Icons.Outlined.Terminal) {
            vm.newSession { nav.navigate("terminal/$it") }
        }
        HomeButton("Sessions", Icons.Outlined.Folder) { nav.navigate("sessions") }
        HomeButton("Packages", Icons.Outlined.Storage) { nav.navigate("packages") }
        HomeButton("Storage", Icons.Outlined.Folder) { nav.navigate("storage") }
        HomeButton("Settings", Icons.Outlined.Settings) { nav.navigate("settings") }
    }
}

@Composable
private fun HomeButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null)
            Text(text)
        }
    }
}
