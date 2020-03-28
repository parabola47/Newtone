package com.parabola.data.settings;

import android.content.SharedPreferences;

import com.parabola.domain.settings.ViewSettingsInteractor;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public final class ViewSettingsInteractorImpl implements ViewSettingsInteractor {

    private final SharedPreferences prefs;


    private static final String TAB_ALBUM_VIEW_TYPE_KEY = "com.parabola.data.settings.TAB_ALBUM_VIEW_TYPE";
    private final BehaviorSubject<AlbumViewType> tabAlbumViewTypeObserver;

    private static final String ARTIST_ALBUMS_VIEW_TYPE_KEY = "com.parabola.data.settings.ARTIST_ALBUMS_VIEW_TYPE";
    private final BehaviorSubject<AlbumViewType> artistAlbumsViewTypeObserver;


    private final BehaviorSubject<TrackItemView> trackItemViewObserver;

    public ViewSettingsInteractorImpl(SharedPreferences sharedPreferences) {
        this.prefs = sharedPreferences;
        this.tabAlbumViewTypeObserver = BehaviorSubject.createDefault(getTabAlbumViewType());
        this.artistAlbumsViewTypeObserver = BehaviorSubject.createDefault(getArtistAlbumsViewType());


        TrackItemView trackItemView = new TrackItemView(
                getTrackItemTextSize(),
                getTrackItemBorderPadding(),
                getIsTrackItemCoverShows(),
                getTrackItemCoverSize(),
                getTrackItemCoverCornersRadius());
        trackItemViewObserver = BehaviorSubject.createDefault(trackItemView);
    }

    //    T R A C K    I T E M S
    private static final String TRACK_ITEM_TEXT_SIZE_KEY = "com.parabola.data.settings.TRACK_ITEM_TEXT_SIZE";
    private static final String TRACK_ITEM_BORDER_PADDING_KEY = "com.parabola.data.settings.TRACK_ITEM_BORDER_PADDING";
    private static final String TRACK_ITEM_COVER_SHOWS_KEY = "com.parabola.data.settings.TRACK_ITEM_COVER_SHOWS";
    private static final String TRACK_ITEM_COVER_SIZE_KEY = "com.parabola.data.settings.TRACK_ITEM_COVER_SIZE";
    private static final String TRACK_ITEM_COVER_CORNER_RADIUS_KEY = "com.parabola.data.settings.TRACK_ITEM_COVER_CORNER_RADIUS";

    @Override
    public TrackItemView getTrackItemViewSettings() {
        return trackItemViewObserver.getValue();
    }

    @Override
    public void setTrackItemView(TrackItemView trackItemView) {
        setTrackItemTextSize(trackItemView.textSize);
        setTrackItemBorderPadding(trackItemView.borderPadding);
        setIsTrackItemCoverShows(trackItemView.isCoverShows);
        setTrackItemCoverSize(trackItemView.coverSize);
        setTrackItemCoverCornersRadius(trackItemView.coverCornersRadius);

        trackItemViewObserver.onNext(trackItemView);
    }

    @Override
    public Observable<TrackItemView> observeTrackItemViewUpdates() {
        return trackItemViewObserver;
    }

    @Override
    public int getTrackItemTextSize() {
        return prefs.getInt(TRACK_ITEM_TEXT_SIZE_KEY, 16);
    }

    private void setTrackItemTextSize(int sizeDp) {
        prefs.edit()
                .putInt(TRACK_ITEM_TEXT_SIZE_KEY, sizeDp)
                .apply();
    }

    @Override
    public int getTrackItemBorderPadding() {
        return prefs.getInt(TRACK_ITEM_BORDER_PADDING_KEY, 16);
    }

    private void setTrackItemBorderPadding(int paddingDp) {
        prefs.edit()
                .putInt(TRACK_ITEM_BORDER_PADDING_KEY, paddingDp)
                .apply();
    }

    @Override
    public boolean getIsTrackItemCoverShows() {
        return prefs.getBoolean(TRACK_ITEM_COVER_SHOWS_KEY, true);
    }

    private void setIsTrackItemCoverShows(boolean show) {
        prefs.edit()
                .putBoolean(TRACK_ITEM_COVER_SHOWS_KEY, show)
                .apply();
    }

    @Override
    public int getTrackItemCoverSize() {
        return prefs.getInt(TRACK_ITEM_COVER_SIZE_KEY, 40);
    }

    private void setTrackItemCoverSize(int coverSizeDp) {
        prefs.edit()
                .putInt(TRACK_ITEM_COVER_SIZE_KEY, coverSizeDp)
                .apply();
    }

    @Override
    public int getTrackItemCoverCornersRadius() {
        return prefs.getInt(TRACK_ITEM_COVER_CORNER_RADIUS_KEY, 4);
    }

    private void setTrackItemCoverCornersRadius(int coverCornersRadiusDp) {
        prefs.edit()
                .putInt(TRACK_ITEM_COVER_CORNER_RADIUS_KEY, coverCornersRadiusDp)
                .apply();
    }


    //    T A B    A L B U M S
    @Override
    public AlbumViewType getTabAlbumViewType() {
        int savedValue = prefs.getInt(TAB_ALBUM_VIEW_TYPE_KEY, getDefaultAlbumViewType().ordinal());

        for (AlbumViewType albumViewType : AlbumViewType.values()) {
            if (albumViewType.ordinal() == savedValue)
                return albumViewType;
        }

        return getDefaultAlbumViewType();
    }

    @Override
    public void setTabAlbumViewType(AlbumViewType viewType) {
        prefs.edit()
                .putInt(TAB_ALBUM_VIEW_TYPE_KEY, viewType.ordinal())
                .apply();
        tabAlbumViewTypeObserver.onNext(viewType);
    }

    @Override
    public Observable<AlbumViewType> observeTabAlbumViewType() {
        return tabAlbumViewTypeObserver;
    }


    //    A R T I S T    A L B U M S
    @Override
    public AlbumViewType getArtistAlbumsViewType() {
        int savedValue = prefs.getInt(ARTIST_ALBUMS_VIEW_TYPE_KEY, getDefaultAlbumViewType().ordinal());

        for (AlbumViewType albumViewType : AlbumViewType.values()) {
            if (albumViewType.ordinal() == savedValue)
                return albumViewType;
        }

        return getDefaultAlbumViewType();
    }

    @Override
    public void setArtistAlbumsViewType(AlbumViewType viewType) {
        prefs.edit()
                .putInt(ARTIST_ALBUMS_VIEW_TYPE_KEY, viewType.ordinal())
                .apply();
        artistAlbumsViewTypeObserver.onNext(viewType);
    }

    @Override
    public Observable<AlbumViewType> observeArtistAlbumsViewType() {
        return artistAlbumsViewTypeObserver;
    }
}
