package com.parabola.data.repository;

import static com.parabola.data.utils.SortingUtil.getAlbumComparatorBySorting;

import com.parabola.domain.model.Album;
import com.parabola.domain.repository.AlbumRepository;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

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
    public Observable<Album> getAllAsObservable() {
        return Observable.fromIterable(dataExtractor.albums);
    }


    @Override
    public Single<List<Album>> getByArtist(int artistId, Sorting sorting) {
        return Observable.fromIterable(dataExtractor.albums)
                .filter(album -> album.getArtistId() == artistId)
                .toSortedList(getAlbumComparatorBySorting(sorting));
    }

}
