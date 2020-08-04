package com.parabola.domain.interactor;

import com.parabola.domain.interactor.RepositoryInteractor.LoadingState;
import com.parabola.domain.interactor.player.PlayerInteractor;
import com.parabola.domain.model.Album;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.observers.ConsumerSingleObserver;

public final class AlbumInteractor {

    private final AlbumRepository albumRepo;
    private final TrackRepository trackRepo;
    private final PlayerInteractor playerInteractor;
    private final RepositoryInteractor repositoryInteractor;
    private final SortingRepository sortingRepo;

    public AlbumInteractor(AlbumRepository albumRepo,
                           TrackRepository trackRepo,
                           PlayerInteractor playerInteractor,
                           RepositoryInteractor repositoryInteractor,
                           SortingRepository sortingRepo) {
        this.albumRepo = albumRepo;
        this.trackRepo = trackRepo;
        this.playerInteractor = playerInteractor;
        this.repositoryInteractor = repositoryInteractor;
        this.sortingRepo = sortingRepo;
    }


    public Observable<List<Album>> observeAllAlbumsUpdates() {
        return Observable.combineLatest(
                repositoryInteractor.observeLoadingState(),
                sortingRepo.observeAllAlbumsSorting(), (loadState, sorting) -> loadState)
                .flatMapSingle(repoLoadState -> {
                    if (repoLoadState != LoadingState.LOADED) {
                        return Single.just(Collections.emptyList());
                    }
                    return getAll();
                });
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
                        playerInteractor::startInShuffleMode,
                        Functions.ERROR_CONSUMER));
    }

}
