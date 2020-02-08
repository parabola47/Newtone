package com.parabola.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.parabola.domain.repository.AccessRepository;
import com.parabola.domain.repository.AccessRepository.AccessType;
import com.parabola.domain.repository.AlbumRepository;
import com.parabola.domain.repository.ArtistRepository;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public final class SortingRepositoryImpl implements SortingRepository {


    private static final String PREFS_NAME = "SortingRepositoryImpl";

    private final SharedPreferences prefs;


    private final BehaviorSubject<TrackRepository.Sorting> allTracksSorting;
    private final BehaviorSubject<TrackRepository.Sorting> albumTracksSorting;
    private final BehaviorSubject<TrackRepository.Sorting> artistTracksSorting;
    private final BehaviorSubject<TrackRepository.Sorting> folderTracksSorting;

    private final BehaviorSubject<AlbumRepository.Sorting> allAlbumsSorting;
    private final BehaviorSubject<AlbumRepository.Sorting> artistAlbumsSorting;

    private final BehaviorSubject<ArtistRepository.Sorting> allArtistsSorting;


    public SortingRepositoryImpl(Context context, AccessRepository accessRepo) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        allTracksSorting = BehaviorSubject.createDefault(allTracksSorting());
        albumTracksSorting = BehaviorSubject.createDefault(albumTracksSorting());
        artistTracksSorting = BehaviorSubject.createDefault(artistTracksSorting());
        folderTracksSorting = BehaviorSubject.createDefault(folderTracksSorting());

        allAlbumsSorting = BehaviorSubject.createDefault(allAlbumsSorting());
        artistAlbumsSorting = BehaviorSubject.createDefault(artistAlbumsSorting());

        allArtistsSorting = BehaviorSubject.createDefault(allArtistsSorting());


        //  Если предоставлен доступ к файлам, то сообщаем всем об обновлении списков
        accessRepo.observeAccessUpdates(AccessType.FILE_STORAGE)
                .subscribe(hasFileStorageAccess -> {
                    if (hasFileStorageAccess) {
                        allArtistsSorting.onNext(allArtistsSorting.getValue());
                        allAlbumsSorting.onNext(allAlbumsSorting.getValue());
                        allTracksSorting.onNext(allTracksSorting.getValue());
                    }
                });
    }

    private static final String TRACKS_ALL = "TRACKS_ALL";
    private static final String TRACKS_ALBUM = "TRACKS_ALBUM";
    private static final String TRACKS_ARTIST = "TRACKS_ARTIST";
    private static final String TRACKS_FOLDER = "TRACKS_FOLDER";

    private static final String ALBUMS_ALL = "ALBUMS_ALL";
    private static final String ALBUMS_ARTIST = "ALBUMS_ARTIST";

    private static final String ARTISTS_ALL = "ARTISTS_ALL";

    @Override
    public TrackRepository.Sorting allTracksSorting() {
        String savedSorting = prefs.getString(TRACKS_ALL, TrackRepository.Sorting.BY_TITLE.name());

        return TrackRepository.Sorting.valueOf(savedSorting);
    }

    @Override
    public void setAllTracksSorting(TrackRepository.Sorting sorting) {
        if (allTracksSorting.getValue() == sorting) {
            return;
        }
        prefs.edit()
                .putString(TRACKS_ALL, sorting.name())
                .apply();
        allTracksSorting.onNext(sorting);
    }


    @Override
    public Observable<TrackRepository.Sorting> observeAllTracksSorting() {
        return allTracksSorting;
    }

    @Override
    public TrackRepository.Sorting albumTracksSorting() {
        String savedSorting = prefs.getString(TRACKS_ALBUM, TrackRepository.Sorting.BY_ALBUM_POSITION.name());

        return TrackRepository.Sorting.valueOf(savedSorting);
    }

    @Override
    public void setAlbumTracksSorting(TrackRepository.Sorting sorting) {
        if (albumTracksSorting.getValue() == sorting) {
            return;
        }
        prefs.edit()
                .putString(TRACKS_ALBUM, sorting.name())
                .apply();
        albumTracksSorting.onNext(sorting);
    }

    @Override
    public Observable<TrackRepository.Sorting> observeAlbumTracksSorting() {
        return albumTracksSorting;
    }

    @Override
    public TrackRepository.Sorting artistTracksSorting() {
        String savedSorting = prefs.getString(TRACKS_ARTIST, TrackRepository.Sorting.BY_TITLE.name());

        return TrackRepository.Sorting.valueOf(savedSorting);
    }

    @Override
    public void setArtistTracksSorting(TrackRepository.Sorting sorting) {
        if (artistTracksSorting.getValue() == sorting) {
            return;
        }
        prefs.edit()
                .putString(TRACKS_ARTIST, sorting.name())
                .apply();
        artistTracksSorting.onNext(sorting);
    }

    @Override
    public Observable<TrackRepository.Sorting> observeArtistTracksSorting() {
        return artistTracksSorting;
    }

    @Override
    public TrackRepository.Sorting folderTracksSorting() {
        String savedSorting = prefs.getString(TRACKS_FOLDER, TrackRepository.Sorting.BY_TITLE.name());

        return TrackRepository.Sorting.valueOf(savedSorting);
    }

    @Override
    public void setFolderTracksSorting(TrackRepository.Sorting sorting) {
        if (folderTracksSorting.getValue() == sorting) {
            return;
        }
        prefs.edit()
                .putString(TRACKS_FOLDER, sorting.name())
                .apply();
        folderTracksSorting.onNext(sorting);
    }


    @Override
    public Observable<TrackRepository.Sorting> observeFolderTracksSorting() {
        return folderTracksSorting;
    }

    @Override
    public AlbumRepository.Sorting allAlbumsSorting() {
        String savedSorting = prefs.getString(ALBUMS_ALL, AlbumRepository.Sorting.BY_TITLE.name());

        return AlbumRepository.Sorting.valueOf(savedSorting);
    }

    @Override
    public void setAllAlbumsSorting(AlbumRepository.Sorting sorting) {
        if (allAlbumsSorting.getValue() == sorting) {
            return;
        }
        prefs.edit()
                .putString(ALBUMS_ALL, sorting.name())
                .apply();
        allAlbumsSorting.onNext(sorting);
    }


    @Override
    public Observable<AlbumRepository.Sorting> observeAllAlbumsSorting() {
        return allAlbumsSorting;
    }

    @Override
    public AlbumRepository.Sorting artistAlbumsSorting() {
        String savedSorting = prefs.getString(ALBUMS_ARTIST, AlbumRepository.Sorting.BY_TITLE.name());

        return AlbumRepository.Sorting.valueOf(savedSorting);
    }

    @Override
    public void setArtistAlbumsSorting(AlbumRepository.Sorting sorting) {
        if (artistAlbumsSorting.getValue() == sorting) {
            return;
        }
        prefs.edit()
                .putString(ALBUMS_ARTIST, sorting.name())
                .apply();
        artistAlbumsSorting.onNext(sorting);
    }

    @Override
    public Observable<AlbumRepository.Sorting> observeArtistAlbumsSorting() {
        return artistAlbumsSorting;
    }

    @Override
    public ArtistRepository.Sorting allArtistsSorting() {
        String savedSorting = prefs.getString(ARTISTS_ALL, ArtistRepository.Sorting.BY_NAME.name());

        return ArtistRepository.Sorting.valueOf(savedSorting);
    }

    @Override
    public void setAllArtistsSorting(ArtistRepository.Sorting sorting) {
        if (allArtistsSorting.getValue() == sorting) {
            return;
        }
        prefs.edit()
                .putString(ARTISTS_ALL, sorting.name())
                .apply();
        allArtistsSorting.onNext(sorting);
    }

    @Override
    public Observable<ArtistRepository.Sorting> observeAllArtistsSorting() {
        return allArtistsSorting;
    }
}
