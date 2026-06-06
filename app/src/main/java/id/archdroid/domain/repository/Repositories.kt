package id.archdroid.domain.repository

import id.archdroid.domain.model.PackageActionType
import id.archdroid.domain.model.TerminalSessionModel
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeSessions(): Flow<List<TerminalSessionModel>>
    suspend fun get(id: String): TerminalSessionModel?
    suspend fun create(shell: String = "/usr/bin/bash", bootstrapCommand: String = ""): TerminalSessionModel
    suspend fun updateState(id: String, cwd: String, scrollback: String)
    suspend fun delete(id: String)
}

interface PackageRepository {
    suspend fun buildPacmanCommand(type: PackageActionType, packageName: String = ""): List<String>
    suspend fun record(type: PackageActionType, packageName: String, status: String)
}
