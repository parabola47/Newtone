package com.parabola.domain.interactor;

import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.model.Artist;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.ConsumerSingleObserver;

public final class ArtistInteractor {

    private final ArtistRepository artistRepo;
    private final TrackRepository trackRepo;
    private final PlayerInteractor playerInteractor;
    private final SortingRepository sortingRepo;

    public ArtistInteractor(ArtistRepository artistRepo,
                            TrackRepository trackRepo,
                            PlayerInteractor playerInteractor,
                            SortingRepository sortingRepo) {
        this.artistRepo = artistRepo;
        this.trackRepo = trackRepo;
        this.playerInteractor = playerInteractor;
        this.sortingRepo = sortingRepo;
    }

    public Single<List<Artist>> getAll() {
        return artistRepo.getAll(sortingRepo.allArtistsSorting());
    }

    public void shuffleArtist(int artistId) {
        trackRepo.getByArtist(artistId)
                .subscribe(new ConsumerSingleObserver<>(
                        playerInteractor::startInShuffleMode,
                        Functions.ERROR_CONSUMER));
    }

}
