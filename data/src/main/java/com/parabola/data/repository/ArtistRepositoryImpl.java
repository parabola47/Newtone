package com.parabola.data.repository;

import com.parabola.domain.model.Artist;
import com.parabola.domain.repository.ArtistRepository;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

import static com.parabola.data.utils.SortingUtil.getArtistComparatorBySorting;


public final class ArtistRepositoryImpl implements ArtistRepository {
    private static final String LOG_TAG = ArtistRepositoryImpl.class.getSimpleName();

    private final DataExtractor dataExtractor;


    public ArtistRepositoryImpl(DataExtractor dataExtractor) {
        this.dataExtractor = dataExtractor;
    }


    @Override
    public Single<Artist> getById(int artistId) {
        return Observable.fromIterable(dataExtractor.artists)
                .filter(artist -> artist.getId() == artistId)
                .firstOrError();
    }


    @Override
    public Single<List<Artist>> getAll(Sorting sorting) {
        return Observable.fromIterable(dataExtractor.artists)
                .toSortedList(getArtistComparatorBySorting(sorting));
    }


    @Override
    public Single<List<Artist>> getByQuery(String query, int limit) {
        String validatedQuery = query.toLowerCase();

        return Observable.fromIterable(dataExtractor.artists)
                .filter(artist -> artist.getName().toLowerCase().contains(validatedQuery))
                .take(limit)
                .toList(limit);
    }

}
