package com.parabola.domain.settings;

import io.reactivex.Observable;

public interface ViewSettingsInteractor {

    default AlbumViewType getDefaultAlbumViewType() {
        return AlbumViewType.GRID;
    }

    AlbumViewType getTabAlbumViewType();
    void setTabAlbumViewType(AlbumViewType viewType);
    Observable<AlbumViewType> observeTabAlbumViewType();


    AlbumViewType getArtistAlbumsViewType();
    void setArtistAlbumsViewType(AlbumViewType viewType);
    Observable<AlbumViewType> observeArtistAlbumsViewType();

    enum AlbumViewType {
        GRID, LIST
    }
}
