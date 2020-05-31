package com.parabola.newtone.di.app.modules;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.parabola.countdown_timer_feature.SleepTimerImpl;
import com.parabola.domain.interactor.AlbumInteractor;
import com.parabola.domain.interactor.ArtistInteractor;
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
                                             @Named(IntentModule.OPEN_ACTIVITY_INTENT) Intent openActivityIntent) {
        return new PlayerInteractorImpl(context, preferences, trackRepo, openActivityIntent);
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
    TrackInteractor provideTrackInteractor(TrackRepository trackRepo, SortingRepository trackSortingRepo) {
        return new TrackInteractor(trackRepo, trackSortingRepo);
    }

    @Singleton
    @Provides
    AlbumInteractor provideAlbumInteractor(AlbumRepository albumRepo, SortingRepository sortingRepo) {
        return new AlbumInteractor(albumRepo, sortingRepo);
    }

    @Singleton
    @Provides
    ArtistInteractor provideArtistInteractor(ArtistRepository artistRepo, TrackRepository trackRepo,
                                             PlayerInteractor playerInteractor,
                                             SortingRepository sortingRepo) {
        return new ArtistInteractor(artistRepo, trackRepo, playerInteractor, sortingRepo);
    }

    @Singleton
    @Provides
    SearchInteractor provideSearchInteractor(ArtistRepository artistRepo, AlbumRepository albumRepo,
                                             TrackRepository trackRepo, PlaylistRepository playlistRepo) {
        return new SearchInteractor(artistRepo, albumRepo, trackRepo, playlistRepo);
    }
}
