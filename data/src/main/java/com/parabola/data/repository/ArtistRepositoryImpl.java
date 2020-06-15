package com.parabola.data.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.parabola.data.model.ArtistData;
import com.parabola.domain.model.Artist;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.PermissionHandler;
import com.parabola.domain.repository.PermissionHandler.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Audio.ArtistColumns.ARTIST;
import static android.provider.MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS;
import static android.provider.MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS;
import static android.provider.MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;


public final class ArtistRepositoryImpl implements ArtistRepository {
    private static final String LOG_TAG = ArtistRepositoryImpl.class.getSimpleName();

    private final ContentResolver contentResolver;
    private PermissionHandler accessRepo;


    public ArtistRepositoryImpl(ContentResolver contentResolver, PermissionHandler accessRepo) {
        this.contentResolver = contentResolver;
        this.accessRepo = accessRepo;
    }


    private final String[] ARTISTS_QUERY_SELECTIONS = new String[]{
            _ID,
            ARTIST,
            NUMBER_OF_ALBUMS,
            NUMBER_OF_TRACKS,
    };


    private static final String SELECTION_BY_ID = _ID + "=?";


    @Override
    public Single<Artist> getById(int artistId) {
        return Single.fromCallable(() ->
                contentResolver.query(
                        EXTERNAL_CONTENT_URI,
                        ARTISTS_QUERY_SELECTIONS,
                        SELECTION_BY_ID,
                        new String[]{String.valueOf(artistId)},
                        null))
                .doAfterSuccess(Cursor::close)
                .map(cursor -> extractArtists(cursor).get(0));
    }


    @Override
    public Single<List<Artist>> getAll(Sorting sorting) {
        if (!accessRepo.hasPermission(Type.FILE_STORAGE)) {
            return Single.just(Collections.emptyList());
        }

        return Single.fromCallable(() ->
                contentResolver.query(
                        EXTERNAL_CONTENT_URI,
                        ARTISTS_QUERY_SELECTIONS,
                        null,
                        null,
                        mapDefaultSorting(sorting)))
                .doAfterSuccess(Cursor::close)
                .map(this::extractArtists);
    }

    private String mapDefaultSorting(ArtistRepository.Sorting defaultSorting) {
        if (defaultSorting == null) return null;

        switch (defaultSorting) {
            case BY_NAME: return ARTIST + " COLLATE NOCASE";
            case BY_NAME_DESC: return ARTIST + " COLLATE NOCASE DESC";
            case BY_TRACKS_COUNT: return NUMBER_OF_TRACKS + ", " + ARTIST + " COLLATE NOCASE";
            case BY_TRACKS_COUNT_DESC: return NUMBER_OF_TRACKS + " DESC, " + ARTIST + " COLLATE NOCASE";
            default: return null;
        }
    }

    @Override
    public Single<List<Artist>> getByQuery(String query, int limit) {
        if (!accessRepo.hasPermission(Type.FILE_STORAGE)) {
            return Single.just(Collections.emptyList());
        }

        String selection = ARTIST + " LIKE ?";

        Uri uri = EXTERNAL_CONTENT_URI.buildUpon()
                .appendQueryParameter("limit", String.valueOf(limit))
                .build();

        return Single.fromCallable(() ->
                contentResolver.query(
                        uri, ARTISTS_QUERY_SELECTIONS,
                        selection, new String[]{"%" + query + "%"}, null))
                .doAfterSuccess(Cursor::close)
                .map(this::extractArtists);
    }

    private List<Artist> extractArtists(Cursor cursor) {
        if (!cursor.moveToFirst()) {
            return Collections.emptyList();
        }

        List<Artist> result = new ArrayList<>(cursor.getCount());
        do {
            ArtistData artist = new ArtistData();

            artist.id = cursor.getInt(cursor.getColumnIndex(_ID));
            artist.name = cursor.getString(cursor.getColumnIndex(ARTIST));
            artist.albumsCount = cursor.getInt(cursor.getColumnIndex(NUMBER_OF_ALBUMS));
            artist.tracksCount = cursor.getInt(cursor.getColumnIndex(NUMBER_OF_TRACKS));

            result.add(artist);
        } while (cursor.moveToNext());

        return result;
    }

}
