package id.archdroid.ui.storage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun StorageRoute(nav: NavHostController) {
    val mounts = listOf(
        "/storage/emulated/0/Download -> ~/storage/downloads",
        "/storage/emulated/0/Documents -> ~/storage/documents",
        "/storage/emulated/0/Pictures -> ~/storage/pictures",
        "/storage/emulated/0/Music -> ~/storage/music"
    )
    Column(Modifier.fillMaxSize().padding(8.dp)) {
        Text("Storage", modifier = Modifier.padding(8.dp))
        mounts.forEach { ListItem(headlineContent = { Text(it) }) }
    }
}
