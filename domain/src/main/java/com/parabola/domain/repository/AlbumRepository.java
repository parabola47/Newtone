package com.parabola.domain.repository;

import com.parabola.domain.model.Album;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface AlbumRepository {

    Single<Album> getById(int albumId);

    Single<List<Album>> getAll(Sorting sorting);
    default Single<List<Album>> getAll() {
        return getAll(null);
    }
    Observable<Album> getAllAsObservable();

    Single<List<Album>> getByArtist(int artistId, Sorting sorting);
    default Single<List<Album>> getByArtist(int artistId) {
        return getByArtist(artistId, null);
    }

    enum Sorting {
        BY_TITLE, BY_TITLE_DESC,
        BY_ARTIST, BY_ARTIST_DESC,
        BY_YEAR, BY_YEAR_DESC,
    }
}
