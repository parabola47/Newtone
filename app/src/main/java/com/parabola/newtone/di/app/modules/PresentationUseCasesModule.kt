package com.parabola.newtone.di.app.modules

import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.repository.TrackRepository
import com.parabola.newtone.presentation.artist.ArtistScreenUseCases
import com.parabola.newtone.presentation.main.albums.TabAlbumScreenUseCases
import com.parabola.newtone.presentation.main.artists.TabArtistScreenUseCases
import com.parabola.newtone.presentation.main.start.StartScreenUseCases
import com.parabola.newtone.presentation.playlist.folderslist.FoldersListScreenUseCases
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PresentationUseCasesModule {

    @Singleton
    @Provides
    fun provideStartScreenUseCases(
        playerInteractor: PlayerInteractor,
        trackRepository: TrackRepository,
    ): StartScreenUseCases = StartScreenUseCases(
        playerInteractor = playerInteractor,
        trackRepository = trackRepository,
    )

    @Singleton
    @Provides
    fun provideTabArtistScreenUseCases(
        playerInteractor: PlayerInteractor,
        trackRepository: TrackRepository,
    ): TabArtistScreenUseCases = TabArtistScreenUseCases(
        playerInteractor = playerInteractor,
        trackRepository = trackRepository,
    )

    @Singleton
    @Provides
    fun provideTabAlbumScreenUseCases(
        playerInteractor: PlayerInteractor,
        trackRepository: TrackRepository,
    ): TabAlbumScreenUseCases = TabAlbumScreenUseCases(
        playerInteractor = playerInteractor,
        trackRepository = trackRepository,
    )

    @Singleton
    @Provides
    fun provideArtistScreenUseCases(
        playerInteractor: PlayerInteractor,
        trackRepository: TrackRepository,
    ): ArtistScreenUseCases = ArtistScreenUseCases(
        playerInteractor = playerInteractor,
        trackRepository = trackRepository,
    )

    @Singleton
    @Provides
    fun provideFoldersListScreenUseCases(
        playerInteractor: PlayerInteractor,
        trackRepository: TrackRepository,
    ): FoldersListScreenUseCases = FoldersListScreenUseCases(
        playerInteractor = playerInteractor,
        trackRepository = trackRepository,
    )

}
