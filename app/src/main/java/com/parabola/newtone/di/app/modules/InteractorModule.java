package com.parabola.newtone.di.app.modules;

import android.content.Context;
import android.content.Intent;

import com.parabola.countdown_timer_feature.SleepTimerImpl;
import com.parabola.domain.interactors.AlbumInteractor;
import com.parabola.domain.interactors.ArtistInteractor;
import com.parabola.domain.interactors.SleepTimerInteractor;
import com.parabola.domain.interactors.TrackInteractor;
import com.parabola.domain.interactors.player.AudioEffectsInteractor;
import com.parabola.domain.interactors.player.PlayerInteractor;
import com.parabola.domain.interactors.player.PlayerSetting;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.ArtistRepository;
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
    PlayerInteractor providePlayerInteractor(Context context, TrackRepository trackRepo,
                                             @Named(IntentModule.OPEN_ACTIVITY_INTENT) Intent openActivityIntent) {
        return new PlayerInteractorImpl(context, trackRepo, openActivityIntent);
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
    ArtistInteractor provideArtistInteractor(ArtistRepository artistRepo, SortingRepository sortingRepo) {
        return new ArtistInteractor(artistRepo, sortingRepo);
    }
}
