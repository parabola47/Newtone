package com.parabola.data.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Playlists.Members;

import com.parabola.data.model.PlaylistData;
import com.parabola.domain.exception.AlreadyExistsException;
import com.parabola.domain.exception.ItemNotFoundException;
import com.parabola.domain.interactor.RepositoryInteractor;
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
    private static final String LOG_TAG = PlaylistRepositoryImpl.class.getSimpleName();

    private final DataExtractor dataExtractor;
    private final ContentResolver contentResolver;
    private final PermissionHandler accessRepo;
    private final BehaviorSubject<Irrelevant> playlistsUpdates = BehaviorSubject.create();


    public PlaylistRepositoryImpl(DataExtractor dataExtractor,
                                  ContentResolver contentResolver, PermissionHandler accessRepo) {
        this.dataExtractor = dataExtractor;
        this.contentResolver = contentResolver;
        this.accessRepo = accessRepo;

        Observable.combineLatest(
                this.accessRepo.observePermissionUpdates(Type.FILE_STORAGE), this.dataExtractor.observeLoadingState(),
                //обновление необходимо если есть доступ к файловому хранилищу и если репозиторий прогружен
                (hasStorageAccess, loadingState) -> hasStorageAccess && loadingState == RepositoryInteractor.LoadingState.LOADED)
                .subscribe(new ConsumerObserver<>(needToUpdate -> {
                    if (needToUpdate) {
                        playlistsUpdates.onNext(Irrelevant.INSTANCE);
                    }
                }));
    }


    private final String[] PLAYLIST_QUERY_SELECTIONS = new String[]{_ID, NAME};


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
        if (!cursor.moveToFirst()) {
            return Collections.emptyList();
        }
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
        playlist.playlistTracks = getPlaylistTrackItems(playlist.id);

        return playlist;
    }


    private List<Playlist.TrackItem> getPlaylistTrackItems(int playlistId) {
        Uri playlistUri = Members.getContentUri("external", playlistId);
        String[] selectedColumns = new String[]{Members.AUDIO_ID};

        try (Cursor cursor = contentResolver.query(playlistUri, selectedColumns, null, null, Members.PLAY_ORDER)) {
            if (cursor == null || !cursor.moveToFirst())
                return Collections.emptyList();

            List<Playlist.TrackItem> trackItems = new ArrayList<>();
            do {
                PlaylistData.TrackItemData trackItem = new PlaylistData.TrackItemData();
                trackItem.trackId = cursor.getInt(0);

                boolean considerThisTrack = dataExtractor.tracks.stream()
                        .anyMatch(track -> track.getId() == trackItem.trackId);

                if (considerThisTrack)
                    trackItems.add(trackItem);
            } while (cursor.moveToNext());

            return Collections.unmodifiableList(trackItems);
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
                        null, null, DATE_ADDED + " DESC"))
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
                    uri, _ID + "=?",
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
        try (Cursor cursor = contentResolver.query(EXTERNAL_CONTENT_URI, null,
                NAME + "=?", new String[]{playlistTitle}, null)) {

            return cursor != null && cursor.getCount() > 0;
        }
    }


    @Override
    public Completable addTracksToPlaylist(int playlistId, int... trackIds) {
        Uri playlistUri = Members.getContentUri("external", playlistId);

        return Single
                //если трек уже присутвовал в плейлисте, то он игнорируется
                .fromCallable(() -> discardTracksThatPlaylistHas(playlistUri, trackIds))
                .map(insertTrackIds -> {
                    int nextTrackPlayOrder = getPlaylistNextTrackPlayOrder(playlistUri);

                    ContentValues[] valuesArray = new ContentValues[insertTrackIds.size()];
                    for (int i = 0; i < insertTrackIds.size(); i++, nextTrackPlayOrder++) {
                        ContentValues values = new ContentValues(2);
                        values.put(Members.PLAY_ORDER, nextTrackPlayOrder);
                        values.put(Members.AUDIO_ID, insertTrackIds.get(i));
                        valuesArray[i] = values;
                    }

                    return contentResolver.bulkInsert(playlistUri, valuesArray);
                })
                .flatMapCompletable(insertedTracksCount -> {
                    if (insertedTracksCount == 0)
                        return Completable.complete();
                    return Completable.fromAction(() -> playlistsUpdates.onNext(Irrelevant.INSTANCE));
                });
    }


    private int getPlaylistNextTrackPlayOrder(Uri playlistUri) {
        try (Cursor cursor = contentResolver.query(playlistUri, new String[]{Members.PLAY_ORDER},
                null, null, Members.PLAY_ORDER + " DESC LIMIT 1")) {
            if (cursor != null && cursor.moveToFirst())
                return cursor.getInt(0) + 1;
            else return 0;
        }
    }


    private List<Integer> discardTracksThatPlaylistHas(Uri playlistUri, int... trackIds) {
        List<Integer> newTrackIds = new ArrayList<>(trackIds.length);
        for (int tracksId : trackIds) {
            newTrackIds.add(tracksId);
        }

        try (Cursor cursor = contentResolver.query(
                playlistUri,
                new String[]{Members.AUDIO_ID},
                Members.AUDIO_ID + " IN (" + idsToString(trackIds) + ")",
                null, null)) {
            if (cursor == null || !cursor.moveToFirst())
                return newTrackIds;

            do {
                newTrackIds.remove((Integer) cursor.getInt(0));
            } while (cursor.moveToNext());

            return newTrackIds;
        }
    }

    private String idsToString(int... ids) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < ids.length; i++) {
            builder.append(ids[i]);
            if (i != ids.length - 1)
                builder.append(',');
        }

        return builder.toString();
    }

    @Override
    public Completable removeTrack(int playlistId, int trackId) {
        return Completable.fromAction(() -> {
            Uri playlistUri = Members.getContentUri("external", playlistId);

            int deletedRowCounts = contentResolver.delete(
                    playlistUri,
                    Members.AUDIO_ID + "=?",
                    new String[]{String.valueOf(trackId)});

            if (deletedRowCounts > 0)
                playlistsUpdates.onNext(Irrelevant.INSTANCE);
        });
    }

    @Override
    public Completable moveTrack(int playlistId, int oldPosition, int newPosition) {
        if (oldPosition == newPosition)
            return Completable.complete();

        return Completable.fromAction(() -> {
            boolean isMoved = Members.moveItem(contentResolver, playlistId, oldPosition, newPosition);
            if (isMoved)
                playlistsUpdates.onNext(Irrelevant.INSTANCE);
        });
    }

}
