package id.archdroid.ui.sessions

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import id.archdroid.domain.repository.SessionRepository
import javax.inject.Inject

@HiltViewModel
class SessionsViewModel @Inject constructor(repository: SessionRepository) : ViewModel() {
    val sessions = repository.observeSessions()
}
