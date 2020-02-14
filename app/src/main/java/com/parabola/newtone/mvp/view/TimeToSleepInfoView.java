package com.parabola.newtone.mvp.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface TimeToSleepInfoView extends MvpView {
    void updateTimeToEndText(String timeToEndText);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void closeScreen();
}
