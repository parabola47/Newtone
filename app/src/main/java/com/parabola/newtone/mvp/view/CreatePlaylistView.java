package com.parabola.newtone.mvp.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface CreatePlaylistView extends MvpView {

    void setPlaylistTitleIsEmptyError();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showToast(String toastText);

    void closeScreen();
}
