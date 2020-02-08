package com.parabola.domain.interactors;

import com.parabola.domain.model.Album;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.SortingRepository;

import java.util.List;

import io.reactivex.Single;

public final class AlbumInteractor {
    private final AlbumRepository albumRepo;
    private final SortingRepository sortingRepo;

    public AlbumInteractor(AlbumRepository albumRepo, SortingRepository sortingRepo) {
        this.albumRepo = albumRepo;
        this.sortingRepo = sortingRepo;
    }

    public Single<List<Album>> getAll() {
        return albumRepo.getAll(sortingRepo.allAlbumsSorting());
    }

    public Single<List<Album>> getByArtist(int artistId) {
        return albumRepo.getByArtist(artistId, sortingRepo.artistAlbumsSorting());
    }
}
