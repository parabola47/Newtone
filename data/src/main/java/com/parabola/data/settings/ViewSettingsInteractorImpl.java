package com.parabola.data.settings;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
import static com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType.GRID;

public final class ViewSettingsInteractorImpl implements ViewSettingsInteractor {

    private final SharedPreferences prefs;

    private final BehaviorSubject<ColorTheme> colorThemeObserver;

    private final BehaviorSubject<TrackItemView> trackItemViewObserver;
    private final BehaviorSubject<AlbumItemView> albumItemViewObserver;
    private final BehaviorSubject<ArtistItemView> artistItemViewObserver;

    public ViewSettingsInteractorImpl(SharedPreferences sharedPreferences) {
        this.prefs = sharedPreferences;

        ColorTheme colorTheme = getColorTheme();
        AppCompatDelegate.setDefaultNightMode(colorTheme == ColorTheme.DARK ? MODE_NIGHT_YES : MODE_NIGHT_NO);
        colorThemeObserver = BehaviorSubject.createDefault(colorTheme);


        TrackItemView trackItemView = new TrackItemView(
                getTrackItemTextSize(),
                getTrackItemBorderPadding(),
                getIsAlbumTitleShows(),
                getIsTrackItemCoverShows(),
                getTrackItemCoverSize(),
                getTrackItemCoverCornersRadius());
        trackItemViewObserver = BehaviorSubject.createDefault(trackItemView);


        AlbumItemView albumItemView = new AlbumItemView(
                getAlbumItemViewType(),
                getAlbumItemTextSize(),
                getAlbumItemBorderPadding(),
                getAlbumItemCoverSize(),
                getAlbumItemCoverCornersRadius());
        albumItemViewObserver = BehaviorSubject.createDefault(albumItemView);


        ArtistItemView artistItemView = new ArtistItemView(
                getArtistItemTextSize(),
                getArtistItemBorderPadding());
        artistItemViewObserver = BehaviorSubject.createDefault(artistItemView);
    }


    //C O L O R    T H E M E
    private static final String COLOR_THEME_KEY = "com.parabola.data.settings.COLOR_THEME";

    @Override
    public ColorTheme getColorTheme() {
        String savedColorTheme = prefs
                .getString(COLOR_THEME_KEY, DEFAULT_COLOR_THEME.name());
        return ColorTheme.valueOf(savedColorTheme);
    }

    @Override
    public void setColorTheme(ColorTheme colorTheme) {
        prefs.edit()
                .putString(COLOR_THEME_KEY, colorTheme.name())
                .apply();

        AppCompatDelegate.setDefaultNightMode(colorTheme == ColorTheme.DARK ? MODE_NIGHT_YES : MODE_NIGHT_NO);

        colorThemeObserver.onNext(colorTheme);
    }

    @Override
    public Observable<ColorTheme> observeColorTheme() {
        return colorThemeObserver;
    }

    //    T R A C K    I T E M S
    private static final String TRACK_ITEM_TEXT_SIZE_KEY = "com.parabola.data.settings.TRACK_ITEM_TEXT_SIZE";
    private static final String TRACK_ITEM_BORDER_PADDING_KEY = "com.parabola.data.settings.TRACK_ITEM_BORDER_PADDING";
    private static final String TRACK_ITEM_ALBUM_TITLE_SHOWS_KEY = "com.parabola.data.settings.TRACK_ITEM_ALBUM_TITLE_SHOWS";
    private static final String TRACK_ITEM_COVER_SHOWS_KEY = "com.parabola.data.settings.TRACK_ITEM_COVER_SHOWS";
    private static final String TRACK_ITEM_COVER_SIZE_KEY = "com.parabola.data.settings.TRACK_ITEM_COVER_SIZE";
    private static final String TRACK_ITEM_COVER_CORNER_RADIUS_KEY = "com.parabola.data.settings.TRACK_ITEM_COVER_CORNER_RADIUS";

    @Override
    public TrackItemView getTrackItemViewSettings() {
        return trackItemViewObserver.getValue();
    }

