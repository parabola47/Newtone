package com.parabola.domain.interactor;

import com.parabola.domain.interactor.RepositoryInteractor.LoadingState;
import com.parabola.domain.model.Artist;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.SortingRepository;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public final class ArtistInteractor {

    private final ArtistRepository artistRepo;
    private final RepositoryInteractor repositoryInteractor;
    private final SortingRepository sortingRepo;


    public ArtistInteractor(ArtistRepository artistRepo,
                            RepositoryInteractor repositoryInteractor,
                            SortingRepository sortingRepo) {
        this.artistRepo = artistRepo;
        this.repositoryInteractor = repositoryInteractor;
        this.sortingRepo = sortingRepo;
    }


    public Observable<List<Artist>> observeAllArtistsUpdates() {
        return Observable.combineLatest(
                repositoryInteractor.observeLoadingState(),
                sortingRepo.observeAllArtistsSorting(), (loadState, sorting) -> loadState)
                .flatMapSingle(repoLoadState -> {
                    if (repoLoadState != LoadingState.LOADED) {
                        return Single.just(Collections.emptyList());
                    }
                    return getAll();
                });
    }


    public Single<List<Artist>> getAll() {
        return artistRepo.getAll(sortingRepo.allArtistsSorting());
    }

}
