package com.parabola.newtone.mvp.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.parabola.domain.model.Track;

import java.util.List;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface AlbumView extends MvpView {
    void setAlbumTitle(String albumTitle);
    void setAlbumArtist(String artistName);
    void setAlbumArt(int albumId, String artLink);

    void refreshTracks(List<Track> tracks);

    void setCurrentTrack(int trackId);
}
