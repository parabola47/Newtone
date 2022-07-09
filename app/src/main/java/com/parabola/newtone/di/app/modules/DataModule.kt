package com.parabola.newtone.di.app.modules

import android.content.ContentResolver
import android.content.SharedPreferences
import com.parabola.data.repository.*
import com.parabola.domain.executor.SchedulerProvider
import com.parabola.domain.repository.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataModule {

    @Singleton
    @Provides
    fun provideDataExtractor(
        excludedFolderRepo: ExcludedFolderRepository,
        schedulers: SchedulerProvider,
        permissionHandler: PermissionHandler,
        contentResolver: ContentResolver,
        preferences: SharedPreferences,
    ): DataExtractor = DataExtractor(
        excludedFolderRepo,
        schedulers,
        permissionHandler,
        contentResolver,
        preferences
    )

    @Singleton
    @Provides
    fun provideTrackRepository(
        playlistRepo: PlaylistRepository,
        dataExtractor: DataExtractor
    ): TrackRepository = TrackRepositoryImpl(playlistRepo, dataExtractor)

    @Singleton
    @Provides
    fun provideAlbumRepository(dataExtractor: DataExtractor): AlbumRepository =
        AlbumRepositoryImpl(dataExtractor)

    @Singleton
    @Provides
    fun provideArtistRepository(dataExtractor: DataExtractor): ArtistRepository =
        ArtistRepositoryImpl(dataExtractor)

    @Singleton
    @Provides
    fun providePlaylistRepository(
        dataExtractor: DataExtractor,
        contentResolver: ContentResolver,
        accessRepo: PermissionHandler,
    ): PlaylistRepository = PlaylistRepositoryImpl(dataExtractor, contentResolver, accessRepo)

    @Singleton
    @Provides
    fun provideFolderRepository(dataExtractor: DataExtractor): FolderRepository =
        FolderRepositoryImpl(dataExtractor)

    @Singleton
    @Provides
    fun provideExcludedFolderRepository(prefs: SharedPreferences): ExcludedFolderRepository =
        ExcludedFolderRepositoryImpl(prefs)
}
