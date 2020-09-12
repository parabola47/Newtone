package com.parabola.data.repository;

import com.parabola.domain.model.Album;
import com.parabola.domain.repository.AlbumRepository;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

import static com.parabola.data.utils.SortingUtil.getAlbumComparatorBySorting;

public final class AlbumRepositoryImpl implements AlbumRepository {
    private static final String LOG_TAG = AlbumRepositoryImpl.class.getSimpleName();


    private final DataExtractor dataExtractor;


    public AlbumRepositoryImpl(DataExtractor dataExtractor) {
        this.dataExtractor = dataExtractor;
    }


    @Override
    public Single<Album> getById(int albumId) {
        return Observable.fromIterable(dataExtractor.albums)
                .filter(album -> album.getId() == albumId)
                .firstOrError();
    }


    @Override
    public Single<List<Album>> getAll(Sorting sorting) {
        return Observable.fromIterable(dataExtractor.albums)
                .toSortedList(getAlbumComparatorBySorting(sorting));
    }


    @Override
    public Single<List<Album>> getByQuery(String query, int limit) {
        String validatedQuery = query.toLowerCase();

        return Observable.fromIterable(dataExtractor.albums)
                .filter(album -> (album.getArtistName() + " " + album.getTitle()).toLowerCase().contains(validatedQuery))
                .take(limit)
                .toList(limit);
    }


    @Override
    public Single<List<Album>> getByArtist(int artistId, Sorting sorting) {
        return Observable.fromIterable(dataExtractor.albums)
                .filter(album -> album.getArtistId() == artistId)
                .toSortedList(getAlbumComparatorBySorting(sorting));
    }

}
