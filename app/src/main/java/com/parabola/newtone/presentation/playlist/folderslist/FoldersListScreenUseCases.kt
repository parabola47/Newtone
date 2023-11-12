package com.parabola.newtone.presentation.playlist.folderslist

import com.parabola.domain.interactor.player.PlayerInteractor
import com.parabola.domain.repository.TrackRepository
import io.reactivex.Completable
import io.reactivex.annotations.CheckReturnValue

class FoldersListScreenUseCases(
    private val trackRepository: TrackRepository,
    private val playerInteractor: PlayerInteractor,
) {

    @CheckReturnValue
    fun shuffleFolder(folderPath: String): Completable {
        return trackRepository.getByFolder(folderPath)
            .doOnSuccess(playerInteractor::startInShuffleMode)
            .ignoreElement()
    }

}
