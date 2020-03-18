package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Track;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface AlbumView extends MvpView {
    void setAlbumTitle(String albumTitle);
    void setAlbumArtist(String artistName);
    void setAlbumArt(Object artCover);

    void refreshTracks(List<Track> tracks);

    void setCurrentTrack(int trackId);
}
