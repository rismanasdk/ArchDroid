package id.archdroid.data.repository

import id.archdroid.data.local.SessionDao
import id.archdroid.data.local.TerminalSessionEntity
import id.archdroid.domain.model.TerminalSessionModel
import id.archdroid.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val dao: SessionDao
) : SessionRepository {
    override fun observeSessions(): Flow<List<TerminalSessionModel>> =
        dao.observeSessions().map { sessions -> sessions.map { it.toModel() } }

    override suspend fun get(id: String): TerminalSessionModel? = dao.getSession(id)?.toModel()

    override suspend fun create(shell: String, bootstrapCommand: String): TerminalSessionModel {
        val now = System.currentTimeMillis()
        val entity = TerminalSessionEntity(
            id = UUID.randomUUID().toString(),
            title = if (bootstrapCommand.isBlank()) "Arch" else bootstrapCommand,
            cwd = "/home/arch",
            shell = shell,
            isActive = true,
            createdAt = now,
            updatedAt = now,
            bootstrapCommand = bootstrapCommand
        )
        dao.upsert(entity)
        return entity.toModel()
    }

    override suspend fun updateState(id: String, cwd: String, scrollback: String) {
        dao.updateState(id, cwd, scrollback.takeLast(256_000), System.currentTimeMillis())
    }

    override suspend fun delete(id: String) = dao.delete(id)

    private fun TerminalSessionEntity.toModel() =
        TerminalSessionModel(id, title, cwd, shell, isActive, updatedAt, bootstrapCommand)
}
