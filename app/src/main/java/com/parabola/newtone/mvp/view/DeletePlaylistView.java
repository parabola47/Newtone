package com.parabola.newtone.mvp.view;

import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface DeletePlaylistView extends MvpView {
    void closeScreen();
}
