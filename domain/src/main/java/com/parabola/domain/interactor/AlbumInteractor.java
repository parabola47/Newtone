package com.parabola.domain.interactor;

import com.parabola.domain.interactor.RepositoryInteractor.LoadingState;
import com.parabola.domain.model.Album;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.SortingRepository;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public final class AlbumInteractor {

    private final AlbumRepository albumRepo;
    private final RepositoryInteractor repositoryInteractor;
    private final SortingRepository sortingRepo;

    public AlbumInteractor(AlbumRepository albumRepo,
                           RepositoryInteractor repositoryInteractor,
                           SortingRepository sortingRepo) {
        this.albumRepo = albumRepo;
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

}
