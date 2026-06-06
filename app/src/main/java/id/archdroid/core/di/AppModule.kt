package id.archdroid.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.archdroid.core.proot.PRoot
import id.archdroid.core.rootfs.RootFsInstaller
import id.archdroid.core.security.SecurePrefs
import id.archdroid.data.local.ArchDroidDatabase
import id.archdroid.data.repository.PackageRepositoryImpl
import id.archdroid.data.repository.SessionRepositoryImpl
import id.archdroid.domain.repository.PackageRepository
import id.archdroid.domain.repository.SessionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): ArchDroidDatabase =
        Room.databaseBuilder(context, ArchDroidDatabase::class.java, "archdroid.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun sessionDao(db: ArchDroidDatabase) = db.sessionDao()
    @Provides fun packageDao(db: ArchDroidDatabase) = db.packageDao()

    @Provides
    @Singleton
    fun securePrefs(@ApplicationContext context: Context) = SecurePrefs(context)

    @Provides
    @Singleton
    fun rootFsInstaller(@ApplicationContext context: Context) = RootFsInstaller(context)

    @Provides
    @Singleton
    fun proot(@ApplicationContext context: Context, installer: RootFsInstaller) = PRoot(context, installer)

    @Provides
    @Singleton
    fun sessionRepository(impl: SessionRepositoryImpl): SessionRepository = impl

    @Provides
    @Singleton
    fun packageRepository(impl: PackageRepositoryImpl): PackageRepository = impl
}
