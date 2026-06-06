package id.archdroid.core.common

sealed interface ResultState<out T> {
    data object Idle : ResultState<Nothing>
    data object Loading : ResultState<Nothing>
    data class Success<T>(val value: T) : ResultState<T>
    data class Error(val message: String, val cause: Throwable? = null) : ResultState<Nothing>
}
