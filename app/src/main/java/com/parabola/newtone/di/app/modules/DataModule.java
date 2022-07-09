package com.parabola.newtone.di.app.modules;

import android.content.ContentResolver;
import android.content.SharedPreferences;

import com.parabola.data.repository.AlbumRepositoryImpl;
import com.parabola.data.repository.ArtistRepositoryImpl;
import com.parabola.data.repository.DataExtractor;
import com.parabola.data.repository.ExcludedFolderRepositoryImpl;
import com.parabola.data.repository.FolderRepositoryImpl;
import com.parabola.data.repository.PlaylistRepositoryImpl;
import com.parabola.data.repository.TrackRepositoryImpl;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.ExcludedFolderRepository;
import com.parabola.domain.repository.FolderRepository;
import com.parabola.domain.repository.PermissionHandler;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.domain.repository.TrackRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class DataModule {

    @Singleton
    @Provides
    DataExtractor provideDataExtractor(ExcludedFolderRepository excludedFolderRepo,
                                       SchedulerProvider schedulers,
                                       PermissionHandler permissionHandler,
                                       ContentResolver contentResolver,
                                       SharedPreferences preferences) {
        return new DataExtractor(excludedFolderRepo, schedulers, permissionHandler, contentResolver, preferences);
    }

    @Singleton
    @Provides
    TrackRepository provideTrackRepository(PlaylistRepository playlistRepo,
                                           DataExtractor dataExtractor) {
        return new TrackRepositoryImpl(playlistRepo, dataExtractor);
    }

    @Singleton
    @Provides
    AlbumRepository provideAlbumRepository(DataExtractor dataExtractor) {
        return new AlbumRepositoryImpl(dataExtractor);
    }

    @Singleton
    @Provides
    ArtistRepository provideArtistRepository(DataExtractor dataExtractor) {
        return new ArtistRepositoryImpl(dataExtractor);
    }

    @Singleton
    @Provides
    PlaylistRepository providePlaylistRepository(DataExtractor dataExtractor,
                                                 ContentResolver contentResolver,
                                                 PermissionHandler accessRepo) {
        return new PlaylistRepositoryImpl(dataExtractor, contentResolver, accessRepo);
    }

    @Singleton
    @Provides
    FolderRepository provideFolderRepository(DataExtractor dataExtractor) {
        return new FolderRepositoryImpl(dataExtractor);
    }


    @Singleton
    @Provides
    ExcludedFolderRepository provideExcludedFolderRepository(SharedPreferences prefs) {
        return new ExcludedFolderRepositoryImpl(prefs);
    }

}
