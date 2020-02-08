package com.parabola.domain.interactors;

import com.parabola.domain.model.Artist;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.SortingRepository;

import java.util.List;

import io.reactivex.Single;

public final class ArtistInteractor {

    private final ArtistRepository artistRepo;
    private final SortingRepository sortingRepo;

    public ArtistInteractor(ArtistRepository artistRepo, SortingRepository sortingRepo) {
        this.artistRepo = artistRepo;
        this.sortingRepo = sortingRepo;
    }

    public Single<List<Artist>> getAll() {
        return artistRepo.getAll(sortingRepo.allArtistsSorting());
    }
}
