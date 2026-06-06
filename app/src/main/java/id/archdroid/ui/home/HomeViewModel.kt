package id.archdroid.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.archdroid.domain.repository.SessionRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessions: SessionRepository
) : ViewModel() {
    fun newSession(open: (String) -> Unit) {
        viewModelScope.launch { open(sessions.create().id) }
    }
}
