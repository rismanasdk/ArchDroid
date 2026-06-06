package id.archdroid.ui.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.archdroid.core.rootfs.InstallProgress

@Composable
fun SplashRoute(onReady: () -> Unit, vm: SplashViewModel = hiltViewModel()) {
    val state = vm.state
    LaunchedEffect(Unit) { vm.installIfNeeded(onReady) }
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("ArchDroid", style = MaterialTheme.typography.headlineLarge)
        if (state.value !is InstallProgress.Error) {
            CircularProgressIndicator()
        }
        Text(
            when (val s = state.value) {
                InstallProgress.Done -> "Starting Arch Linux"
                is InstallProgress.Download -> "Downloading ${s.readBytes / 1024 / 1024} MB"
                is InstallProgress.Error -> s.message
                is InstallProgress.Step -> s.message
            },
            textAlign = TextAlign.Center
        )
        if (state.value is InstallProgress.Error) {
            Button(onClick = { vm.installIfNeeded(onReady) }, modifier = Modifier.padding(top = 16.dp)) {
                Text("Retry")
            }
        }
    }
}