    @Override
    public void setTrackItemView(TrackItemView trackItemView) {
        prefs.edit()
                .putInt(TRACK_ITEM_TEXT_SIZE_KEY, trackItemView.textSize)
                .putInt(TRACK_ITEM_BORDER_PADDING_KEY, trackItemView.borderPadding)
                .putBoolean(TRACK_ITEM_ALBUM_TITLE_SHOWS_KEY, trackItemView.isAlbumTitleShows)
                .putBoolean(TRACK_ITEM_COVER_SHOWS_KEY, trackItemView.isCoverShows)
                .putInt(TRACK_ITEM_COVER_SIZE_KEY, trackItemView.coverSize)
                .putInt(TRACK_ITEM_COVER_CORNER_RADIUS_KEY, trackItemView.coverCornersRadius)
                .apply();

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

    @Override
    public int getTrackItemBorderPadding() {
        return prefs.getInt(TRACK_ITEM_BORDER_PADDING_KEY, 16);
    }

    @Override
    public boolean getIsAlbumTitleShows() {
        return prefs.getBoolean(TRACK_ITEM_ALBUM_TITLE_SHOWS_KEY, false);
    }

    @Override
    public boolean getIsTrackItemCoverShows() {
        return prefs.getBoolean(TRACK_ITEM_COVER_SHOWS_KEY, true);
    }

    @Override
    public int getTrackItemCoverSize() {
        return prefs.getInt(TRACK_ITEM_COVER_SIZE_KEY, 40);
    }

    @Override
    public int getTrackItemCoverCornersRadius() {
        return prefs.getInt(TRACK_ITEM_COVER_CORNER_RADIUS_KEY, 4);
    }


    //A L B U M S    I T E M S
    private static final String ALBUM_ITEM_VIEW_TYPE_KEY = "com.parabola.data.settings.ALBUM_ITEM_VIEW_TYPE";
    private static final String ALBUM_ITEM_TEXT_SIZE_KEY = "com.parabola.data.settings.ALBUM_ITEM_TEXT_SIZE";
    private static final String ALBUM_ITEM_BORDER_PADDING_KEY = "com.parabola.data.settings.ALBUM_ITEM_BORDER_PADDING";
    private static final String ALBUM_ITEM_COVER_SIZE_KEY = "com.parabola.data.settings.ALBUM_ITEM_COVER_SIZE";
    private static final String ALBUM_ITEM_COVER_CORNERS_RADIUS_KEY = "com.parabola.data.settings.ALBUM_ITEM_COVER_CORNERS_RADIUS";

    @Override
    public AlbumItemView getAlbumItemViewSettings() {
        return albumItemViewObserver.getValue();
    }

    @Override
    public void setAlbumItemViewSettings(AlbumItemView albumItemView) {
        prefs.edit()
                .putString(ALBUM_ITEM_VIEW_TYPE_KEY, albumItemView.viewType.name())
                .putInt(ALBUM_ITEM_TEXT_SIZE_KEY, albumItemView.textSize)
                .putInt(ALBUM_ITEM_BORDER_PADDING_KEY, albumItemView.borderPadding)
                .putInt(ALBUM_ITEM_COVER_SIZE_KEY, albumItemView.coverSize)
                .putInt(ALBUM_ITEM_COVER_CORNERS_RADIUS_KEY, albumItemView.coverCornersRadius)
                .apply();

        albumItemViewObserver.onNext(albumItemView);
    }

    @Override
    public Observable<AlbumItemView> observeAlbumItemViewUpdates() {
        return albumItemViewObserver;
    }

    @Override
    public AlbumViewType getAlbumItemViewType() {
        return AlbumViewType.valueOf(prefs.getString(ALBUM_ITEM_VIEW_TYPE_KEY, GRID.name()));
    }

    @Override
    public int getAlbumItemTextSize() {
        return prefs.getInt(ALBUM_ITEM_TEXT_SIZE_KEY, 16);
    }

    @Override
    public int getAlbumItemBorderPadding() {
        return prefs.getInt(ALBUM_ITEM_BORDER_PADDING_KEY, 16);
    }


    @Override
    public int getAlbumItemCoverSize() {
        return prefs.getInt(ALBUM_ITEM_COVER_SIZE_KEY, 64);
    }

    @Override
    public int getAlbumItemCoverCornersRadius() {
        return prefs.getInt(ALBUM_ITEM_COVER_CORNERS_RADIUS_KEY, 4);
    }


    //A R T I S T    I T E M S
    private static final String ARTIST_ITEM_TEXT_SIZE_KEY = "com.parabola.data.settings.ARTIST_ITEM_TEXT_SIZE";
    private static final String ARTIST_ITEM_BORDER_PADDING_KEY = "com.parabola.data.settings.ARTIST_ITEM_BORDER_PADDING";

    @Override
    public ArtistItemView getArtistItemViewSettings() {
        return artistItemViewObserver.getValue();
    }

    @Override
    public void setArtistItemViewSettings(ArtistItemView artistItemView) {
        prefs.edit()
                .putInt(ARTIST_ITEM_TEXT_SIZE_KEY, artistItemView.textSize)
                .putInt(ARTIST_ITEM_BORDER_PADDING_KEY, artistItemView.borderPadding)
                .apply();

        artistItemViewObserver.onNext(artistItemView);
    }

    @Override
    public Observable<ArtistItemView> observeArtistItemViewUpdates() {
        return artistItemViewObserver;
    }

    @Override
    public int getArtistItemTextSize() {
        return prefs.getInt(ARTIST_ITEM_TEXT_SIZE_KEY, 16);
    }

    @Override
    public int getArtistItemBorderPadding() {
        return prefs.getInt(ARTIST_ITEM_BORDER_PADDING_KEY, 16);
    }
}
