package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;

@AddToEndSingle
public interface AlbumView extends MvpView {
    void setAlbumTitle(String albumTitle);
    void setAlbumArtist(String artistName);
    void setAlbumArt(Object artCover);

    void refreshTracks(List<Track> tracks);
    void setItemViewSettings(TrackItemView itemViewSettings);
    void setItemDividerShowing(boolean showed);

    void setCurrentTrack(int trackId);
}
