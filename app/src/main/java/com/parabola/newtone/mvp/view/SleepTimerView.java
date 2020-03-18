package com.parabola.newtone.mvp.view;

import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface SleepTimerView extends MvpView {

    void showToast(String toastText);
    void closeScreen();
}
