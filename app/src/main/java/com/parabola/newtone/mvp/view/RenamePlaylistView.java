package com.parabola.newtone.mvp.view;

import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface RenamePlaylistView extends MvpView {

    void setPlaylistTitle(String playlistTitle);
    void setTitleSelected();

    void showToast(String toastText);

    void closeScreen();
    void setPlaylistTitleIsEmptyError();
}
