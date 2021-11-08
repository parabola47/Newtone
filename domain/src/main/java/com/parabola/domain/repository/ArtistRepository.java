package com.parabola.domain.repository;

import com.parabola.domain.model.Artist;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface ArtistRepository {

    Single<Artist> getById(int artistId);
    Single<List<Artist>> getAll(Sorting sorting);
    default Single<List<Artist>> getAll() {
        return getAll(null);
    }
    Observable<Artist> getAllAsObservable();


    enum Sorting {
        BY_NAME, BY_NAME_DESC,
        BY_TRACKS_COUNT, BY_TRACKS_COUNT_DESC,
    }
}
