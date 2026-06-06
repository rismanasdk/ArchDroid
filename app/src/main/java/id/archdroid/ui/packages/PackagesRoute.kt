package id.archdroid.ui.packages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun PackagesRoute(nav: NavHostController, vm: PackagesViewModel = hiltViewModel()) {
    var pkg by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Packages")
        OutlinedTextField(value = pkg, onValueChange = { pkg = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Package") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.openUpdate { nav.navigate("terminal/$it") } }) { Icon(Icons.Outlined.Sync, null); Text("Update") }
            Button(onClick = { vm.openInstall(pkg) { nav.navigate("terminal/$it") } }) { Icon(Icons.Outlined.Download, null); Text("Install") }
            Button(onClick = { vm.openSearch(pkg) { nav.navigate("terminal/$it") } }) { Icon(Icons.Outlined.Search, null); Text("Search") }
        }
        Text("Pacman commands run inside Arch terminal: pacman -Syu, pacman -S git, pacman -R package.")
    }
}
