package com.parabola.data.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.provider.MediaStore;
import android.util.Log;

import com.parabola.data.model.TrackData;
import com.parabola.domain.interactors.type.Irrelevant;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.AccessRepository;
import com.parabola.domain.repository.AccessRepository.AccessType;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.PlaylistRepository;
import com.parabola.domain.repository.TrackRepository;
import com.parabola.domain.utils.StringTool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import java8.util.Comparators;
import java8.util.function.Function;
import java8.util.function.Predicate;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Audio.AudioColumns.ALBUM;
import static android.provider.MediaStore.Audio.AudioColumns.ALBUM_ID;
import static android.provider.MediaStore.Audio.AudioColumns.ARTIST;
import static android.provider.MediaStore.Audio.AudioColumns.ARTIST_ID;
import static android.provider.MediaStore.Audio.AudioColumns.TRACK;
import static android.provider.MediaStore.Audio.AudioColumns.YEAR;
import static android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.DURATION;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.TITLE;

public final class TrackRepositoryImpl implements TrackRepository {
    private static final String TAG = TrackRepositoryImpl.class.getSimpleName();

    private final ContentResolver contentResolver;
    private final AlbumRepository albumRepo;
    private final PlaylistRepository playlistRepo;
    private AccessRepository accessRepo;

    private final FavouriteTrackHelper favouriteTrackHelper;

    private static final String TRACK_REPOSITORY_SHARED_PREFS_NAME = "TRACK_REPOSITORY_SHARED_PREFS";


