package id.archdroid.data.repository

import id.archdroid.data.local.PackageActionEntity
import id.archdroid.data.local.PackageDao
import id.archdroid.domain.model.PackageActionType
import id.archdroid.domain.repository.PackageRepository
import javax.inject.Inject

class PackageRepositoryImpl @Inject constructor(
    private val dao: PackageDao
) : PackageRepository {
    override suspend fun buildPacmanCommand(type: PackageActionType, packageName: String): List<String> {
        val args = mutableListOf("sudo", "pacman")
        args += type.pacmanArgs
        if (packageName.isNotBlank()) args += packageName.trim()
        return args
    }

    override suspend fun record(type: PackageActionType, packageName: String, status: String) {
        dao.insert(
            PackageActionEntity(
                action = type.name,
                packageName = packageName,
                command = buildPacmanCommand(type, packageName).joinToString(" "),
                status = status,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
