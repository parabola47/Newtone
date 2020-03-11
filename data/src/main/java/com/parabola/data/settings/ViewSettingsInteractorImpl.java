package com.parabola.data.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.parabola.domain.settings.ViewSettingsInteractor;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public final class ViewSettingsInteractorImpl implements ViewSettingsInteractor {

    private static final String VIEW_SETTINGS_INTERACTOR_IMPL_PREFS = "com.parabola.data.settings.ViewSettingsInteractorImpl";
    private final SharedPreferences prefs;


    private static final String TAB_ALBUM_VIEW_TYPE_KEY = "com.parabola.data.settings.TAB_ALBUM_VIEW_TYPE";
    private final BehaviorSubject<AlbumViewType> tabAlbumViewTypeObserver;

    private static final String ARTIST_ALBUMS_VIEW_TYPE_KEY = "com.parabola.data.settings.ARTIST_ALBUMS_VIEW_TYPE";
    private final BehaviorSubject<AlbumViewType> artistAlbumsViewTypeObserver;


    public ViewSettingsInteractorImpl(Context context) {
        this.prefs = context.getSharedPreferences(VIEW_SETTINGS_INTERACTOR_IMPL_PREFS, Context.MODE_PRIVATE);

        this.tabAlbumViewTypeObserver = BehaviorSubject.createDefault(getTabAlbumViewType());
        this.artistAlbumsViewTypeObserver = BehaviorSubject.createDefault(getArtistAlbumsViewType());
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