    public TrackRepositoryImpl(Context context, AlbumRepository albumRepo, PlaylistRepository playlistRepo, AccessRepository accessRepo) {
        this.contentResolver = context.getContentResolver();
        this.albumRepo = albumRepo;
        this.playlistRepo = playlistRepo;
        this.accessRepo = accessRepo;

        SharedPreferences trackReposPrefs = context.getSharedPreferences(TRACK_REPOSITORY_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        favouriteTrackHelper = new FavouriteTrackHelper(trackReposPrefs);
    }


    private final String[] TRACK_QUERY_SELECTIONS = new String[]{
            _ID,
            DATA,
            TITLE,
            DATE_ADDED,
            ALBUM_ID,
            ALBUM,
            ARTIST_ID,
            ARTIST,
            DURATION,
            TRACK,
            YEAR,
            SIZE
    };


    private static final String SELECTION_NON_HIDDEN_MUSIC = MediaStore.Audio.Media.IS_MUSIC + "=1";

    private static final String SELECTION_MUSIC_BY_ALBUM_ID = SELECTION_NON_HIDDEN_MUSIC + " AND " + ALBUM_ID + "=?";
    private static final String SELECTION_MUSIC_BY_ARTIST_ID = SELECTION_NON_HIDDEN_MUSIC + " AND " + ARTIST_ID + "=?";


    @Override
    public Single<Track> getById(int trackId) {
        return Single.fromCallable(() ->
                contentResolver.query(
                        EXTERNAL_CONTENT_URI,
                        TRACK_QUERY_SELECTIONS,
                        _ID + "=?",
                        new String[]{String.valueOf(trackId)},
                        null))
                .doAfterSuccess(Cursor::close)
                .map(cursor -> extractTracks(cursor).get(0));
    }

    @Override
    public Single<List<Track>> getByIds(List<Integer> trackIds) {
        return getByIds(trackIds, null)
                //  упорядочиваем список, чтобы он соответствовал переданным в качестве параметра идентификатораи
                .map(tracks -> {
                    Collections.sort(tracks, (t1, t2) -> Integer.compare(trackIds.indexOf(t1.getId()), trackIds.indexOf(t2.getId())));
                    return tracks;
                });
    }

    @Override
    public Single<List<Track>> getByIds(List<Integer> trackIds, Sorting sorting) {
        if (!accessRepo.hasAccess(AccessType.FILE_STORAGE))
            return Single.just(Collections.emptyList());


        return Single.fromCallable(() ->
                contentResolver.query(
                        EXTERNAL_CONTENT_URI,
                        TRACK_QUERY_SELECTIONS,
                        _ID + " IN (" + StringTool.makeQueryPlaceholders(trackIds.size()) + ")",
                        toStringArray(trackIds),
                        mapDefaultSorting(sorting)))
                .doAfterSuccess(Cursor::close)
                .map(this::extractTracks);
    }

    private String[] toStringArray(List<Integer> from) {
        String[] result = new String[from.size()];
        for (int i = 0; i < from.size(); i++) {
            result[i] = String.valueOf(from.get(i));
        }
        return result;
    }

    @Override
    public Single<List<Track>> getAll(Sorting sorting) {
        if (!accessRepo.hasAccess(AccessType.FILE_STORAGE))
            return Single.just(Collections.emptyList());


        return Single.fromCallable(() ->
                contentResolver.query(
                        EXTERNAL_CONTENT_URI,
                        TRACK_QUERY_SELECTIONS,
                        SELECTION_NON_HIDDEN_MUSIC, null, mapDefaultSorting(sorting)))
                .doAfterSuccess(Cursor::close)
                .map(this::extractTracks);
    }

    private String mapDefaultSorting(Sorting defaultSorting) {
        if (defaultSorting == null) return null;
        switch (defaultSorting) {
            case BY_TITLE: return TITLE;
            case BY_TITLE_DESC: return TITLE + " DESC";
            case BY_ARTIST: return ARTIST + ", " + TITLE;
            case BY_ARTIST_DESC: return ARTIST + " DESC, " + TITLE;
            case BY_DURATION: return DURATION;
            case BY_DURATION_DESC: return DURATION + " DESC";
            case BY_DATE_ADDING: return DATE_ADDED;
            case BY_DATE_ADDING_DESC: return DATE_ADDED + " DESC";
            case BY_ALBUM_POSITION: return "CAST(" + TRACK + " AS int)";
            case BY_ALBUM_POSITION_DESC: return "CAST(" + TRACK + " AS int) DESC";
            default: return null;
        }
    }

    private final Predicate<TrackData> isFavouriteCondition = new Predicate<TrackData>() {
        public boolean test(TrackData trackData) {
            return favouriteTrackHelper.isFavourite(trackData.getId());
        }
    };
    private final Function<TrackData, Long> favouriteTimestampFunction = new Function<TrackData, Long>() {
        public Long apply(TrackData trackData) {
            return favouriteTrackHelper.getFavouriteTimeStamp(trackData.getId());
        }
    };
    private final Function<TrackData, Bitmap> getArtFunction = new Function<TrackData, Bitmap>() {
        public Bitmap apply(TrackData trackData) {
            return (Bitmap) albumRepo.getArtImage(trackData.albumId);
        }
    };
    private final Function<TrackData, Integer> getGenreIdFunction = new Function<TrackData, Integer>() {
        public Integer apply(TrackData trackData) {
            String[] genresProjection = {_ID};

            Cursor cursor = contentResolver.query(
                    MediaStore.Audio.Genres.getContentUriForAudioId("external", trackData.id),
                    genresProjection, null, null, null);
            if (Objects.requireNonNull(cursor).getCount() == 0)
                return 0;

            cursor.moveToFirst();
            int genreId = cursor.getInt(0);
            cursor.close();

            return genreId;
        }
    };
    private final Function<TrackData, String> getGenreNameFunction = new Function<TrackData, String>() {
        public String apply(TrackData trackData) {
            Cursor cursor = contentResolver.query(
                    MediaStore.Audio.Genres.getContentUri("external"),
                    new String[]{MediaStore.Audio.GenresColumns.NAME},
                    _ID + "=?",
                    new String[]{String.valueOf(trackData.getGenreId())}, null);
            if (Objects.requireNonNull(cursor).getCount() == 0)
                return "";
            cursor.moveToFirst();
            String genreName = cursor.getString(0);
            cursor.close();

            return genreName;
        }
    };
    private final Function<TrackData, Integer> getBitrateFunction = trackData -> {
        try {
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(trackData.filePath);

            return mediaExtractor.getTrackFormat(0)
                    .getInteger(MediaFormat.KEY_BIT_RATE) / 1000;
        } catch (IOException e) {
            return 0;
        }
    };
    private final Function<TrackData, Integer> getSampleRateFunction = track -> {
        try {
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(track.filePath);

            return mediaExtractor.getTrackFormat(0)
                    .getInteger(MediaFormat.KEY_SAMPLE_RATE);
        } catch (IOException e) {
            return 0;
        }
    };

    private List<Track> extractTracks(Cursor cursor) {
        long start = System.currentTimeMillis();
        if (cursor.getCount() == 0) {
            return Collections.emptyList();
        }
        cursor.moveToFirst();

        List<Track> result = new ArrayList<>(cursor.getCount());
        do {
            TrackData track = new TrackData();

            track.id = cursor.getInt(cursor.getColumnIndexOrThrow(_ID));
            track.title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
            track.dateAddingTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DATE_ADDED));
            track.albumId = cursor.getInt(cursor.getColumnIndexOrThrow(ALBUM_ID));
            track.albumTitle = cursor.getString(cursor.getColumnIndexOrThrow(ALBUM));
            track.artistId = cursor.getInt(cursor.getColumnIndexOrThrow(ARTIST_ID));
            track.artistName = cursor.getString(cursor.getColumnIndexOrThrow(ARTIST));
            track.durationMs = cursor.getLong(cursor.getColumnIndexOrThrow(DURATION));
            track.filePath = cursor.getString(cursor.getColumnIndexOrThrow(DATA));
            track.year = cursor.getInt(cursor.getColumnIndexOrThrow(YEAR));
            track.positionInCd = cursor.getInt(cursor.getColumnIndexOrThrow(TRACK));
            track.fileSize = cursor.getInt(cursor.getColumnIndexOrThrow(SIZE));
            track.isFavouriteCondition = this.isFavouriteCondition;
            track.favouriteTimeStampFunction = this.favouriteTimestampFunction;
            track.getArtFunction = this.getArtFunction;
            track.getGenreIdFunction = this.getGenreIdFunction;
            track.getGenreNameFunction = this.getGenreNameFunction;
            track.getBitrateFunction = this.getBitrateFunction;
            track.getSampleRateFunction = this.getSampleRateFunction;

            result.add(track);
        } while (cursor.moveToNext());

