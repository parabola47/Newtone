package com.parabola.domain.interactors;

import com.parabola.domain.model.Track;
import com.parabola.domain.repository.SortingRepository;
import com.parabola.domain.repository.TrackRepository;

import java.util.List;

import io.reactivex.Single;

public final class TrackInteractor {

    private final TrackRepository trackRepo;
    private final SortingRepository configRepo;

    public TrackInteractor(TrackRepository trackRepo, SortingRepository configRepo) {
        this.trackRepo = trackRepo;
        this.configRepo = configRepo;
    }


    public Single<List<Track>> getAll() {
        return trackRepo.getAll(configRepo.allTracksSorting());
    }


    public Single<List<Track>> getByAlbum(int albumId) {
        return trackRepo.getByAlbum(albumId, configRepo.albumTracksSorting());
    }


    public Single<List<Track>> getByArtist(int artistId) {
        return trackRepo.getByArtist(artistId, configRepo.artistTracksSorting());
    }


    public Single<List<Track>> getByPlaylist(int playlistId) {
        return trackRepo.getByPlaylist(playlistId);
    }

    public Single<List<Track>> getRecentlyAddedTracks() {
        return trackRepo.getAll(TrackRepository.Sorting.BY_DATE_ADDING_DESC);
    }

    public Single<List<Track>> getFavourites() {
        return trackRepo.getFavourites();
    }
}
