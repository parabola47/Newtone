package com.parabola.domain.interactor;

import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.model.Album;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.ConsumerSingleObserver;

public final class AlbumInteractor {

    private final AlbumRepository albumRepo;
    private final TrackRepository trackRepo;
    private final PlayerInteractor playerInteractor;
    private final SortingRepository sortingRepo;

    public AlbumInteractor(AlbumRepository albumRepo,
                           TrackRepository trackRepo,
                           PlayerInteractor playerInteractor,
                           SortingRepository sortingRepo) {
        this.albumRepo = albumRepo;
        this.trackRepo = trackRepo;
        this.playerInteractor = playerInteractor;
        this.sortingRepo = sortingRepo;
    }

    public Single<List<Album>> getAll() {
        return albumRepo.getAll(sortingRepo.allAlbumsSorting());
    }

    public Single<List<Album>> getByArtist(int artistId) {
        return albumRepo.getByArtist(artistId, sortingRepo.artistAlbumsSorting());
    }

    public void shuffleAlbum(int albumId) {
        trackRepo.getByAlbum(albumId)
                .subscribe(new ConsumerSingleObserver<>(
                        playerInteractor::startShufflePlaying,
                        Functions.ERROR_CONSUMER));
    }
}
