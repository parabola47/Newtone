package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Track;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface ArtistTracksView extends MvpView {
    void refreshTracks(List<Track> tracks);
    void setSectionShowing(boolean enable);

    void setArtistName(String artistName);
    void setTracksCountTxt(String tracksCountStr);

    void setCurrentTrack(int trackId);
}
