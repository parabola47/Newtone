package com.parabola.newtone.mvp.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.parabola.domain.model.Track;

import java.util.List;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface ArtistTracksView extends MvpView {
    void refreshTracks(List<Track> tracks);
    void setSectionShowing(boolean enable);

    void setArtistName(String artistName);
    void setTracksCountTxt(String tracksCountStr);

    void setCurrentTrack(int trackId);
}
