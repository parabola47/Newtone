package com.parabola.newtone.presentation.main.albums

import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.repository.TrackRepository
import io.reactivex.Completable
import io.reactivex.annotations.CheckReturnValue

class TabAlbumScreenUseCases(
    private val playerInteractor: PlayerInteractor,
    private val trackRepository: TrackRepository,
) {

    @CheckReturnValue
    fun shuffleAlbum(albumId: Int): Completable {
        return trackRepository.getByAlbum(albumId)
            .doOnSuccess(playerInteractor::startInShuffleMode)
            .ignoreElement()
    }

}
