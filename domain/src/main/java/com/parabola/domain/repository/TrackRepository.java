package com.parabola.domain.repository;


import com.parabola.domain.interactor.type.Irrelevant;
import com.parabola.domain.model.Track;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface TrackRepository {

    Single<Track> getById(int trackId);
    Single<List<Track>> getByIds(List<Integer> trackIds);
    Single<List<Track>> getByIds(List<Integer> trackIds, Sorting sorting);

    Single<List<Track>> getAll(Sorting sorting);
    default Single<List<Track>> getAll() {
        return getAll(null);
    }

    Observable<Integer> observeTrackDeleting(); //возвращает id удалённого трека
    void deleteTrack(int trackId);

    Single<List<Track>> getByAlbum(int albumId, Sorting sorting);
    default Single<List<Track>> getByAlbum(int albumId) {
        return getByAlbum(albumId, null);
    }

    Single<List<Track>> getByArtist(int artistId, Sorting sorting);
    default Single<List<Track>> getByArtist(int artistId) {
        return getByArtist(artistId, null);
    }

    Single<List<Track>> getByPlaylist(int playlistId);


    Single<List<Track>> getFavourites();

    //вызывается сразу и после обновления списка избранных треков
    Observable<Irrelevant> observeFavouritesChanged();
    //добавляет в список избранных, если раннее не был в нём, исключает если трек был в списке избранных
    // true, если после выполнения трек стал избранным, иначе false
    boolean toggleFavourite(int trackId);
    void addToFavourites(int trackId);
    void removeFromFavourites(int trackId);


    enum Sorting {
        BY_TITLE, BY_TITLE_DESC,
        BY_ARTIST, BY_ARTIST_DESC,
        BY_DURATION, BY_DURATION_DESC,
        BY_DATE_ADDING, BY_DATE_ADDING_DESC,
        BY_ALBUM_POSITION, BY_ALBUM_POSITION_DESC
    }
}
