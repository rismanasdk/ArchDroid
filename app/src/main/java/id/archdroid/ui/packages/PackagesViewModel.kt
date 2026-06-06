package id.archdroid.ui.packages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.archdroid.domain.model.PackageActionType
import id.archdroid.domain.repository.PackageRepository
import id.archdroid.domain.repository.SessionRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PackagesViewModel @Inject constructor(
    private val packages: PackageRepository,
    private val sessions: SessionRepository
) : ViewModel() {
    fun openUpdate(open: (String) -> Unit) = openPacman(PackageActionType.Update, "", open)
    fun openInstall(name: String, open: (String) -> Unit) = openPacman(PackageActionType.Install, name, open)
    fun openSearch(name: String, open: (String) -> Unit) = openPacman(PackageActionType.Search, name, open)

    private fun openPacman(type: PackageActionType, name: String, open: (String) -> Unit) {
        viewModelScope.launch {
            val command = packages.buildPacmanCommand(type, name).joinToString(" ")
            packages.record(type, name, "opened")
            open(sessions.create(bootstrapCommand = command).id)
        }
    }
}
