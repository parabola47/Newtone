package com.parabola.newtone.di.app.modules;

import android.content.ContentResolver;
import android.content.Context;

import com.parabola.data.repository.AlbumRepositoryImpl;
import com.parabola.data.repository.ArtistRepositoryImpl;
import com.parabola.data.repository.FolderRepositoryImpl;
import com.parabola.data.repository.PlaylistRepositoryImpl;
import com.parabola.data.repository.TrackRepositoryImpl;
import com.parabola.domain.repository.AccessRepository;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.FolderRepository;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class DataModule {

    @Singleton
    @Provides
    TrackRepository provideTrackRepository(Context context, AlbumRepository albumRepo, PlaylistRepository playlistRepo, AccessRepository accessRepo) {
        return new TrackRepositoryImpl(context, albumRepo, playlistRepo, accessRepo);
    }

    @Singleton
    @Provides
    AlbumRepository provideAlbumRepository(ContentResolver contentResolver, AccessRepository accessRepo) {
        return new AlbumRepositoryImpl(contentResolver, accessRepo);
    }

    @Singleton
    @Provides
    ArtistRepository provideArtistRepository(ContentResolver contentResolver, AccessRepository accessRepo) {
        return new ArtistRepositoryImpl(contentResolver, accessRepo);
    }

    @Singleton
    @Provides
    PlaylistRepository providePlaylistRepository(ContentResolver contentResolver, AccessRepository accessRepo) {
        return new PlaylistRepositoryImpl(contentResolver, accessRepo);
    }

    @Singleton
    @Provides
    FolderRepository provideFolderRepository(ContentResolver contentResolver, TrackRepository trackRepo, SortingRepository sortingRepository) {
        return new FolderRepositoryImpl(contentResolver, trackRepo, sortingRepository);
    }
}
