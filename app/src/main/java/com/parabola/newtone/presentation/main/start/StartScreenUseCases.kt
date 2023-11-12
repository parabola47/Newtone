package com.parabola.newtone.presentation.main.start

import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.repository.TrackRepository
import io.reactivex.Completable
import io.reactivex.annotations.CheckReturnValue

class StartScreenUseCases(
    private val playerInteractor: PlayerInteractor,
    private val trackRepository: TrackRepository,
) {

    @CheckReturnValue
    fun shuffleAll(): Completable {
        return trackRepository.all
            .doOnSuccess(playerInteractor::startInShuffleMode)
            .ignoreElement()
    }

}
