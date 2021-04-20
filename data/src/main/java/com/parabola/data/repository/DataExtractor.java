package com.parabola.data.repository;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.provider.MediaStore;

import com.parabola.data.model.AlbumData;
import com.parabola.data.model.ArtistData;
import com.parabola.data.model.FolderData;
import com.parabola.data.model.TrackData;
import com.parabola.domain.executor.SchedulerProvider;
import com.parabola.domain.interactor.RepositoryInteractor;
import com.parabola.domain.model.Album;
import com.parabola.domain.model.Artist;
import com.parabola.domain.model.Folder;
import com.parabola.domain.model.Track;
import com.parabola.domain.repository.ExcludedFolderRepository;
import com.parabola.domain.repository.PermissionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Audio.AudioColumns.ALBUM_ID;
import static android.provider.MediaStore.Audio.AudioColumns.ARTIST_ID;
import static android.provider.MediaStore.Audio.AudioColumns.IS_MUSIC;
import static android.provider.MediaStore.Audio.AudioColumns.TRACK;
import static android.provider.MediaStore.Audio.AudioColumns.YEAR;
import static android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.MediaColumns.ALBUM;
import static android.provider.MediaStore.MediaColumns.ARTIST;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.DURATION;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.TITLE;
import static com.parabola.data.utils.FileUtil.deleteFile;

public final class DataExtractor implements RepositoryInteractor {
    private static final String LOG_TAG = DataExtractor.class.getSimpleName();

    private final ExcludedFolderRepository excludedFolderRepo;
    private final ContentResolver contentResolver;

    final List<Track> tracks = new ArrayList<>(100);
    final List<Album> albums = new ArrayList<>(60);
    final List<Artist> artists = new ArrayList<>(40);
    final List<Folder> folders = new ArrayList<>(30);

    final FavouriteTrackHelper favouriteTrackHelper;
    final AlbumArtExtractor albumArtExtractor;


    private final BehaviorSubject<LoadingState> loadingStateObserver = BehaviorSubject.createDefault(LoadingState.IN_LOADING);


    private final String[] TRACK_QUERY_SELECTIONS = new String[]{
            _ID, DATA, TITLE, ALBUM_ID, ALBUM, ARTIST_ID, ARTIST, DURATION,
            DATE_ADDED, TRACK, YEAR, SIZE};


    private final String[] UNSUPPORTED_FILE_FORMATS = {".mid", ".midi"};

    private final int MINIMAL_FILE_SIZE_BYTES = 1024;


    public DataExtractor(ExcludedFolderRepository excludedFolderRepo,
                         SchedulerProvider schedulers,
                         PermissionHandler permissionHandler,
                         ContentResolver contentResolver,
                         SharedPreferences preferences) {
        this.excludedFolderRepo = excludedFolderRepo;
        this.contentResolver = contentResolver;

        permissionHandler.observePermissionUpdates(PermissionHandler.Type.FILE_STORAGE)
                .filter(hasFileStoragePermission -> hasFileStoragePermission)
                .doOnNext(hasFileStoragePermission -> {
                    initAllData();
                    loadingStateObserver.onNext(LoadingState.LOADED);
                })
                .subscribeOn(schedulers.io())
                .subscribe();


        excludedFolderRepo.onExcludeFoldersUpdatesObserver()
                .doOnNext(i -> {
                    loadingStateObserver.onNext(LoadingState.IN_LOADING);
                    initAllData();
                    loadingStateObserver.onNext(LoadingState.LOADED);
                })
                .subscribe();

        favouriteTrackHelper = new FavouriteTrackHelper(preferences);
        albumArtExtractor = new AlbumArtExtractor(contentResolver, tracks);
    }


    @Override
    public Observable<LoadingState> observeLoadingState() {
        return loadingStateObserver;
    }


    private void initAllData() {
        tracks.clear();
        albums.clear();
        artists.clear();
        folders.clear();

        try (Cursor cursor = contentResolver.query(
                EXTERNAL_CONTENT_URI, TRACK_QUERY_SELECTIONS,
                createTracksSelection(), null, null)) {
            if (cursor == null)
                return;

            initTracks(cursor);
        }

        initAlbums();
        initArtists();
        initFolders();
    }


    private String createTracksSelection() {
        String trackSelection = IS_MUSIC + "=1";

        //фильтр исключённых папок
        for (String excludedFolder : excludedFolderRepo.getExcludedFolders()) {
            trackSelection += " AND " + DATA + " NOT LIKE '" + excludedFolder + "%'";
        }
        //фильтр неподдерживаемых форматов файлов
        for (String unsupportedFormat : UNSUPPORTED_FILE_FORMATS) {
            trackSelection += " AND " + DATA + " NOT LIKE '%" + unsupportedFormat + "'";
        }
        //фильтр по размеру файла
        trackSelection += " AND " + SIZE + " > " + MINIMAL_FILE_SIZE_BYTES;

        return trackSelection;
    }


