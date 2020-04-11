package com.parabola.data.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Playlists.Members;

import com.parabola.data.model.PlaylistData;
import com.parabola.data.utils.RxCursorIterable;
import com.parabola.domain.exception.AlreadyExistsException;
import com.parabola.domain.exception.ItemNotFoundException;
import com.parabola.domain.interactor.observer.ConsumerObserver;
import com.parabola.domain.interactor.type.Irrelevant;
import com.parabola.domain.model.Playlist;
import com.parabola.domain.repository.PermissionHandler;
import com.parabola.domain.repository.PermissionHandler.Type;
import com.parabola.domain.repository.PlaylistRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.Audio.PlaylistsColumns.DATE_ADDED;
import static android.provider.MediaStore.Audio.PlaylistsColumns.NAME;

public final class PlaylistRepositoryImpl implements PlaylistRepository {

    private final ContentResolver contentResolver;
    private final PermissionHandler accessRepo;
    private final BehaviorSubject<Irrelevant> playlistsUpdates = BehaviorSubject.create();


    public PlaylistRepositoryImpl(ContentResolver contentResolver, PermissionHandler accessRepo) {
        this.contentResolver = contentResolver;
        this.accessRepo = accessRepo;

        this.accessRepo.observePermissionUpdates(Type.FILE_STORAGE)
                .subscribe(new ConsumerObserver<>(hasStorageAccess -> {
                    if (hasStorageAccess) {
                        playlistsUpdates.onNext(Irrelevant.INSTANCE);
                    }
                }));
    }


    private final String[] PLAYLIST_QUERY_SELECTIONS = new String[]{
            _ID,
            NAME,
            DATE_ADDED
    };


    @Override
    public Single<Playlist> getById(int playlistId) {
        return Single.fromCallable(() -> {
                    try (Cursor cursor = contentResolver.query(
                            EXTERNAL_CONTENT_URI,
                            PLAYLIST_QUERY_SELECTIONS,
                            _ID + "=?", new String[]{String.valueOf(playlistId)}, null)) {

                        if (cursor != null && cursor.moveToFirst()) {
                            return extractPlaylistFromCursor(cursor);
                        } else {
                            throw new ItemNotFoundException();
                        }
                    }
                }
        );
    }

    private List<Playlist> extractPlaylistsFromCursor(Cursor cursor) {
        if (cursor.getCount() == 0) {
            return Collections.emptyList();
        }
        cursor.moveToFirst();
        List<Playlist> result = new ArrayList<>(cursor.getCount());

        do {
            result.add(extractPlaylistFromCursor(cursor));
        } while (cursor.moveToNext());


        return result;
    }

    private Playlist extractPlaylistFromCursor(Cursor cursor) {
        PlaylistData playlist = new PlaylistData();

        playlist.id = cursor.getInt(cursor.getColumnIndexOrThrow(_ID));
        playlist.title = cursor.getString(cursor.getColumnIndexOrThrow(NAME));
        playlist.dateAddingTimestamp = cursor.getInt(cursor.getColumnIndexOrThrow(DATE_ADDED));
        playlist.playlistTracks = getPlaylistTrackItems(playlist.id);

        return playlist;
    }


    private List<Playlist.TrackItem> getPlaylistTrackItems(int playlistId) {
        Uri uri = Members.getContentUri("external", playlistId);
        String[] cols = new String[]{
                Members.AUDIO_ID,
        };

        try (Cursor cursor = contentResolver.query(uri, cols, null, null, null)) {
            return Observable.fromIterable(RxCursorIterable.from(cursor))
                    .map(c -> {
                        PlaylistData.TrackItemData trackItem = new PlaylistData.TrackItemData();
                        trackItem.trackId = c.getInt(c.getColumnIndexOrThrow(Members.AUDIO_ID));

                        return (Playlist.TrackItem) trackItem;
                    })
                    .toList()
                    .onErrorReturnItem(new ArrayList<>())
                    .blockingGet();
        }
    }


    @Override
    public Single<List<Playlist>> getAll() {
        if (!accessRepo.hasPermission(Type.FILE_STORAGE))
            return Single.just(Collections.emptyList());

        return Single.fromCallable(() ->
                contentResolver.query(
                        EXTERNAL_CONTENT_URI,
                        PLAYLIST_QUERY_SELECTIONS,
                        null, null, null))
                .doAfterSuccess(Cursor::close)
                .map(this::extractPlaylistsFromCursor);
    }


    @Override
    public Single<List<Playlist>> getByQuery(String query, int limit) {
        if (!accessRepo.hasPermission(Type.FILE_STORAGE))
            return Single.just(Collections.emptyList());

        String selection = NAME + " LIKE ?";

        Uri uri = EXTERNAL_CONTENT_URI.buildUpon()
                .appendQueryParameter("limit", String.valueOf(limit))
                .build();

        return Single.fromCallable(() ->
                contentResolver.query(
                        uri,
                        PLAYLIST_QUERY_SELECTIONS,
                        selection, new String[]{"%" + query + "%"}, null))
                .doAfterSuccess(Cursor::close)
                .map(this::extractPlaylistsFromCursor);
    }

