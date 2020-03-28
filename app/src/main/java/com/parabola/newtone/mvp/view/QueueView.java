package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface QueueView extends MvpView {
    void refreshTracks(List<Track> tracks);
    void setItemViewSettings(TrackItemView viewSettings);
    void setTrackCount(int tracksCount);
    void setCurrentTrackPosition(int currentTrackPosition);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void removeTrackByPosition(int position);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void goToItem(int itemPosition);
}
