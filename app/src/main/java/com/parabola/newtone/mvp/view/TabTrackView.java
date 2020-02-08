package com.parabola.newtone.mvp.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.parabola.domain.model.Track;

import java.util.List;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface TabTrackView extends MvpView {
    void refreshTracks(List<Track> tracks);

    void setCurrentTrack(int trackId);
    void setSectionShowing(boolean enable);

    void removeTrack(int trackId);
}
