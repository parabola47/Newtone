package com.parabola.domain.settings;

import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType;

import io.reactivex.Observable;

public interface ViewSettingsInteractor {

    //C O L O R    T H E M E
    ColorTheme DEFAULT_COLOR_THEME = ColorTheme.DARK;

    ColorTheme getColorTheme();
    void setColorTheme(ColorTheme colorTheme);
    Observable<ColorTheme> observeColorTheme();


    //T R A C K    I T E M S
    TrackItemView getTrackItemViewSettings();
    void setTrackItemView(TrackItemView trackItemView);
    Observable<TrackItemView> observeTrackItemViewUpdates();

    int getTrackItemTextSize();
    int getTrackItemBorderPadding();
    boolean getIsAlbumTitleShows();
    boolean getIsTrackItemCoverShows();
    int getTrackItemCoverSize();
    int getTrackItemCoverCornersRadius();


    //A L B U M    I T E M S
    AlbumItemView getAlbumItemViewSettings();
    void setAlbumItemViewSettings(AlbumItemView albumItemView);
    Observable<AlbumItemView> observeAlbumItemViewUpdates();

    AlbumViewType getAlbumItemViewType();
    int getAlbumItemTextSize();
    int getAlbumItemBorderPadding();
    int getAlbumItemCoverSize();
    int getAlbumItemCoverCornersRadius();


    //A R T I S T    I T E M S
    ArtistItemView getArtistItemViewSettings();
    void setArtistItemViewSettings(ArtistItemView artistItemView);
    Observable<ArtistItemView> observeArtistItemViewUpdates();

    int getArtistItemTextSize();
    int getArtistItemBorderPadding();


    enum ColorTheme {
        DARK, LIGHT
    }

    class TrackItemView {
        public final int textSize;
        public final int borderPadding;
        public final boolean isAlbumTitleShows;
        public final boolean isCoverShows;
        public final int coverSize;
        public final int coverCornersRadius;

        public TrackItemView(int textSize, int borderPadding, boolean isAlbumTitleShows,
                             boolean isCoverShows, int coverSize, int coverCornersRadius) {
            this.textSize = textSize;
            this.borderPadding = borderPadding;
            this.isAlbumTitleShows = isAlbumTitleShows;
            this.isCoverShows = isCoverShows;
            this.coverSize = coverSize;
            this.coverCornersRadius = coverCornersRadius;
        }
    }

    class AlbumItemView {
        public final AlbumViewType viewType;
        public final int textSize;
        public final int borderPadding;
        public final int coverSize;
        public final int coverCornersRadius;

        public AlbumItemView(AlbumViewType viewType,
                             int textSize, int borderPadding,
                             int coverSize, int coverCornersRadius) {
            this.viewType = viewType;
            this.textSize = textSize;
            this.borderPadding = borderPadding;
            this.coverSize = coverSize;
            this.coverCornersRadius = coverCornersRadius;
        }

        public enum AlbumViewType {
            GRID, LIST
        }
    }

    class ArtistItemView {
        public final int textSize;
        public final int borderPadding;

        public ArtistItemView(int textSize, int borderPadding) {
            this.textSize = textSize;
            this.borderPadding = borderPadding;
        }
    }

}
