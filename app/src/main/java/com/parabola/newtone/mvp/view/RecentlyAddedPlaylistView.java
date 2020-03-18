package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Track;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface RecentlyAddedPlaylistView extends MvpView {

    void refreshTracks(List<Track> trackList);
    void removeTrack(int trackId);

    void setCurrentTrack(int trackId);

    void clearCurrentItem();
    void clearItems();
}
