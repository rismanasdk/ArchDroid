package id.archdroid.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "terminal_sessions")
data class TerminalSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val cwd: String,
    val shell: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val scrollbackSnapshot: String = "",
    val bootstrapCommand: String = ""
)

@Entity(tableName = "package_actions")
data class PackageActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val packageName: String,
    val command: String,
    val status: String,
    val createdAt: Long
)
