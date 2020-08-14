package com.parabola.newtone.di.app.modules;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.parabola.countdown_timer_feature.SleepTimerImpl;
import com.parabola.data.repository.DataExtractor;
import com.parabola.domain.interactor.AlbumInteractor;
import com.parabola.domain.interactor.ArtistInteractor;
import com.parabola.domain.interactor.FolderInteractor;
import com.parabola.domain.interactor.RepositoryInteractor;
import com.parabola.domain.interactor.SearchInteractor;
import com.parabola.domain.interactor.SleepTimerInteractor;
import com.parabola.domain.interactor.TrackInteractor;
import com.parabola.domain.interactor.player.AudioEffectsInteractor;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.interactor.player.PlayerSetting;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.player_feature.PlayerInteractorImpl;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class InteractorModule {

    @Singleton
    @Provides
    PlayerInteractor providePlayerInteractor(Context context, SharedPreferences preferences,
                                             TrackRepository trackRepo,
                                             RepositoryInteractor repositoryInteractor,
                                             @Named(IntentModule.OPEN_ACTIVITY_INTENT) Intent openActivityIntent) {
        return new PlayerInteractorImpl(context, preferences, trackRepo, repositoryInteractor, openActivityIntent);
    }

    @Singleton
    @Provides
    AudioEffectsInteractor provideFxInteractor(PlayerInteractor playerInteractor) {
        return playerInteractor.getAudioEffectInteractor();
    }

    @Singleton
    @Provides
    PlayerSetting providePlayerSetting(PlayerInteractor playerInteractor) {
        return playerInteractor.getPlayerSetting();
    }

    @Singleton
    @Provides
    SleepTimerInteractor provideSleepTimerInteractor() {
        return new SleepTimerImpl();
    }


    @Singleton
    @Provides
    RepositoryInteractor provideRepositoryInteractor(DataExtractor dataExtractor) {
        return dataExtractor;
    }


    @Singleton
    @Provides
    TrackInteractor provideTrackInteractor(TrackRepository trackRepo,
                                           RepositoryInteractor repositoryInteractor,
                                           SortingRepository trackSortingRepo) {
        return new TrackInteractor(trackRepo, repositoryInteractor, trackSortingRepo);
    }

    @Singleton
    @Provides
    AlbumInteractor provideAlbumInteractor(AlbumRepository albumRepo, TrackRepository trackRepo,
                                           PlayerInteractor playerInteractor,
                                           RepositoryInteractor repositoryInteractor,
                                           SortingRepository sortingRepo) {
        return new AlbumInteractor(albumRepo, trackRepo, playerInteractor, repositoryInteractor, sortingRepo);
    }

    @Singleton
    @Provides
    ArtistInteractor provideArtistInteractor(ArtistRepository artistRepo, TrackRepository trackRepo,
                                             PlayerInteractor playerInteractor,
                                             RepositoryInteractor repositoryInteractor,
                                             SortingRepository sortingRepo) {
        return new ArtistInteractor(artistRepo, trackRepo, playerInteractor, repositoryInteractor, sortingRepo);
    }

    @Singleton
    @Provides
    FolderInteractor provideFolderInteractor(TrackRepository trackRepo, PlayerInteractor playerInteractor) {
        return new FolderInteractor(trackRepo, playerInteractor);
    }

    @Singleton
    @Provides
    SearchInteractor provideSearchInteractor(ArtistRepository artistRepo, AlbumRepository albumRepo,
                                             TrackRepository trackRepo, PlaylistRepository playlistRepo) {
        return new SearchInteractor(artistRepo, albumRepo, trackRepo, playlistRepo);
    }
}
