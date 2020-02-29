package com.parabola.newtone.mvp.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.parabola.domain.model.Track;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface TrackAdditionalInfoView extends MvpView {
    void setTrack(Track track);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void closeScreen();
}
