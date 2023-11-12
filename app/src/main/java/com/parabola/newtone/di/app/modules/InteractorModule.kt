package com.parabola.newtone.di.app.modules

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.parabola.data.repository.DataExtractor
import com.parabola.domain.interactor.*
import com.parabola.domain.interactor.player.AudioEffectsInteractor
import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.interactor.player.PlayerSetting
import com.parabola.domain.repository.*
import com.parabola.newtone.player_feature.PlayerInteractorImpl
import com.parabola.search_feature.SearchInteractor
import com.parabola.search_feature.SearchInteractorImpl
import com.parabola.sleep_timer_feature.SleepTimerImpl
import com.parabola.sleep_timer_feature.SleepTimerInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class InteractorModule {

    @Singleton
    @Provides
    fun providePlayerInteractor(
        context: Context,
        preferences: SharedPreferences,
        trackRepo: TrackRepository,
        repositoryInteractor: RepositoryInteractor,
        @Named(IntentModule.OPEN_ACTIVITY_INTENT) openActivityIntent: Intent,
    ): PlayerInteractor = PlayerInteractorImpl(
        context,
        preferences,
        trackRepo,
        repositoryInteractor,
        openActivityIntent
    )

    @Singleton
    @Provides
    fun provideFxInteractor(playerInteractor: PlayerInteractor): AudioEffectsInteractor =
        playerInteractor.audioEffectInteractor

    @Singleton
    @Provides
    fun providePlayerSetting(playerInteractor: PlayerInteractor): PlayerSetting =
        playerInteractor.playerSetting

    @Singleton
    @Provides
    fun provideSleepTimerInteractor(): SleepTimerInteractor = SleepTimerImpl()

    @Singleton
    @Provides
    fun provideRepositoryInteractor(dataExtractor: DataExtractor): RepositoryInteractor =
        dataExtractor

    @Singleton
    @Provides
    fun provideTrackInteractor(
        trackRepo: TrackRepository,
        repositoryInteractor: RepositoryInteractor,
        trackSortingRepo: SortingRepository,
    ): TrackInteractor =
        TrackInteractor(trackRepo, repositoryInteractor, trackSortingRepo)

    @Singleton
    @Provides
    fun provideAlbumInteractor(
        albumRepo: AlbumRepository,
        repositoryInteractor: RepositoryInteractor,
        sortingRepo: SortingRepository,
    ): AlbumInteractor = AlbumInteractor(
        albumRepo,
        repositoryInteractor,
        sortingRepo
    )

    @Singleton
    @Provides
    fun provideArtistInteractor(
        artistRepo: ArtistRepository,
        repositoryInteractor: RepositoryInteractor,
        sortingRepo: SortingRepository,
    ): ArtistInteractor = ArtistInteractor(
        artistRepo,
        repositoryInteractor,
        sortingRepo
    )

    @Singleton
    @Provides
    fun provideSearchInteractor(
        artistRepo: ArtistRepository,
        albumRepo: AlbumRepository,
        trackRepo: TrackRepository,
        playlistRepo: PlaylistRepository,
    ): SearchInteractor = SearchInteractorImpl(artistRepo, albumRepo, trackRepo, playlistRepo)
}
