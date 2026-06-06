package id.archdroid.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TerminalSessionEntity::class, PackageActionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ArchDroidDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun packageDao(): PackageDao
}
