package com.parabola.data.repository;

import static com.parabola.data.utils.SortingUtil.getTrackComparatorBySorting;

import com.parabola.domain.interactor.type.Irrelevant;
import com.parabola.domain.model.Playlist;
import com.parabola.domain.model.Playlist.TrackItem;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.domain.repository.TrackRepository;

import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public final class TrackRepositoryImpl implements TrackRepository {

    private final PlaylistRepository playlistRepo;
    private final DataExtractor dataExtractor;
    private final FavouriteTrackHelper favouriteTrackHelper;


    public TrackRepositoryImpl(PlaylistRepository playlistRepo, DataExtractor dataExtractor) {
        this.playlistRepo = playlistRepo;
        this.dataExtractor = dataExtractor;
        this.favouriteTrackHelper = dataExtractor.favouriteTrackHelper;
    }


    @Override
    public Single<Track> getById(int trackId) {
        return Observable.fromIterable(dataExtractor.tracks)
                .filter(track -> track.getId() == trackId)
                .firstOrError();
    }

    @Override
    public Single<List<Track>> getByIds(List<Integer> trackIds) {
        return Observable.fromIterable(dataExtractor.tracks)
                .filter(track -> trackIds.contains(track.getId()))
                //  упорядочиваем список, чтобы он соответствовал переданным в качестве параметра идентификатораи
                .toSortedList(Comparator.comparingInt(t -> trackIds.indexOf(t.getId())));
    }

    @Override
    public Single<List<Track>> getByIds(List<Integer> trackIds, Sorting sorting) {
        return Observable.fromIterable(dataExtractor.tracks)
                .filter(track -> trackIds.contains(track.getId()))
                .toSortedList(getTrackComparatorBySorting(sorting));
    }


    @Override
    public boolean isExists(int trackId) {
        return dataExtractor.tracks.stream()
                .anyMatch(track -> track.getId() == trackId);
    }


    @Override
    public Single<List<Track>> getAll(Sorting sorting) {
        return Observable.fromIterable(dataExtractor.tracks)
                .toSortedList(getTrackComparatorBySorting(sorting));
    }

    @Override
    public Observable<Track> getAllAsObservable() {
        return Observable.fromIterable(dataExtractor.tracks);
    }


    private final PublishSubject<Integer> trackDeletingObserver = PublishSubject.create();

    @Override
    public Observable<Integer> observeTrackDeleting() {
        return trackDeletingObserver;
    }

    @Override
    public Single<Boolean> deleteTrack(int trackId) {
        return Single.fromCallable(() -> dataExtractor.deleteTrack(trackId))
                .doOnSuccess(isDeleted -> {
                    if (isDeleted) trackDeletingObserver.onNext(trackId);
                })
                .subscribeOn(Schedulers.io());
    }


    private Single<Boolean> removeFromRepository(int trackId) {
        return Single.fromCallable(() -> dataExtractor.removeFromRepository(trackId))
                .doOnSuccess(isDeleted -> {
                    if (isDeleted) trackDeletingObserver.onNext(trackId);
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public void trackDeletedInternally(int trackId) {
        removeFromRepository(trackId)
                .subscribe();
    }

    @Override
    public Single<List<Track>> getByAlbum(int albumId, Sorting sorting) {
        return Observable.fromIterable(dataExtractor.tracks)
                .filter(track -> track.getAlbumId() == albumId)
                .toSortedList(getTrackComparatorBySorting(sorting));
    }


    @Override
    public Single<List<Track>> getByArtist(int artistId, Sorting sorting) {
        return Observable.fromIterable(dataExtractor.tracks)
                .filter(track -> track.getArtistId() == artistId)
                .toSortedList(getTrackComparatorBySorting(sorting));
    }

    @Override
    public Single<List<Track>> getByPlaylist(int playlistId) {
        return playlistRepo.getById(playlistId)
                .map(Playlist::getPlaylistTracks)
                .flatMapObservable(Observable::fromIterable)
                .map(TrackItem::getTrackId)
                .toList()
                .flatMap(this::getByIds);
    }


    @Override
    public Single<List<Track>> getByFolder(String folderPath, Sorting sorting) {
        return Observable.fromIterable(dataExtractor.tracks)
                .filter(track -> track.getFolderPath().equals(folderPath))
                .toSortedList(getTrackComparatorBySorting(sorting));
    }


    @Override
    public Single<List<Track>> getFavourites() {
        return Single
                .fromCallable(favouriteTrackHelper::getFavourites)
                .flatMap(this::getByIds)
                //после извлечения избранных треков, необходимо проверить, существуют ли ещё эти треки
                .doAfterSuccess(favouriteTrackHelper::invalidateFavourites);
    }

    private final PublishSubject<Irrelevant> favoritesUpdates = PublishSubject.create();

    @Override
    public Observable<Irrelevant> observeFavouritesChanged() {
        return favoritesUpdates;
    }

    @Override
    public boolean isFavourite(int trackId) {
        return favouriteTrackHelper.isFavourite(trackId);
    }

    @Override
    public boolean toggleFavourite(int trackId) {
        boolean isFavourite = favouriteTrackHelper.isFavourite(trackId);

        if (isFavourite) removeFromFavourites(trackId);
        else addToFavourites(trackId);

        return !isFavourite;
    }

    @Override
    public void addToFavourites(int trackId) {
        favouriteTrackHelper.makeFavourite(trackId);
        favoritesUpdates.onNext(Irrelevant.INSTANCE);
    }

    @Override
    public void removeFromFavourites(int trackId) {
        favouriteTrackHelper.makeNotFavourite(trackId);
        favoritesUpdates.onNext(Irrelevant.INSTANCE);
    }

    @Override
    public void moveFavouriteTrack(int positionFrom, int positionTo) {
        if (positionFrom == positionTo)
            return;

        favouriteTrackHelper.moveFavouriteTrack(positionFrom, positionTo);
        favoritesUpdates.onNext(Irrelevant.INSTANCE);
    }

}
