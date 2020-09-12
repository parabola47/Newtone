package com.parabola.data.repository;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Audio.Media;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.parabola.domain.model.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Audio.AlbumColumns.ALBUM_ART;

final class AlbumArtExtractor {

    private final ContentResolver contentResolver;
    private final List<Track> tracks;
    private Size targetCoverSize;

    AlbumArtExtractor(ContentResolver contentResolver, List<Track> tracks) {
        this.contentResolver = contentResolver;
        this.tracks = tracks;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            targetCoverSize = new Size(700, 700);
        }
    }


    private final int cacheMaxSize = (int) (Runtime.getRuntime().maxMemory() / 8);
    private final LruCache<Integer, Bitmap> albumArtCache = new LruCache<Integer, Bitmap>(cacheMaxSize) {
        @Override
        protected int sizeOf(@NonNull Integer albumId, @NonNull Bitmap bitmap) {
            return bitmap.getByteCount();
        }
    };
    private final List<Integer> nullBitmapIds = new ArrayList<>();


    Bitmap getByAlbum(int albumId) {
        Bitmap cachedBitmap = albumArtCache.get(albumId);
        if (cachedBitmap != null)
            return cachedBitmap;

        if (nullBitmapIds.contains(albumId))
            return null;

        Bitmap result = extractAlbumBitmap(albumId);

        if (result != null) albumArtCache.put(albumId, result);
        else nullBitmapIds.add(albumId);

        return result;
    }

    private Bitmap extractAlbumBitmap(int albumId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return tracks.stream()
                    .filter(track -> track.getAlbumId() == albumId)
                    .map(Track::getId)
                    .map(trackId -> ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, trackId))
                    .flatMap(trackUri -> {
                        try {
                            return Stream.of(contentResolver.loadThumbnail(trackUri, targetCoverSize, null));
                        } catch (IOException e) {
                            return Stream.empty();
                        }
                    })
                    .findFirst()
                    .orElse(null);
        } else {
            String artLink = getAlbumArtLink(albumId);
            return BitmapFactory.decodeFile(artLink);
        }
    }

    //работает только в Android P и ниже
    private String getAlbumArtLink(int albumId) {
        try (Cursor cursor = contentResolver.query(
                Albums.EXTERNAL_CONTENT_URI,
                new String[]{ALBUM_ART},
                _ID + "=?",
                new String[]{String.valueOf(albumId)},
                null)) {

            if (cursor == null || !cursor.moveToFirst()) {
                return "";
            }

            return cursor.getString(0);
        }
    }

}