    @Override
    public Single<Playlist> addNew(String newPlaylistTitle) {
        return Single.fromCallable(() -> containPlaylist(newPlaylistTitle))
                .flatMap(playlistAlreadyExists -> {
                    if (playlistAlreadyExists)
                        return Single.error(new AlreadyExistsException("Playlist with title '" + newPlaylistTitle + "' already exists"));
                    return Single.just(createPlaylistValues(newPlaylistTitle));
                })
                .map(contentValues -> contentResolver.insert(EXTERNAL_CONTENT_URI, contentValues))
                .map(Uri::getLastPathSegment)
                .map(Integer::valueOf)
                .doOnSuccess((integer) -> playlistsUpdates.onNext(Irrelevant.INSTANCE))
                .flatMap(this::getById);
    }

    @Override
    public Completable rename(int playlistId, String newTitle) {
        return Single
                .fromCallable(() -> {
                    String SELECTION = NAME + "=? AND " + _ID + "!=?";

                    try (Cursor cursor = contentResolver.query(EXTERNAL_CONTENT_URI, null,
                            SELECTION, new String[]{newTitle, String.valueOf(playlistId)}, // ищем есть ли плейлист с таким именем, не учитывая текущий
                            null)) {
                        return cursor != null && cursor.getCount() > 0;
                    }
                })
                .flatMapCompletable(playlistAlreadyExists -> {
                    if (playlistAlreadyExists) {
                        return Completable.error(new AlreadyExistsException("Playlist with title '" + newTitle + "' already exists"));
                    }
                    return Completable.fromAction(() -> {
                        ContentValues values = new ContentValues(1);
                        values.put(NAME, newTitle);

                        int updatedRowCounts = contentResolver.update(MediaStore.Audio.Playlists.getContentUri("external"),
                                values,
                                _ID + "=?", new String[]{String.valueOf(playlistId)});
                        if (updatedRowCounts > 0)
                            playlistsUpdates.onNext(Irrelevant.INSTANCE);
                    });

                });
    }

    @Override
    public Completable remove(int playlistId) {
        return Completable.fromAction(() -> {
            Uri uri = MediaStore.Audio.Playlists.getContentUri("external");

            int deletedRowCounts = contentResolver.delete(
                    uri,
                    _ID + "=?",
                    new String[]{String.valueOf(playlistId)});

            if (deletedRowCounts > 0)
                playlistsUpdates.onNext(Irrelevant.INSTANCE);
        });
    }

    @Override
    public Observable<Irrelevant> observePlaylistsUpdates() {
        return playlistsUpdates;
    }

    private ContentValues createPlaylistValues(String newPlaylistTitle) {
        ContentValues playlistValues = new ContentValues();

        playlistValues.put(NAME, newPlaylistTitle);
        playlistValues.put(DATE_ADDED, System.currentTimeMillis());
        playlistValues.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());

        return playlistValues;
    }


    private boolean containPlaylist(String playlistTitle) {
        String SELECTION_BY_NAME = NAME + "=?";

        try (Cursor cursor = contentResolver.query(EXTERNAL_CONTENT_URI, null,
                SELECTION_BY_NAME, new String[]{playlistTitle},
                null)) {

            return cursor != null && cursor.getCount() > 0;
        }
    }


    @Override
    public Completable addTrackToPlaylist(int playlistId, int trackId) {
        return Single
                .fromCallable(() -> {
                    String SELECTION = Members.AUDIO_ID + "=?";

                    try (Cursor cursor = contentResolver.query(Members.getContentUri("external", playlistId), null,
                            SELECTION, new String[]{String.valueOf(trackId)},
                            null)) {
                        return cursor != null && cursor.getCount() > 0;
                    }
                })
                .flatMapCompletable(trackAlreadyInPlaylist -> {
                    if (trackAlreadyInPlaylist) {
                        return Completable.complete();
                    }
                    return Completable.fromAction(() -> {
                        Uri uri = Members.getContentUri("external", playlistId);

                        try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                            int base = cursor != null ? cursor.getCount() : 0;

                            ContentValues values = new ContentValues();
                            values.put(Members.PLAY_ORDER, base + trackId);
                            values.put(Members.AUDIO_ID, trackId);

                            if (contentResolver.insert(uri, values) != null)
                                playlistsUpdates.onNext(Irrelevant.INSTANCE);
                        }
                    });
                });
    }


    @Override
    public Completable removeTrack(int playlistId, int trackId) {
        return Completable.fromAction(() -> {
            Uri uri = Members.getContentUri("external", playlistId);

            int deletedRowCounts = contentResolver.delete(
                    uri,
                    Members.AUDIO_ID + "=?",
                    new String[]{String.valueOf(trackId)});

            if (deletedRowCounts > 0)
                playlistsUpdates.onNext(Irrelevant.INSTANCE);
        });
    }
}
