package com.parabola.data.repository;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.parabola.data.model.AlbumData;
import com.parabola.domain.model.Album;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.PermissionHandler;
import com.parabola.domain.repository.PermissionHandler.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import io.reactivex.Single;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Audio.AlbumColumns.ALBUM;
import static android.provider.MediaStore.Audio.AlbumColumns.ALBUM_ART;
import static android.provider.MediaStore.Audio.AlbumColumns.ARTIST;
import static android.provider.MediaStore.Audio.AlbumColumns.ARTIST_ID;
import static android.provider.MediaStore.Audio.AlbumColumns.LAST_YEAR;
import static android.provider.MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS;
import static android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

public final class AlbumRepositoryImpl implements AlbumRepository {
    private static final String LOG_TAG = AlbumRepositoryImpl.class.getSimpleName();

    private final ContentResolver contentResolver;
    private final PermissionHandler accessRepo;

    public AlbumRepositoryImpl(ContentResolver contentResolver, PermissionHandler accessRepo) {
        this.contentResolver = contentResolver;
        this.accessRepo = accessRepo;
    }


    private final String[] ALBUM_QUERY_SELECTIONS = new String[]{
            _ID,
            ARTIST_ID,
            ALBUM,
            LAST_YEAR,
            NUMBER_OF_SONGS,
            ARTIST,
    };


    private final Function<AlbumData, Bitmap> getArtFunction = albumData -> getArtImage(albumData.id);

    private static final String SELECTION_BY_ID = _ID + "=?";


    @Override
    public Single<Album> getById(int albumId) {
        return Single.fromCallable(() ->
                contentResolver.query(
                        EXTERNAL_CONTENT_URI,
                        ALBUM_QUERY_SELECTIONS,
                        SELECTION_BY_ID,
                        new String[]{String.valueOf(albumId)},
                        null))
                .doAfterSuccess(Cursor::close)
                .map(cursor -> extractAlbums(cursor).get(0));
    }


    @Override
    public Single<List<Album>> getAll(Sorting sorting) {
        if (!accessRepo.hasPermission(Type.FILE_STORAGE)) {
            return Single.just(Collections.emptyList());
        }

        return Single.fromCallable(() ->
                contentResolver.query(
                        EXTERNAL_CONTENT_URI,
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
    public Single<List<Album>> getByQuery(String query, int limit) {
        if (!accessRepo.hasPermission(Type.FILE_STORAGE)) {
            return Single.just(Collections.emptyList());
        }

        String selection = ALBUM + " LIKE ?";

        Uri uri = EXTERNAL_CONTENT_URI.buildUpon()
                .appendQueryParameter("limit", String.valueOf(limit))
                .build();

        return Single.fromCallable(() ->
                contentResolver.query(
                        uri, ALBUM_QUERY_SELECTIONS,
                        selection, new String[]{"%" + query + "%"}, null))
                .doAfterSuccess(Cursor::close)
                .map(this::extractAlbums);
    }

    @Override
    public Single<List<Album>> getByArtist(int artistId, Sorting sorting) {
        return Single.fromCallable(() ->
                contentResolver.query(
                        EXTERNAL_CONTENT_URI,
                        ALBUM_QUERY_SELECTIONS,
                        ARTIST_ID + "=?",
                        new String[]{String.valueOf(artistId)},
                        mapDefaultSorting(sorting)))
                .doAfterSuccess(Cursor::close)
                .map(this::extractAlbums);
    }

    private final int cacheMaxSize = (int) (Runtime.getRuntime().maxMemory() / 8);
    private final LruCache<Integer, Bitmap> albumArtCache = new LruCache<Integer, Bitmap>(cacheMaxSize) {
        @Override
        protected int sizeOf(@NonNull Integer albumId, @NonNull Bitmap bitmap) {
            return bitmap.getByteCount();
        }
    };
    private final List<Integer> nullBitmapIds = new ArrayList<>();


    @Override
    public Bitmap getArtImage(int albumId) {
        Bitmap cachedBitmap = albumArtCache.get(albumId);
        if (cachedBitmap != null)
            return cachedBitmap;

        if (nullBitmapIds.contains(albumId))
            return null;

        Bitmap result = extractBitmap(albumId);

        if (result != null) albumArtCache.put(albumId, result);
        else nullBitmapIds.add(albumId);

        return result;
    }

    private Bitmap extractBitmap(int albumId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                Uri uri = ContentUris.withAppendedId(EXTERNAL_CONTENT_URI, albumId);
                Size size = new Size(700, 700);

                return contentResolver
                        .loadThumbnail(uri, size, null);
            } catch (IOException ex) {
                return null;
            }
        } else {
            String artLink = getArtLink(albumId);
            return BitmapFactory.decodeFile(artLink);
        }
    }

    //работает только в Android P и ниже
    private String getArtLink(int albumId) {
        try (Cursor cursor = contentResolver.query(
                EXTERNAL_CONTENT_URI,
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
        if (!cursor.moveToFirst()) {
            return Collections.emptyList();
        }

        List<Album> result = new ArrayList<>(cursor.getCount());
        do {
            AlbumData album = new AlbumData();

            album.id = cursor.getInt(cursor.getColumnIndex(_ID));
            album.title = cursor.getString(cursor.getColumnIndex(ALBUM));
            album.artistId = cursor.getInt(cursor.getColumnIndex(ARTIST_ID));
            album.getArtFunction = this.getArtFunction;
            album.year = cursor.getInt(cursor.getColumnIndex(LAST_YEAR));
            album.tracksCount = cursor.getInt(cursor.getColumnIndex(NUMBER_OF_SONGS));
            album.artistName = cursor.getString(cursor.getColumnIndex(ARTIST));

            result.add(album);
        } while (cursor.moveToNext());

        return result;
    }

}
