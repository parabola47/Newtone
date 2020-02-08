package com.parabola.domain.repository;

import io.reactivex.Observable;

public interface SortingRepository {

    TrackRepository.Sorting allTracksSorting();
    void setAllTracksSorting(TrackRepository.Sorting sorting);
    Observable<TrackRepository.Sorting> observeAllTracksSorting();

    TrackRepository.Sorting albumTracksSorting();
    void setAlbumTracksSorting(TrackRepository.Sorting sorting);
    Observable<TrackRepository.Sorting> observeAlbumTracksSorting();

    TrackRepository.Sorting artistTracksSorting();
    void setArtistTracksSorting(TrackRepository.Sorting sorting);
    Observable<TrackRepository.Sorting> observeArtistTracksSorting();

    TrackRepository.Sorting folderTracksSorting();
    void setFolderTracksSorting(TrackRepository.Sorting sorting);
    Observable<TrackRepository.Sorting> observeFolderTracksSorting();

    AlbumRepository.Sorting allAlbumsSorting();
    void setAllAlbumsSorting(AlbumRepository.Sorting sorting);
    Observable<AlbumRepository.Sorting> observeAllAlbumsSorting();

    AlbumRepository.Sorting artistAlbumsSorting();
    void setArtistAlbumsSorting(AlbumRepository.Sorting sorting);
    Observable<AlbumRepository.Sorting> observeArtistAlbumsSorting();

    ArtistRepository.Sorting allArtistsSorting();
    void setAllArtistsSorting(ArtistRepository.Sorting sorting);
    Observable<ArtistRepository.Sorting> observeAllArtistsSorting();

}
