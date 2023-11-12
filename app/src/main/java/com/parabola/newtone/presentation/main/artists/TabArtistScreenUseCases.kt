package com.parabola.newtone.presentation.main.artists

import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.repository.TrackRepository
import io.reactivex.Completable
import io.reactivex.annotations.CheckReturnValue

class TabArtistScreenUseCases(
    private val trackRepository: TrackRepository,
    private val playerInteractor: PlayerInteractor,
) {

    @CheckReturnValue
    fun shuffleArtist(artistId: Int): Completable {
        return trackRepository.getByArtist(artistId)
            .doOnSuccess(playerInteractor::startInShuffleMode)
            .ignoreElement()
    }

}
