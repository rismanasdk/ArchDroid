package id.archdroid.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun SettingsRoute(nav: NavHostController) {
    var font by remember { mutableFloatStateOf(14f) }
    var dark by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().padding(8.dp)) {
        Text("Settings", modifier = Modifier.padding(8.dp))
        ListItem(headlineContent = { Text("Font size ${font.toInt()}") }, supportingContent = { Slider(font, { font = it }, valueRange = 10f..24f) })
        ListItem(headlineContent = { Text("Dark mode") }, trailingContent = { Switch(dark, { dark = it }) })
        ListItem(headlineContent = { Text("Default shell") }, supportingContent = { Text("Bash, optional Zsh after pacman -S zsh") })
        ListItem(headlineContent = { Text("Scrollback") }, supportingContent = { Text("Large buffer, persisted snapshot capped at 256 KB") })
    }
}
