package com.parabola.data.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.parabola.data.model.ArtistData;
import com.parabola.domain.model.Artist;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.PermissionHandler;
import com.parabola.domain.repository.PermissionHandler.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;

public final class ArtistRepositoryImpl implements ArtistRepository {
    private static final String TAG = ArtistRepositoryImpl.class.getSimpleName();

    private final ContentResolver contentResolver;
    private PermissionHandler accessRepo;


    public ArtistRepositoryImpl(ContentResolver contentResolver, PermissionHandler accessRepo) {
        this.contentResolver = contentResolver;
        this.accessRepo = accessRepo;
    }


    private final String[] ARTISTS_QUERY_SELECTIONS = new String[]{
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
    };


    private static final String SELECTION_BY_ID = MediaStore.Audio.Artists._ID + "=?";


    @Override
    public Single<Artist> getById(int artistId) {
        return Single.fromCallable(() ->
                contentResolver.query(
                        MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
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
                        MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
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
            case BY_NAME: return MediaStore.Audio.Artists.ARTIST + " COLLATE NOCASE";
            case BY_NAME_DESC: return MediaStore.Audio.Artists.ARTIST + " COLLATE NOCASE DESC";
            case BY_TRACKS_COUNT: return MediaStore.Audio.Artists.NUMBER_OF_TRACKS + ", " + MediaStore.Audio.Artists.ARTIST + " COLLATE NOCASE";
            case BY_TRACKS_COUNT_DESC: return MediaStore.Audio.Artists.NUMBER_OF_TRACKS + " DESC, " + MediaStore.Audio.Artists.ARTIST + " COLLATE NOCASE";
            default: return null;
        }
    }

    private List<Artist> extractArtists(Cursor cursor) {
        long start = System.currentTimeMillis();

        if (cursor.getCount() == 0) {
            return Collections.emptyList();
        }
        cursor.moveToFirst();

        List<Artist> result = new ArrayList<>(cursor.getCount());
        do {
            ArtistData artist = new ArtistData();

            artist.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists._ID));
            artist.name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
            artist.albumsCount = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS));
            artist.tracksCount = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));

            result.add(artist);
        } while (cursor.moveToNext());

        long end = System.currentTimeMillis();

        Log.d(TAG, "Extract " + result.size() + " artists for " + (end - start) + " ms");

        return result;
    }

}
