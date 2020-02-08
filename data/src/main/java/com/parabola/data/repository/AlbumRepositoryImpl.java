package com.parabola.data.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.parabola.data.model.AlbumData;
import com.parabola.domain.model.Album;
import com.parabola.domain.repository.AccessRepository;
import com.parabola.domain.repository.AccessRepository.AccessType;
import com.parabola.domain.repository.AlbumRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Audio.AlbumColumns.ALBUM;
import static android.provider.MediaStore.Audio.AlbumColumns.ALBUM_ART;
import static android.provider.MediaStore.Audio.AlbumColumns.ARTIST;
import static android.provider.MediaStore.Audio.AlbumColumns.ARTIST_ID;
import static android.provider.MediaStore.Audio.AlbumColumns.LAST_YEAR;
import static android.provider.MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS;

public final class AlbumRepositoryImpl implements AlbumRepository {
    private static final String TAG = AlbumRepositoryImpl.class.getSimpleName();

    private final ContentResolver contentResolver;
    private final AccessRepository accessRepo;

    public AlbumRepositoryImpl(ContentResolver contentResolver, AccessRepository accessRepo) {
        this.contentResolver = contentResolver;
        this.accessRepo = accessRepo;
    }


    private final String[] ALBUM_QUERY_SELECTIONS = new String[]{
            _ID,
            ARTIST_ID,
            ALBUM,
            ALBUM_ART,
            LAST_YEAR,
            NUMBER_OF_SONGS,
            ARTIST,
    };

    private static final String SELECTION_BY_ID = _ID + "=?";


    @Override
    public Single<Album> getById(int albumId) {
        return Single.fromCallable(() ->
                contentResolver.query(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        ALBUM_QUERY_SELECTIONS,
                        SELECTION_BY_ID,
                        new String[]{String.valueOf(albumId)},
                        null))
                .doAfterSuccess(Cursor::close)
                .map(cursor -> extractAlbums(cursor).get(0));
    }


    @Override
    public Single<List<Album>> getAll(Sorting sorting) {
        if (!accessRepo.hasAccess(AccessType.FILE_STORAGE)) {
            return Single.just(Collections.emptyList());
        }

        return Single.fromCallable(() ->
                contentResolver.query(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        ALBUM_QUERY_SELECTIONS,
                        null,
                        null,
                        mapDefaultSorting(sorting)))
                .doAfterSuccess(Cursor::close)
                .map(this::extractAlbums);
    }

    private String mapDefaultSorting(Sorting defaultSorting) {
        if (defaultSorting == null) return null;
        switch (defaultSorting) {
            case BY_TITLE: return ALBUM + " COLLATE NOCASE";
            case BY_TITLE_DESC: return ALBUM + " COLLATE NOCASE DESC";
            case BY_YEAR: return LAST_YEAR + ", " + ALBUM + " COLLATE NOCASE";
            case BY_YEAR_DESC: return LAST_YEAR + " DESC, " + ALBUM + " COLLATE NOCASE";
            case BY_ARTIST: return ARTIST + " COLLATE NOCASE, " + ALBUM + " COLLATE NOCASE";
            case BY_ARTIST_DESC: return ARTIST + " COLLATE NOCASE DESC, " + ALBUM + " COLLATE NOCASE";
            default: return null;
        }
    }


    @Override
    public Single<List<Album>> getByArtist(int artistId, Sorting sorting) {
        return Single.fromCallable(() ->
                contentResolver.query(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        ALBUM_QUERY_SELECTIONS,
                        ARTIST_ID + "=?",
                        new String[]{String.valueOf(artistId)},
                        mapDefaultSorting(sorting)))
                .doAfterSuccess(Cursor::close)
                .map(this::extractAlbums);
    }

    @Override
    public String getArtLink(int albumId) {
        try (Cursor cursor = contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{ALBUM_ART},
                SELECTION_BY_ID,
                new String[]{String.valueOf(albumId)},
                null)) {

            if (cursor == null || !cursor.moveToFirst()) {
                return "";
            }

            return cursor.getString(0);
        }
    }

    private List<Album> extractAlbums(Cursor cursor) {
        long start = System.currentTimeMillis();
        if (cursor.getCount() == 0) {
            return Collections.emptyList();
        }
        cursor.moveToFirst();

        List<Album> result = new ArrayList<>(cursor.getCount());
        do {
            AlbumData album = new AlbumData();

            album.id = cursor.getInt(cursor.getColumnIndex(_ID));
            album.title = cursor.getString(cursor.getColumnIndex(ALBUM));
            album.artistId = cursor.getInt(cursor.getColumnIndex(ARTIST_ID));
            album.artLink = cursor.getString(cursor.getColumnIndex(ALBUM_ART));
            album.year = cursor.getInt(cursor.getColumnIndex(LAST_YEAR));
            album.tracksCount = cursor.getInt(cursor.getColumnIndex(NUMBER_OF_SONGS));
            album.artistName = cursor.getString(cursor.getColumnIndex(ARTIST));

            result.add(album);
        } while (cursor.moveToNext());

        long end = System.currentTimeMillis();

        Log.d(TAG, "Extract " + result.size() + " albums for " + (end - start) + " ms");

        return result;
    }

}
