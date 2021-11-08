package com.parabola.domain.repository;


import com.parabola.domain.interactor.type.Irrelevant;
import com.parabola.domain.model.Track;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface TrackRepository {

    //выбрасывает ошибку в Rx IllegalArgumentException,
    //если трек с таким иденитификатором не найден
    Single<Track> getById(int trackId);
    Single<List<Track>> getByIds(List<Integer> trackIds);
    Single<List<Track>> getByIds(List<Integer> trackIds, Sorting sorting);

    boolean isExists(int trackId);

    Single<List<Track>> getAll(Sorting sorting);
    default Single<List<Track>> getAll() {
        return getAll(null);
    }
    Observable<Track> getAllAsObservable();

    Observable<Integer> observeTrackDeleting(); //возвращает id удалённого трека
    Single<Boolean> deleteTrack(int trackId); //true, если удалось удалить файл

    Single<List<Track>> getByAlbum(int albumId, Sorting sorting);
    default Single<List<Track>> getByAlbum(int albumId) {
        return getByAlbum(albumId, null);
    }

    Single<List<Track>> getByArtist(int artistId, Sorting sorting);
    default Single<List<Track>> getByArtist(int artistId) {
        return getByArtist(artistId, null);
    }

    Single<List<Track>> getByPlaylist(int playlistId);

    Single<List<Track>> getByFolder(String folderPath, Sorting sorting);
    default Single<List<Track>> getByFolder(String folderPath) {
        return getByFolder(folderPath, null);
    }

    Single<List<Track>> getFavourites();

    //вызывается каждый раз после обновления списка избранных треков
    Observable<Irrelevant> observeFavouritesChanged();

    boolean isFavourite(int trackId);
    // Добавляет трек в список избранных, если он на текущий момент не находится в нём.
    // Если трек уже находится в списке избранных, то исключает его.
    // Вернёт true, если после выполнения трек стал избранным, иначе false
    boolean toggleFavourite(int trackId);
    void addToFavourites(int trackId);
    void removeFromFavourites(int trackId);
    void moveFavouriteTrack(int positionFrom, int positionTo);


    enum Sorting {
        BY_TITLE, BY_TITLE_DESC,
        BY_ARTIST, BY_ARTIST_DESC,
        BY_DURATION, BY_DURATION_DESC,
        BY_DATE_ADDING, BY_DATE_ADDING_DESC,
        BY_ALBUM_POSITION, BY_ALBUM_POSITION_DESC
    }
}
