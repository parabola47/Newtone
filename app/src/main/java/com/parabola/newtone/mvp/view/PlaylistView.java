package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Track;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface PlaylistView extends MvpView {
    void setTracksCount(int playlistSize);
    void setPlaylistTitle(String playlistTitle);
    void refreshTracks(List<Track> tracks);

    void setCurrentTrack(int trackId);
}
