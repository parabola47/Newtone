package com.parabola.domain.interactor;

import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.repository.TrackRepository;

import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.ConsumerSingleObserver;

public final class FolderInteractor {

    private final TrackRepository trackRepo;
    private final PlayerInteractor playerInteractor;

    public FolderInteractor(TrackRepository trackRepo, PlayerInteractor playerInteractor) {
        this.trackRepo = trackRepo;
        this.playerInteractor = playerInteractor;
    }


    public void shuffleFolder(String folderPath) {
        trackRepo.getByFolder(folderPath)
                .subscribe(new ConsumerSingleObserver<>(
                        playerInteractor::startInShuffleMode,
                        Functions.ERROR_CONSUMER));
    }

}
