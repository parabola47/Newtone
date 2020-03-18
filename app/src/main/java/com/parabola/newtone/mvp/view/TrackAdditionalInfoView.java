package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Track;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface TrackAdditionalInfoView extends MvpView {
    void setTrack(Track track);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void closeScreen();
}
