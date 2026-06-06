package id.archdroid.ui.splash

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.archdroid.core.rootfs.InstallProgress
import id.archdroid.core.rootfs.RootFsInstaller
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val installer: RootFsInstaller
) : ViewModel() {
    val state = mutableStateOf<InstallProgress>(InstallProgress.Step("Preparing"))

    fun installIfNeeded(onReady: () -> Unit) {
        viewModelScope.launch {
            try {
                installer.install().collect {
                    state.value = it
                    if (it == InstallProgress.Done) onReady()
                }
            } catch (e: Throwable) {
                Log.e("SplashViewModel", "Installation failed", e)
                state.value = InstallProgress.Error(
                    e.message ?: "Install failed: ${e::class.java.simpleName}\n\nPlease check:\n1. Binary files exist in app/src/main/assets/bin/\n2. Internet connection for RootFS download\n3. Storage space available"
                )
            }
        }
    }
}