    public void deleteTrack(int trackId) {
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).getId() == trackId) {
                Track deletedTrack = tracks.remove(i);
                deleteTrackInternal(deletedTrack);

                break;
            }
        }
    }

    private void deleteTrackInternal(Track track) {
        if (deleteFile(track.getFilePath())) {
            String whereClause = MediaStore.Audio.Media.DATA + " = ?";
            contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, whereClause,
                    new String[]{track.getFilePath()});
            initArtists();
            initAlbums();
            initFolders();
        }
    }


    private void initTracks(Cursor cursor) {
        if (!cursor.moveToFirst()) {
            return;
        }

        do {
            TrackData track = new TrackData();

            track.id = cursor.getInt(cursor.getColumnIndexOrThrow(_ID));
            track.title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
            track.albumId = cursor.getInt(cursor.getColumnIndexOrThrow(ALBUM_ID));
            track.albumTitle = cursor.getString(cursor.getColumnIndexOrThrow(ALBUM));
            track.artistId = cursor.getInt(cursor.getColumnIndexOrThrow(ARTIST_ID));
            track.artistName = cursor.getString(cursor.getColumnIndexOrThrow(ARTIST));
            track.durationMs = cursor.getLong(cursor.getColumnIndexOrThrow(DURATION));
            track.filePath = cursor.getString(cursor.getColumnIndexOrThrow(DATA));
            track.year = cursor.getInt(cursor.getColumnIndexOrThrow(YEAR));
            track.dateAdded = cursor.getInt(cursor.getColumnIndexOrThrow(DATE_ADDED));
            track.positionInCd = cursor.getInt(cursor.getColumnIndexOrThrow(TRACK));
            track.fileSize = cursor.getInt(cursor.getColumnIndexOrThrow(SIZE));
            track.isFavouriteCondition = this.isFavouriteCondition;
            track.getArtFunction = this.getTrackArtFunction;
            track.getGenreIdFunction = this.getGenreIdFunction;
            track.getGenreNameFunction = this.getGenreNameFunction;
            track.getBitrateFunction = this.getBitrateFunction;
            track.getSampleRateFunction = this.getSampleRateFunction;

            tracks.add(track);
        } while (cursor.moveToNext());
    }

    private void initAlbums() {
        for (Track track : tracks) {
            if (albums.stream().noneMatch(album -> album.getId() == track.getAlbumId())) {

                AlbumData albumData = new AlbumData();
                albumData.id = track.getAlbumId();
                albumData.title = track.getAlbumTitle();
                albumData.artistId = track.getArtistId();
                albumData.artistName = track.getArtistName();
                albumData.getArtFunction = this.getAlbumArtFunction;
                albumData.year = track.getYear();
                albumData.tracksCount = (int) tracks.stream().filter(t -> t.getAlbumId() == albumData.id).count();

                albums.add(albumData);
            }
        }
    }

    private void initArtists() {
        for (Track track : tracks) {
            if (artists.stream().noneMatch(artist -> artist.getId() == track.getArtistId())) {
                ArtistData artistData = new ArtistData();
                artistData.id = track.getArtistId();
                artistData.name = track.getArtistName();
                artistData.albumsCount = (int) albums.stream().filter(a -> a.getArtistId() == artistData.id).count();
                artistData.tracksCount = (int) tracks.stream().filter(t -> t.getArtistId() == artistData.id).count();

                artists.add(artistData);
            }
        }
    }

    private void initFolders() {
        for (Track track : tracks) {
            if (folders.stream().noneMatch(folder -> track.getFolderPath().equals(folder.getAbsolutePath()))) {
                FolderData folderData = new FolderData();

                folderData.folderPath = track.getFolderPath();
                folderData.tracksCount = (int) tracks.stream().filter(t -> folderData.folderPath.equals(t.getFolderPath())).count();

                folders.add(folderData);
            }
        }
    }


    private final Predicate<TrackData> isFavouriteCondition = new Predicate<TrackData>() {
        public boolean test(TrackData trackData) {
            return favouriteTrackHelper.isFavourite(trackData.getId());
        }
    };
    private final Function<TrackData, Bitmap> getTrackArtFunction = new Function<TrackData, Bitmap>() {
        public Bitmap apply(TrackData trackData) {
            return albumArtExtractor.getByAlbum(trackData.albumId);
        }
    };
    private final Function<AlbumData, Bitmap> getAlbumArtFunction = new Function<AlbumData, Bitmap>() {
        public Bitmap apply(AlbumData albumData) {
            return albumArtExtractor.getByAlbum(albumData.id);
        }
    };
    private final Function<TrackData, Integer> getGenreIdFunction = new Function<TrackData, Integer>() {
        public Integer apply(TrackData trackData) {
            try (Cursor cursor = contentResolver.query(
                    MediaStore.Audio.Genres.getContentUriForAudioId("external", trackData.id),
                    new String[]{_ID}, null, null, null)) {
                if (cursor == null || !cursor.moveToFirst())
                    return 0;

                return cursor.getInt(0);
            }
        }
    };
    private final Function<TrackData, String> getGenreNameFunction = new Function<TrackData, String>() {
        public String apply(TrackData trackData) {
            try (Cursor cursor = contentResolver.query(
                    MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.GenresColumns.NAME},
                    _ID + " = " + trackData.getGenreId(), null, null)) {
                if (cursor == null || !cursor.moveToFirst())
                    return "";

                return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.GenresColumns.NAME));
            }
        }
    };
    private final Function<TrackData, Integer> getBitrateFunction = trackData -> {
        try {
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(trackData.filePath);

            return mediaExtractor.getTrackFormat(0)
                    .getInteger(MediaFormat.KEY_BIT_RATE) / 1000;
        } catch (IOException | NullPointerException e) {
            return 0;
        }
    };
    private final Function<TrackData, Integer> getSampleRateFunction = track -> {
        try {
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(track.filePath);

            return mediaExtractor.getTrackFormat(0)
                    .getInteger(MediaFormat.KEY_SAMPLE_RATE);
        } catch (IOException | NullPointerException e) {
            return 0;
        }
    };


}
