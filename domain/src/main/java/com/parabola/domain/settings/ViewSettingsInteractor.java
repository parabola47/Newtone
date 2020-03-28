package com.parabola.domain.settings;

import io.reactivex.Observable;

public interface ViewSettingsInteractor {

    TrackItemView getTrackItemViewSettings();
    void setTrackItemView(TrackItemView trackItemView);
    Observable<TrackItemView> observeTrackItemViewUpdates();

    int getTrackItemTextSize();
    int getTrackItemBorderPadding();
    boolean getIsTrackItemCoverShows();
    int getTrackItemCoverSize();
    int getTrackItemCoverCornersRadius();

    class TrackItemView {
        public final int textSize;
        public final int borderPadding;
        public final boolean isCoverShows;
        public final int coverSize;
        public final int coverCornersRadius;

        public TrackItemView(int textSize, int borderPadding, boolean isCoverShows, int coverSize, int coverCornersRadius) {
            this.textSize = textSize;
            this.borderPadding = borderPadding;
            this.isCoverShows = isCoverShows;
            this.coverSize = coverSize;
            this.coverCornersRadius = coverCornersRadius;
        }
    }


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
