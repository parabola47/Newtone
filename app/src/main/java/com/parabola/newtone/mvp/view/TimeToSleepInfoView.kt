package com.parabola.newtone.mvp.view;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface TimeToSleepInfoView extends MvpView {
    void updateTimeToEndText(String timeToEndText);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void closeScreen();
}
