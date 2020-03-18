package com.parabola.newtone.mvp.view;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface CreatePlaylistView extends MvpView {

    void setPlaylistTitleIsEmptyError();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showToast(String toastText);

    void closeScreen();
}
