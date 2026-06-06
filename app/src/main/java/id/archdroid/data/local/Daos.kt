package id.archdroid.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM terminal_sessions ORDER BY updatedAt DESC")
    fun observeSessions(): Flow<List<TerminalSessionEntity>>

    @Query("SELECT * FROM terminal_sessions WHERE id = :id")
    suspend fun getSession(id: String): TerminalSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: TerminalSessionEntity)

    @Query("UPDATE terminal_sessions SET cwd = :cwd, updatedAt = :updatedAt, scrollbackSnapshot = :scrollback WHERE id = :id")
    suspend fun updateState(id: String, cwd: String, scrollback: String, updatedAt: Long)

    @Query("DELETE FROM terminal_sessions WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface PackageDao {
    @Query("SELECT * FROM package_actions ORDER BY createdAt DESC LIMIT 100")
    fun observeRecentActions(): Flow<List<PackageActionEntity>>

    @Insert
    suspend fun insert(action: PackageActionEntity)
}
