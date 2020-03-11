package com.parabola.domain.repository;

import com.parabola.domain.interactor.type.Irrelevant;
import com.parabola.domain.model.Playlist;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface PlaylistRepository {

    Single<Playlist> getById(int playlistId);
    Single<List<Playlist>> getAll();

    //выбрасывает ошибку в Rx AlreadyExistsException,
    //если плейлист с таким именем уже существует
    Single<Playlist> addNew(String newPlaylistTitle);
    //выбрасывает ошибку в Rx AlreadyExistsException,
    //если плейлист с таким именем, за исключением переданного в playlistId уже существует
    Completable rename(int playlistId, String newTitle);
    Completable remove(int playlistId);

    Observable<Irrelevant> observePlaylistsUpdates();

    Completable addTrackToPlaylist(int playlistId, int trackId);
    Completable removeTrack(int playlistId, int trackId);
}