        long end = System.currentTimeMillis();

        Log.d(TAG, "Extract " + result.size() + " tracks for " + (end - start) + " ms");

        return result;
    }


    private PublishSubject<Integer> trackDeletingObserver = PublishSubject.create();

    @Override
    public Observable<Integer> observeTrackDeleting() {
        return trackDeletingObserver;
    }

    @Override
    public void deleteTrack(int trackId) {
        getById(trackId)
                .map(Track::getFilePath)
                .flatMapCompletable(filePath -> Completable.fromAction(() -> {
                    if (deleteFile(filePath)) {
                        String whereClause = MediaStore.Audio.Media.DATA + " = ?";
                        contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, whereClause,
                                new String[]{filePath});
                    }
                }))
                .subscribeOn(Schedulers.io())
                .subscribe(() -> trackDeletingObserver.onNext(trackId));
    }

    //true если файл по данному пути поличилось удалить или если файл отсутсвовал изначально,
    // false, если не удалось удалить файл
    private boolean deleteFile(String path) {
        File deleteFile = new File(path);
        if (deleteFile.exists()) {
            return deleteFile.delete();
        }
        return true;
    }

    @Override
    public Single<List<Track>> getByAlbum(int albumId, Sorting sorting) {
        return Single.fromCallable(() ->
                contentResolver.query(
                        EXTERNAL_CONTENT_URI,
                        TRACK_QUERY_SELECTIONS,
                        SELECTION_MUSIC_BY_ALBUM_ID,
                        new String[]{String.valueOf(albumId)},
                        mapDefaultSorting(sorting)))
                .doAfterSuccess(Cursor::close)
                .map(this::extractTracks);
    }


    @Override
    public Single<List<Track>> getByArtist(int artistId, Sorting sorting) {
        return Single.fromCallable(() ->
                contentResolver.query(
                        EXTERNAL_CONTENT_URI, TRACK_QUERY_SELECTIONS,
                        SELECTION_MUSIC_BY_ARTIST_ID,
                        new String[]{String.valueOf(artistId)},
                        mapDefaultSorting(sorting)))
                .doAfterSuccess(Cursor::close)
                .map(this::extractTracks);
    }

    @Override
    public Single<List<Track>> getByPlaylist(int playlistId) {
        return playlistRepo.getById(playlistId)
                .flatMapObservable(playlist -> Observable.fromIterable(playlist.getPlaylistTracks()))
                .map(trackItem -> getById(trackItem.getTrackId()).blockingGet())
                .toList();
    }

    @Override
    public Single<List<Track>> getFavourites() {
        return Observable.fromIterable(favouriteTrackHelper.getFavourites())
                .sorted(Comparators.reversed(Comparators.comparingLong(Map.Entry::getValue)))
                .map(Map.Entry::getKey)
                .toList()
                .flatMap(this::getByIds);
    }

    private final BehaviorSubject<Irrelevant> favoritesUpdates = BehaviorSubject.createDefault(Irrelevant.INSTANCE);

    @Override
    public Observable<Irrelevant> observeFavouritesChanged() {
        return favoritesUpdates;
    }

    @Override
    public boolean toggleFavourite(int trackId) {
        boolean isFavourite = favouriteTrackHelper.isFavourite(trackId);

        if (isFavourite) removeFromFavourites(trackId);
        else addToFavourites(trackId);

        return !isFavourite;
    }

    @Override
    public void addToFavourites(int trackId) {
        favouriteTrackHelper.makeFavourite(trackId);
        favoritesUpdates.onNext(Irrelevant.INSTANCE);
    }

    @Override
    public void removeFromFavourites(int trackId) {
        favouriteTrackHelper.makeNotFavourite(trackId);
        favoritesUpdates.onNext(Irrelevant.INSTANCE);
    }
}
