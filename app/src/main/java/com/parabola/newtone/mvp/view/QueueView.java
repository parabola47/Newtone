package com.parabola.newtone.mvp.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.parabola.domain.model.Track;

import java.util.List;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface QueueView extends MvpView {
    void refreshTracks(List<Track> tracks);
    void setTrackCount(int tracksCount);
    void setCurrentTrackPosition(int currentTrackPosition);
    void removeTrackByPosition(int position);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void goToItem(int itemPosition);
}
